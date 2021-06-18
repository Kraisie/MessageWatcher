package com.motorbesitzen.messagewatcher.bot.service;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.motorbesitzen.messagewatcher.data.dao.BadWord;
import com.motorbesitzen.messagewatcher.data.dao.BlacklistedDomain;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.dao.DiscordMember;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.data.repo.DiscordMemberRepo;
import com.motorbesitzen.messagewatcher.util.LogUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class Censor {

	private final EnvSettings envSettings;
	private final DiscordGuildRepo guildRepo;
	private final DiscordMemberRepo memberRepo;

	private static final Pattern LINK_PATTERN = Pattern.compile(
			"(?i)(?:(?:https?|ftp)://)(?![^/]*--)(?![^/]*\\./)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\x{00a1}-\\x{ffff}0-9]-*)*[a-z\\x{00a1}-\\x{ffff}0-9]+)(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}0-9]-*)*[a-z\\x{00a1}-\\x{ffff}0-9]+)*(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?"
	);

	private static final Pattern INVITE_PATTERN = Pattern.compile(
			"(?:https?://)?" +                     // Scheme
					"(?:\\w+\\.)?" +                       // Subdomain
					"discord(?:(?:app)?\\.com" +           // Discord domain
					"/invite|\\.gg)/(?<code>[a-z0-9-]+)" + // Path
					"(?:\\?\\S*)?(?:#\\S*)?",              // Useless query or URN appendix
			Pattern.CASE_INSENSITIVE);

	@Autowired
	Censor(final EnvSettings envSettings, final DiscordGuildRepo guildRepo, final DiscordMemberRepo memberRepo) {
		this.envSettings = envSettings;
		this.guildRepo = guildRepo;
		this.memberRepo = memberRepo;
	}

	@Transactional
	public void censorMessage(final Message message) {
		final Guild guild = message.getGuild();
		final Optional<DiscordGuild> dcGuildOpt = guildRepo.findById(guild.getIdLong());
		final DiscordGuild dcGuild = dcGuildOpt.orElseGet(() -> createDiscordGuild(guild));

		final User author = message.getAuthor();
		final Optional<DiscordMember> dcMemberOpt = memberRepo.findByDiscordIdAndGuild_GuildId(author.getIdLong(), guild.getIdLong());
		final DiscordMember dcMember = dcMemberOpt.orElseGet(() -> createMember(author, dcGuild));
		final long originalWarnCount = dcMember.getWarningCount();
		dcMember.increaseMessageCount();

		final String originalContent = message.getContentRaw();
		String censoredContent = originalContent;
		if (dcGuild.shouldCensorInvites()) {
			censoredContent = removeInvites(dcMember, originalContent);
		}

		censoredContent = censorContent(dcGuild, dcMember, censoredContent);
		if (isMessageCensored(originalContent, censoredContent)) {
			final Member self = guild.getSelfMember();
			final Message.Attachment imageAttachment = getImageAttachment(message);
			if (originalWarnCount == dcMember.getWarningCount()) {
				if (self.hasPermission(Permission.MANAGE_WEBHOOKS)) {
					replaceMessageWebhook(message, censoredContent, imageAttachment);
				} else {
					replaceMessageEmbed(message, censoredContent, null, imageAttachment);
				}
			} else {
				replaceMessageEmbed(message, censoredContent,
						"You have used a punishable word. Be aware that further usage will be punished!", imageAttachment);
				if (dcGuild.getCensorBanThreshold() > 0 && originalWarnCount != dcMember.getWarningCount() &&
						dcMember.getWarningCount() >= dcGuild.getCensorBanThreshold()) {
					banMember(guild, dcGuild, dcMember);
					return;
				}

				if (dcGuild.getCensorKickThreshold() > 0 && originalWarnCount != dcMember.getWarningCount() &&
						dcMember.getWarningCount() >= dcGuild.getCensorKickThreshold()) {
					kickMember(guild, dcGuild, dcMember);
				}
			}
		}

		memberRepo.save(dcMember);
	}

	private DiscordGuild createDiscordGuild(final Guild guild) {
		final long guildId = guild.getIdLong();
		final DiscordGuild dcGuild = DiscordGuild.createDefault(guildId);
		guildRepo.save(dcGuild);
		return dcGuild;
	}

	private DiscordMember createMember(final User user, final DiscordGuild dcGuild) {
		final long memberId = user.getIdLong();
		final DiscordMember dcMember = DiscordMember.createDefault(memberId, dcGuild);
		memberRepo.save(dcMember);
		return dcMember;
	}

	private String removeInvites(final DiscordMember dcMember, final String originalContent) {
		final Matcher matcher = INVITE_PATTERN.matcher(originalContent);
		final StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			matcher.appendReplacement(sb, "<INVITE CENSORED>");
			dcMember.increaseLinkCensorCount();
		}

		matcher.appendTail(sb);
		return sb.toString();
	}

	private String censorContent(final DiscordGuild dcGuild, final DiscordMember dcMember, final String content) {
		final String[] originalLines = content.trim().split("\n");
		final List<String> censoredLines = new ArrayList<>();
		for (String line : originalLines) {
			censoredLines.add(censorLine(dcGuild, dcMember, line));
		}

		return generateLineString(censoredLines);
	}

	private String censorLine(final DiscordGuild dcGuild, final DiscordMember dcMember, final String line) {
		final Map<Integer, String> linkMap = new HashMap<>();
		final Map<Integer, String> wordMap = new HashMap<>();
		final Map<Integer, String> emoteMap = new HashMap<>();
		splitLine(line, linkMap, wordMap, emoteMap);
		censorParts(dcGuild, dcMember, linkMap, wordMap);
		return stitchMessage(linkMap, wordMap, emoteMap);
	}

	private boolean isLink(final String token) {
		final Matcher matcher = LINK_PATTERN.matcher(token);
		return matcher.matches();
	}

	private void addToWordMap(final Map<Integer, String> wordMap, final int pos, final String token) {
		if (wordMap.get(pos) == null) {
			wordMap.put(pos, token);
			return;
		}

		final String currentValue = wordMap.get(pos);
		wordMap.put(pos, currentValue + " " + token);
	}

	private void splitLine(final String line, final Map<Integer, String> linkMap, final Map<Integer, String> wordMap, final Map<Integer, String> emoteMap) {
		final String[] tokens = line.split(" +");
		int pos = 0;
		for (int i = 0; i < tokens.length; i++) {
			if (isLink(tokens[i])) {
				linkMap.put(pos, tokens[i]);
			} else if (isEmote(tokens[i])) {
				emoteMap.put(pos, tokens[i]);
			} else {
				int j = i;
				do {
					addToWordMap(wordMap, pos, tokens[j]);
					if (++j >= tokens.length) {
						break;
					}
				} while (!isLink(tokens[j]) && !isEmote(tokens[j]));
				i = j - 1;
			}

			pos++;
		}
	}

	private boolean isEmote(final String token) {
		return token.matches("<a?:.*:[0-9]{15,20}>");
	}

	private void censorParts(final DiscordGuild dcGuild, final DiscordMember dcMember, final Map<Integer, String> linkMap, final Map<Integer, String> wordMap) {
		for (Map.Entry<Integer, String> link : linkMap.entrySet()) {
			link.setValue(censorLink(dcGuild, dcMember, link.getValue()));
		}

		for (Map.Entry<Integer, String> wordPart : wordMap.entrySet()) {
			wordPart.setValue(censorWordPart(dcGuild, dcMember, wordPart.getValue()));
		}
	}

	private String stitchMessage(final Map<Integer, String> linkMap, final Map<Integer, String> wordMap, final Map<Integer, String> emoteMap) {
		final StringBuilder sb = new StringBuilder();
		final int msgSize = linkMap.size() + wordMap.size() + emoteMap.size();
		for (int i = 0; i < msgSize; i++) {
			if (wordMap.get(i) != null) {
				sb.append(wordMap.get(i)).append(" ");
				continue;
			}

			if (emoteMap.get(i) != null) {
				sb.append(emoteMap.get(i)).append(" ");
				continue;
			}

			if (linkMap.get(i) != null) {
				sb.append(linkMap.get(i)).append(" ");
			}
		}

		return sb.toString().trim();
	}

	private String censorLink(final DiscordGuild dcGuild, final DiscordMember dcMember, final String link) {
		final String domain = getDomainOfLink(link);
		if (isBlacklistedDomainInGuild(domain, dcGuild)) {
			dcMember.increaseLinkCensorCount();
			return "<LINK CENSORED>";
		}

		return link;
	}

	private String getDomainOfLink(final String link) {
		try {
			final URI url = new URI(link);
			final String host = url.getHost();

			// URI is bad with unicode like: ✪ and thus returns null
			if (host == null) {
				return "Unknown domain";
			}

			return host;
		} catch (URISyntaxException e) {
			LogUtil.logDebug("Invalid URL: " + link);
			return "";
		}
	}

	private boolean isBlacklistedDomainInGuild(final String domain, final DiscordGuild dcGuild) {
		final Set<BlacklistedDomain> blacklistedDomains = dcGuild.getBlacklistedDomains();
		for (BlacklistedDomain blacklistedDomain : blacklistedDomains) {
			final String domainLower = domain.toLowerCase();
			final String blacklistedDomainLower = blacklistedDomain.getDomain().toLowerCase();
			if (domain.equalsIgnoreCase(blacklistedDomain.getDomain()) || domainLower.endsWith(blacklistedDomainLower)) {
				return true;
			}
		}

		return false;
	}

	private String censorWordPart(final DiscordGuild dcGuild, final DiscordMember dcMember, final String wordPart) {
		String newPart = wordPart;
		for (BadWord badWord : dcGuild.getBadWordsOrderdByWordLengthDesc()) {
			final Pattern pattern = badWord.isWildcard() ?
					Pattern.compile("(?i)" + badWord.getWord()) :                            // (?i) to ignore case
					Pattern.compile("(?i)(^|(?<=[\\p{S}\\p{P}\\p{C}\\s]))" + badWord.getWord() + "((?=[\\p{S}\\p{P}\\p{C}\\s])|$)");

			final String replacement = badWord.getReplacement();
			final Matcher matcher = pattern.matcher(newPart);
			final StringBuilder sb = new StringBuilder();
			while (matcher.find()) {
				matcher.appendReplacement(sb, replacement);
				dcMember.increaseWordCensorCount();

				if (badWord.isPunishable()) {
					dcMember.increaseWarningCount();
				}
			}

			matcher.appendTail(sb);
			newPart = sb.toString();
		}

		return newPart;
	}

	private String generateLineString(final List<String> lines) {
		final StringBuilder sb = new StringBuilder();
		for (String part : lines) {
			sb.append(part).append("\n");
		}

		return sb.toString().trim();
	}

	private boolean isMessageCensored(final String original, final String result) {
		final String originalNoFormat = original.replaceAll("\\s+", "");
		final String resultNoFormat = result.replaceAll("\\s+", "");
		return !originalNoFormat.equalsIgnoreCase(resultNoFormat);
	}

	private void replaceMessageWebhook(final Message message, final String newMessage, final Message.Attachment imageAttachment) {
		final TextChannel channel = message.getTextChannel();
		final Member author = message.getMember();
		if (author == null) {
			message.delete().queue();
			return;
		}

		final Message repliedMessage = message.getReferencedMessage();
		final String newReply =
				repliedMessage != null ?
						repliedMessage.getAuthor().getAsMention() + "\n" + newMessage :
						newMessage;

		channel.retrieveWebhooks().queue(
				webhooks -> {
					if (webhooks.size() == 0) {
						channel.createWebhook("censor").queue(
								fakeWebhook -> message.delete().queue(
										v -> sendWebhookMessage(fakeWebhook, author, newReply, imageAttachment),
										throwable -> replaceMessageEmbed(message, newMessage, "Avoid censored words/links in the future!", imageAttachment)
								)
						);
					} else {
						message.delete().queue(
								v -> sendWebhookMessage(webhooks.get(0), author, newReply, imageAttachment),
								throwable -> replaceMessageEmbed(message, newMessage, "Avoid censored words/links in the future!", imageAttachment)
						);
					}
				}
		);
	}

	private Message.Attachment getImageAttachment(final Message message) {
		final List<Message.Attachment> attachments = message.getAttachments();
		if (attachments.size() == 0) {
			return null;
		}

		final Member author = message.getMember();
		if (author == null) {
			return null;
		}

		final TextChannel channel = message.getTextChannel();
		if (!canPostImages(author, channel)) {
			return null;
		}

		for (Message.Attachment attachment : attachments) {
			if (attachment.isImage()) {
				return attachment;
			}
		}

		return null;
	}

	private boolean canPostImages(final Member author, final TextChannel channel) {
		return author.hasPermission(channel, Permission.MESSAGE_ATTACH_FILES);
	}

	private void sendWebhookMessage(final Webhook fakeWebhook, final Member author, final String newMessage,
									final Message.Attachment imageAttachment) {
		final WebhookClient client = WebhookClient.withUrl(fakeWebhook.getUrl());
		final WebhookMessageBuilder builder = new WebhookMessageBuilder();
		final AllowedMentions noMassPings = new AllowedMentions().withParseEveryone(false).withParseRoles(false).withParseUsers(true);
		builder.setUsername(author.getEffectiveName())
				.setAvatarUrl(author.getUser().getEffectiveAvatarUrl())
				.setContent(getWrappedMessage(newMessage))
				.setAllowedMentions(noMassPings);

		if (imageAttachment != null) {
			builder.append("\n" + imageAttachment.getProxyUrl());
		}

		final TextChannel channel = fakeWebhook.getChannel();
		client.send(builder.build()).thenAcceptAsync(
				webhookReadOnlyMessage -> {
					if (!canEmbedLinks(author, channel)) {
						channel.retrieveMessageById(webhookReadOnlyMessage.getId()).queue(
								webhookMessage -> webhookMessage.suppressEmbeds(true).queue(
										v -> LogUtil.logDebug("Suppressed link embed in webhook."),
										throwable -> LogUtil.logDebug("Can not suppress link embed:", throwable)
								)
						);
					}
				}
		);
		client.close();
	}

	private boolean canEmbedLinks(final Member author, final TextChannel channel) {
		return author.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS);
	}

	private void replaceMessageEmbed(final Message message, final String newMessage, final String warnMessage,
									 final Message.Attachment imageAttachment) {
		final TextChannel channel = message.getTextChannel();
		final EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(envSettings.getEmbedColor());
		eb.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
		eb.setTitle("Message censored:").setDescription(getWrappedMessage(newMessage));
		eb.setFooter(warnMessage);
		addAttachment(eb, imageAttachment);
		tryCensor(channel, message, eb);
	}

	private String getWrappedMessage(final String message) {
		if (message.length() <= 1950) {
			return message;
		}

		return message.substring(0, 1949) + "...";
	}

	private void tryCensor(final TextChannel channel, final Message message, final EmbedBuilder eb) {
		final Message repliedMessage = message.getReferencedMessage();
		try {
			message.delete().queue(
					v -> LogUtil.logDebug("Deleted message to censor."),
					throwable -> LogUtil.logError("Could not delete message!", throwable)
			);

			if (repliedMessage != null) {
				repliedMessage.replyEmbeds(eb.build()).mentionRepliedUser(false).queue(
						v -> LogUtil.logDebug("Sent replacement message."),
						throwable -> LogUtil.logError("Could not send censored message!", throwable)
				);
			} else {
				channel.sendMessageEmbeds(eb.build()).queue(
						v -> LogUtil.logDebug("Sent replacement message."),
						throwable -> LogUtil.logError("Could not send censored message!", throwable)
				);
			}
		} catch (InsufficientPermissionException e) {
			LogUtil.logWarning("Can not censor a message in \"" + channel.getGuild().getName() + "\": " + e.getPermission().getName());
		} catch (IllegalArgumentException e) {
			LogUtil.logWarning("Can not send censor message in \"" + channel.getGuild().getName() + "\": " + e.getMessage());
		}
	}

	private void addAttachment(final EmbedBuilder eb, final Message.Attachment attachment) {
		if (attachment == null) {
			return;
		}

		if (attachment.isImage()) {
			eb.setImage(attachment.getProxyUrl());
		}
	}

	private void banMember(final Guild guild, final DiscordGuild dcGuild, final DiscordMember dcMember) {
		try {
			tryBan(guild, dcGuild, dcMember);
		} catch (HierarchyException e) {
			LogUtil.logDebug("Could not ban member " + dcMember.getDiscordId() + " on \"" + guild.getName() + "\".", e);
		}
	}

	private void tryBan(final Guild guild, final DiscordGuild dcGuild, final DiscordMember dcMember) throws HierarchyException {
		guild.retrieveMemberById(dcMember.getDiscordId()).queue(
				member -> {
					if (guild.getSelfMember().canInteract(member)) {
						member.ban(0, "Banned for reaching " + dcGuild.getCensorBanThreshold() +
								" censor infractions (infractions: " + dcMember.getWarningCount() + ")!").queue(
								v -> LogUtil.logInfo("Banned " + member.getUser().getAsTag() + " for reaching the censor limit of guild " + guild.getName()),
								throwable -> LogUtil.logDebug("Could not ban " + member.getUser().getAsTag() + " for reaching the " +
										"censor limit of guild \"" + guild.getName(), throwable)
						);
					} else {
						LogUtil.logDebug("Can not interact (censor ban) with " + member.getUser().getAsTag() + " (" + member.getId() + ").");
					}
				},
				throwable -> LogUtil.logDebug("Could not retrieve author to ban for censor.", throwable)
		);
	}

	private void kickMember(final Guild guild, final DiscordGuild dcGuild, final DiscordMember dcMember) {
		try {
			tryKick(guild, dcGuild, dcMember);
		} catch (HierarchyException e) {
			LogUtil.logDebug("Could not kick member " + dcMember.getDiscordId() + " on \"" + guild.getName() + "\".", e);
		}
	}

	private void tryKick(final Guild guild, final DiscordGuild dcGuild, final DiscordMember dcMember) throws HierarchyException {
		guild.retrieveMemberById(dcMember.getDiscordId()).queue(
				member -> {
					if (guild.getSelfMember().canInteract(member)) {
						member.kick("Kicked for reaching " + dcGuild.getCensorKickThreshold() +
								" censor infractions (infractions: " + dcMember.getWarningCount() + ")!").queue(
								v -> LogUtil.logInfo("Kicked " + member.getUser().getAsTag() + " for reaching the censor limit of guild " + guild.getName()),
								throwable -> LogUtil.logDebug("Could not kick " + member.getUser().getAsTag() + " for reaching the " +
										"censor limit of guild \"" + guild.getName(), throwable)
						);
					} else {
						LogUtil.logDebug("Can not interact (censor kick) with " + member.getUser().getAsTag() + " (" + member.getId() + ").");
					}
				},
				throwable -> LogUtil.logDebug("Could not retrieve author to kick for censor.", throwable)
		);
	}
}
