package com.motorbesitzen.messagewatcher.bot.event;

import club.minnced.discord.webhook.WebhookClient;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.dao.MessageVerification;
import com.motorbesitzen.messagewatcher.data.dao.ModRole;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.data.repo.MessageVerificationRepo;
import com.motorbesitzen.messagewatcher.data.repo.ModRoleRepo;
import com.motorbesitzen.messagewatcher.util.LogUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReactionListener extends ListenerAdapter {

	private final DiscordGuildRepo guildRepo;
	private final ModRoleRepo roleRepo;
	private final MessageVerificationRepo verificationRepo;

	@Autowired
	ReactionListener(final DiscordGuildRepo guildRepo, final ModRoleRepo roleRepo, final MessageVerificationRepo verificationRepo) {
		this.guildRepo = guildRepo;
		this.roleRepo = roleRepo;
		this.verificationRepo = verificationRepo;
	}

	@Override
	public void onGuildMessageReactionAdd(final GuildMessageReactionAddEvent event) {
		if (event.getUser().isBot()) {
			return;
		}

		event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(
				msg -> handleReaction(event, msg),
				throwable -> LogUtil.logDebug("Could not retrieve message of react with ID " + event.getMessageIdLong(), throwable)
		);
	}

	private void handleReaction(final GuildMessageReactionAddEvent event, final Message message) {
		final Guild guild = event.getGuild();
		final long guildId = guild.getIdLong();
		final long messageId = message.getIdLong();
		final Optional<MessageVerification> messageVerificationOpt = verificationRepo.findByVerifyMessageIdAndGuild_GuildId(messageId, guildId);
		if (messageVerificationOpt.isPresent() && isAuthorized(event.getMember())) {
			if (!isAuthorized(event.getMember())) {
				message.removeReaction((Emote) event.getReactionEmote(), event.getUser()).queue();
				return;
			}

			handleVerification(event, message, messageVerificationOpt.get());
			return;
		}

		handleReport(event, message);
	}

	private boolean isAuthorized(final Member member) {
		if (member.hasPermission(Permission.ADMINISTRATOR)) {
			return true;
		}

		final List<Role> memberRoles = member.getRoles();
		final List<ModRole> modRoles = roleRepo.findAllByGuild_GuildId(member.getGuild().getIdLong());
		for (Role memberRole : memberRoles) {
			for (ModRole modRole : modRoles) {
				if (memberRole.getIdLong() == modRole.getRoleId()) {
					return true;
				}
			}
		}

		return false;
	}

	private void handleVerification(final GuildMessageReactionAddEvent event, final Message message, final MessageVerification messageVerification) {
		final MessageReaction.ReactionEmote emote = event.getReactionEmote();
		if (emote.isEmoji()) {
			final String emoji = emote.getEmoji();
			if (emoji.equals("✅")) {
				handleMessageVerificationAccept(message, messageVerification);
			} else if (emoji.equals("❌")) {
				handleMessageVerificationDecline(message, messageVerification);
			} else {
				message.removeReaction((Emote) emote).queue();
			}
		}
	}

	private void handleMessageVerificationAccept(final Message message, final MessageVerification messageVerification) {
		final Guild guild = message.getGuild();
		final long originalChannelId = messageVerification.getOriginalChannelId();
		final TextChannel originalChannel = guild.getTextChannelById(originalChannelId);
		if (originalChannel == null) {
			message.reply("The channel does not exist anymore!").queue();
			return;
		}

		final long originalMessageId = messageVerification.getOriginalMessageId();
		final String originalContent = messageVerification.getMessageContent();
		editWebhookMessage(originalChannel, originalMessageId, originalContent);
		message.clearReactions().queue();
		verificationRepo.delete(messageVerification);
	}

	private void handleMessageVerificationDecline(final Message message, final MessageVerification messageVerification) {
		final Guild guild = message.getGuild();
		final long originalChannelId = messageVerification.getOriginalChannelId();
		final TextChannel originalChannel = guild.getTextChannelById(originalChannelId);
		if (originalChannel == null) {
			return;
		}

		final long originalMessageId = messageVerification.getOriginalMessageId();
		editWebhookMessage(originalChannel, originalMessageId,
				"**<VERIFICATION DENIED>**\n\nThis content has been disapproved by staff.");
		message.clearReactions().queue();
		verificationRepo.delete(messageVerification);
	}

	private void editWebhookMessage(final TextChannel channel, final long messageId, final String content) {
		channel.retrieveWebhooks().queue(
				webhooks -> {
					if (webhooks.size() == 0) {
						channel.createWebhook("verification").queue(
								webhook -> {
									final WebhookClient client = WebhookClient.withUrl(webhook.getUrl());
									client.edit(messageId, content);
									client.close();
								}
						);
					} else {
						final WebhookClient client = WebhookClient.withUrl(webhooks.get(0).getUrl());
						client.edit(messageId, content);
						client.close();
					}
				}
		);
	}

	private void handleReport(final GuildMessageReactionAddEvent event, final Message message) {
		final Guild guild = event.getGuild();
		final long guildId = guild.getIdLong();
		final Optional<DiscordGuild> discordGuildOpt = guildRepo.findById(guildId);
		final DiscordGuild dcGuild = discordGuildOpt.orElseGet(() -> createDiscordGuild(guildId));
		if (dcGuild.getReportEmoteId() == 0 || dcGuild.getReportCountThreshold() == 0 || dcGuild.getReportChannelId() == 0) {
			return;
		}

		final Emote reportEmote = guild.getEmoteById(dcGuild.getReportEmoteId());
		if (reportEmote == null || !reportEmote.isAvailable()) {
			LogUtil.logInfo("Removing report emote on \"" + guild.getName() + "\" due to not being available!");
			dcGuild.setReportEmoteId(0L);
			guildRepo.save(dcGuild);
			return;
		}

		if (event.getReactionEmote().isEmoji()) {
			return;
		}

		if (event.getReactionEmote().getIdLong() != reportEmote.getIdLong()) {
			return;
		}

		if (isReport(dcGuild, message, reportEmote)) {
			sendReportMessage(message, reportEmote, dcGuild);
		}
	}

	private DiscordGuild createDiscordGuild(final long guildId) {
		final DiscordGuild dcGuild = DiscordGuild.createDefault(guildId);
		guildRepo.save(dcGuild);
		return dcGuild;
	}

	private boolean isReport(final DiscordGuild dcGuild, final Message message, final Emote reportEmote) {
		final List<MessageReaction> reactions = message.getReactions();
		for (MessageReaction reaction : reactions) {
			if (reaction.getReactionEmote().isEmoji()) {
				continue;
			}

			if (reaction.getReactionEmote().getIdLong() == reportEmote.getIdLong()) {
				return hasReachedThreshold(dcGuild, reaction);
			}
		}

		return false;
	}

	private boolean hasReachedThreshold(final DiscordGuild dcGuild, final MessageReaction reaction) {
		final long reactionCount = reaction.getCount();
		return dcGuild.getReportCountThreshold() == reactionCount;
	}

	private void sendReportMessage(final Message message, final Emote reportEmote, final DiscordGuild dcGuild) {
		final Guild guild = message.getGuild();
		final TextChannel reportChannel = guild.getTextChannelById(dcGuild.getReportChannelId());
		if (reportChannel == null) {
			LogUtil.logInfo("Removing report channel on \"" + guild.getName() + "\" due to not being present!");
			dcGuild.setReportChannelId(0L);
			guildRepo.save(dcGuild);
			return;
		}

		buildEmbedReport(reportChannel, message, reportEmote);
	}

	private void buildEmbedReport(final TextChannel reportChannel, final Message message, final Emote reportEmote) {
		final EmbedBuilder reportEmbed = new EmbedBuilder();
		final String author = message.isWebhookMessage() ? "a webhook" : message.getAuthor().getAsTag();
		reportEmbed.setTitle("A message by " + author + " has been reported!");
		reportEmbed.setColor(Color.RED);
		reportEmbed.addField("Sender", message.getAuthor().getAsMention(), false);

		final String content = getWrappedContent(message.getContentRaw());
		reportEmbed.addField("Content", "\"" + content + "\"", false);

		buildEmbedCallback(reportChannel, reportEmbed, message, reportEmote);
	}

	private String getWrappedContent(final String content) {
		if (content.length() < 1000) {
			return content;
		}

		return content.substring(0, 999) + "...";
	}

	private void buildEmbedCallback(final TextChannel reportChannel, final EmbedBuilder reportEmbed, final Message message, final Emote reportEmote) {
		final List<MessageReaction> reactions = message.getReactions();
		for (MessageReaction reaction : reactions) {
			if (reaction.getReactionEmote().isEmoji()) {
				continue;
			}

			if (reaction.getReactionEmote().getIdLong() == reportEmote.getIdLong()) {
				reaction.retrieveUsers().queue(
						reporters -> buildEmbedCallbackReporters(reportChannel, reportEmbed, message, reporters),
						throwable -> {
							LogUtil.logError("Could not request the users who reacted to a reported message!", throwable);
							buildEmbedCallbackReporters(reportChannel, reportEmbed, message, new ArrayList<>());
						}
				);
			}
		}
	}

	private void buildEmbedCallbackReporters(final TextChannel reportChannel, final EmbedBuilder reportEmbed, final Message message, final List<User> reporters) {
		final StringBuilder users = new StringBuilder();
		for (User user : reporters) {
			users.append(user.getAsMention()).append(" ");
		}

		final String reportUsers = users.length() > 0 ? users.toString() : "Could not request users who reported that message!";
		final String messageLink = "https://discord.com/channels/" + message.getGuild().getId() + "/" + message.getTextChannel().getId() + "/" + message.getId();
		reportEmbed.addField("Reported by", reportUsers, false);
		reportEmbed.addBlankField(false);
		reportEmbed.addField("Direct link", messageLink, false);
		reportEmbed.setFooter("Message content can be empty if it consists of a file attachment other than a picture!");

		final List<Message.Attachment> attachments = message.getAttachments();
		for (Message.Attachment attachment : attachments) {
			if (attachment.isImage()) {
				reportEmbed.setImage(attachment.getProxyUrl());
				break;
			}
		}

		reportChannel.sendMessageEmbeds(reportEmbed.build()).queue();
	}
}
