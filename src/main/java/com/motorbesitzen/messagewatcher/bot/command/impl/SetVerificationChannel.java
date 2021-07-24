package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("verifychannel")
class SetVerificationChannel extends CommandImpl {

	private final DiscordGuildRepo guildRepo;

	@Autowired
	SetVerificationChannel(final DiscordGuildRepo guildRepo) {
		this.guildRepo = guildRepo;
	}

	@Override
	public String getName() {
		return "verifychannel";
	}

	@Override
	public String getUsage() {
		return getName() + " #channel";
	}

	@Override
	public String getDescription() {
		return "Sets the channel in which message verifications happen.";
	}

	@Transactional
	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final long guildId = event.getGuild().getIdLong();
		final Optional<DiscordGuild> discordGuildOpt = guildRepo.findById(guildId);
		final DiscordGuild dcGuild = discordGuildOpt.orElseGet(() -> createDiscordGuild(guildId));
		final Message message = event.getMessage();
		final List<TextChannel> mentionedChannels = message.getMentionedChannels();
		if (mentionedChannels.size() == 0) {
			sendErrorMessage(event.getChannel(), "Please provide one or more channels to whitelist.");
			return;
		}

		final TextChannel verificationChannel = mentionedChannels.get(0);
		dcGuild.setVerifyChannelId(verificationChannel.getIdLong());
		guildRepo.save(dcGuild);
		answer(event.getChannel(), "Set verification channel.");
	}

	private DiscordGuild createDiscordGuild(final long guildId) {
		final DiscordGuild dcGuild = DiscordGuild.createDefault(guildId);
		guildRepo.save(dcGuild);
		return dcGuild;
	}
}
