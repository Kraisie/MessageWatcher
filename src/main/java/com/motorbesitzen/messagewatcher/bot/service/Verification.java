package com.motorbesitzen.messagewatcher.bot.service;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.receive.ReadonlyMessage;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.dao.DiscordMember;
import com.motorbesitzen.messagewatcher.data.dao.MessageVerification;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.data.repo.DiscordMemberRepo;
import com.motorbesitzen.messagewatcher.data.repo.MessageVerificationRepo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Verification {

	private final DiscordGuildRepo guildRepo;
	private final DiscordMemberRepo memberRepo;
	private final MessageVerificationRepo verificationRepo;

	private Verification(final DiscordGuildRepo guildRepo, final DiscordMemberRepo memberRepo, final MessageVerificationRepo verificationRepo) {
		this.guildRepo = guildRepo;
		this.memberRepo = memberRepo;
		this.verificationRepo = verificationRepo;
	}

	public boolean isRequired(final Message message) {
		final long senderId = message.getAuthor().getIdLong();
		final long guildId = message.getGuild().getIdLong();
		return !verificationRepo.existsBySender_DiscordIdAndGuild_GuildId(senderId, guildId);
	}

	public void start(final GuildMessageReceivedEvent event) {
		// first create the DB object and fill it with info later
		// this gets done to prevent triggering multiple verifications by spamming messages
		// (up to 5 as you can send 5 messages per second on Discord)
		final Message message = event.getMessage();
		final MessageVerification messageVerification = createMessageVerification(message);
		message.delete().queue(
				v -> sendVerifyInformation(event, messageVerification)
		);
	}

	private MessageVerification createMessageVerification(final Message message) {
		final String content = message.getContentRaw();
		final Guild guild = message.getGuild();
		final long guildId = guild.getIdLong();
		final Optional<DiscordGuild> dcGuildOpt = guildRepo.findById(guildId);
		final DiscordGuild dcGuild = dcGuildOpt.orElseGet(() -> createNewGuild(guildId));
		final User author = message.getAuthor();
		final long senderId = author.getIdLong();
		final Optional<DiscordMember> senderOpt = memberRepo.findByDiscordIdAndGuild_GuildId(senderId, guildId);
		final DiscordMember sender = senderOpt.orElseGet(() -> DiscordMember.createDefault(senderId, dcGuild));
		final MessageVerification messageVerification = new MessageVerification(sender, content, dcGuild);
		verificationRepo.save(messageVerification);
		return messageVerification;
	}

	private void sendVerifyInformation(final GuildMessageReceivedEvent event, final MessageVerification messageVerification) {
		final TextChannel channel = event.getChannel();
		channel.retrieveWebhooks().queue(
				webhooks -> {
					if (webhooks.size() == 0) {
						channel.createWebhook("verification").queue(
								fakeWebhook -> sendInfoMessage(event, messageVerification, fakeWebhook)
						);
					} else {
						sendInfoMessage(event, messageVerification, webhooks.get(0));
					}
				}
		);
	}

	private void sendInfoMessage(final GuildMessageReceivedEvent event, final MessageVerification messageVerification, final Webhook webhook) {
		final Member author = event.getMember();
		if (author == null) {
			return;
		}

		final WebhookClient client = WebhookClient.withUrl(webhook.getUrl());
		final WebhookMessageBuilder builder = new WebhookMessageBuilder();
		final AllowedMentions noMassPings = new AllowedMentions().withParseEveryone(false).withParseRoles(false).withParseUsers(true);
		builder.setUsername(author.getEffectiveName())
				.setAvatarUrl(author.getUser().getEffectiveAvatarUrl())
				.setContent(
						"**<VERIFICATION NEEDED>**\n\nThis message requires verification by staff. Its content will stay " +
								"hidden until verified.\n**Any message until verification will get deleted and will be lost!**"
				).setAllowedMentions(noMassPings);

		client.send(builder.build()).thenAcceptAsync(
				webhookMessage -> sendVerifyMessage(event, messageVerification, webhookMessage)
		);
		client.close();
	}

	private void sendVerifyMessage(final GuildMessageReceivedEvent event, final MessageVerification messageVerification, final ReadonlyMessage webhookMessage) {
		final DiscordGuild dcGuild = messageVerification.getGuild();
		final long verifyChannelId = dcGuild.getVerifyChannelId();
		final TextChannel verifyChannel = event.getGuild().getTextChannelById(verifyChannelId);
		if (verifyChannel == null) {
			verificationRepo.delete(messageVerification);
			return;
		}

		final Message message = event.getMessage();
		final String content = message.getContentRaw();
		final MessageEmbed embed = buildEmbed(content, event.getAuthor());
		verifyChannel.sendMessageEmbeds(embed).queue(
				msg -> {
					final long originalChannelId = webhookMessage.getChannelId();
					final long originalMessageId = webhookMessage.getId();
					final long verificationMessageId = msg.getIdLong();
					messageVerification.setOriginalChannelId(originalChannelId);
					messageVerification.setOriginalMessageId(originalMessageId);
					messageVerification.setVerifyMessageId(verificationMessageId);
					verificationRepo.save(messageVerification);
					msg.addReaction("✅").queue(
							v -> msg.addReaction("❌").queue()
					);
				}
		);
	}

	private DiscordGuild createNewGuild(final long guildId) {
		final DiscordGuild dcGuild = DiscordGuild.createDefault(guildId);
		guildRepo.save(dcGuild);
		return dcGuild;
	}

	private MessageEmbed buildEmbed(final String content, final User author) {
		return new EmbedBuilder()
				.setTitle("Verification required!")
				.setAuthor(author.getName(), null, author.getEffectiveAvatarUrl())
				.setDescription(content)
				.build();
	}
}
