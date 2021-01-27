package com.motorbesitzen.messagewatcher.bot.service;

import com.motorbesitzen.messagewatcher.data.dao.BadWord;
import com.motorbesitzen.messagewatcher.data.dao.BlacklistedDomain;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.dao.DiscordMember;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.data.repo.DiscordMemberRepo;
import com.motorbesitzen.messagewatcher.util.LogUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class Censor {

	private final DiscordGuildRepo guildRepo;
	private final DiscordMemberRepo memberRepo;

	private static final String LINK_REGEX =
			"(?i)(?:(?:https?|ftp)://)(?![^/]*--)(?![^/]*\\./)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\x{00a1}-\\x{ffff}0-9]-*)*[a-z\\x{00a1}-\\x{ffff}0-9]+)(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}0-9]-*)*[a-z\\x{00a1}-\\x{ffff}0-9]+)*(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?";

	@Autowired
	public Censor(final DiscordGuildRepo guildRepo, final DiscordMemberRepo memberRepo) {
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
		dcMember.increaseMessageCount();

		final String originalContent = message.getContentRaw();
		final String censoredContent = censorContent(dcGuild, dcMember, originalContent);

		if (!originalContent.equalsIgnoreCase(censoredContent)) {
			replaceMessage(message, censoredContent);
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

	private String censorContent(final DiscordGuild dcGuild, final DiscordMember dcMember, final String originalContent) {
		final String censoredContent = censorLinks(dcGuild, dcMember, originalContent);
		return censorWords(dcGuild, dcMember, censoredContent);
	}

	private String censorLinks(final DiscordGuild dcGuild, final DiscordMember dcMember, final String content) {
		final Pattern pattern = Pattern.compile(LINK_REGEX);
		final Matcher matcher = pattern.matcher(content);

		final StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			final String rawLink = matcher.group();
			final String domain = getDomainOfLink(rawLink);
			if (isBlacklistedDomainInGuild(domain, dcGuild)) {
				matcher.appendReplacement(sb, "<LINK CENSORED>");
				dcMember.increaseLinkCensorCount();
			}
		}

		matcher.appendTail(sb);
		return sb.toString();
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

	private String censorWords(final DiscordGuild dcGuild, final DiscordMember dcMember, final String content) {
		String newContent = content;
		for (BadWord badWord : dcGuild.getBadWordsOrderdByWordLength()) {
			final Pattern pattern = badWord.isWildcard() ?
					Pattern.compile("(?i)" + badWord.getWord()) :                            // (?i) to ignore case
					Pattern.compile("(?i)(^|(?<=[\\p{S}\\p{P}\\p{C}\\s]))" + badWord.getWord() + "((?=[\\p{S}\\p{P}\\p{C}\\s])|$)");

			String replacement = badWord.getReplacement();
			final Matcher matcher = pattern.matcher(newContent);
			final StringBuffer sb = new StringBuffer();
			while (matcher.find()) {
				if (matcher.group().startsWith(" ")) {
					replacement = " " + replacement;
				}

				if (matcher.group().endsWith(" ")) {
					replacement = replacement + " ";
				}

				matcher.appendReplacement(sb, replacement);
				dcMember.increaseWordCensorCount();
			}

			matcher.appendTail(sb);
			newContent = sb.toString();
		}

		return newContent;
	}

	private void replaceMessage(final Message message, final String newMessage) {
		final TextChannel channel = message.getTextChannel();
		final EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(new Color(222, 105, 12));
		eb.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
		eb.setTitle("Message censored:").setDescription(newMessage);
		eb.setFooter("Avoid censored words/links in the future!");
		addAttachedImages(eb, message);
		tryCensor(channel, message, eb);
	}

	private void tryCensor(final TextChannel channel, final Message message, final EmbedBuilder eb) {
		try {
			message.delete().queue(
					v -> LogUtil.logDebug("Deleted message to censor."),
					throwable -> LogUtil.logError("Could not delete message!", throwable)
			);

			channel.sendMessage(eb.build()).queue(
					v -> LogUtil.logDebug("Sent replacement message."),
					throwable -> LogUtil.logError("Could not send censored message!", throwable)
			);
		} catch (InsufficientPermissionException e) {
			LogUtil.logWarning("Can not censor a message in \"" + channel.getGuild().getName() + "\": " + e.getPermission().getName());
		} catch (IllegalArgumentException e) {
			LogUtil.logWarning("Can not send censor message in \"" + channel.getGuild().getName() + "\": " + e.getMessage());
		}
	}

	private void addAttachedImages(final EmbedBuilder eb, final Message message) {
		List<Message.Attachment> attachments = message.getAttachments();
		if (attachments.size() > 0) {
			for (Message.Attachment attachment : attachments) {
				if (attachment.isImage()) {
					eb.setImage(attachment.getProxyUrl());
					break;
				}
			}
		}
	}
}
