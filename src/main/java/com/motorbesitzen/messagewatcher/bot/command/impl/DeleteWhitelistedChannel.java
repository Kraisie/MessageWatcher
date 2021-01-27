package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.dao.WhitelistedChannel;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.data.repo.WhitelistedChannelRepo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public class DeleteWhitelistedChannel extends CommandImpl {

	private final DiscordGuildRepo guildRepo;
	private final WhitelistedChannelRepo channelRepo;

	public DeleteWhitelistedChannel(final DiscordGuildRepo guildRepo, final WhitelistedChannelRepo channelRepo) {
		this.guildRepo = guildRepo;
		this.channelRepo = channelRepo;
	}

	@Transactional
	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final long guildId = event.getGuild().getIdLong();
		final Optional<DiscordGuild> discordGuildOpt = guildRepo.findById(guildId);
		discordGuildOpt.ifPresentOrElse(
				dcGuild -> deleteWhitelistedChannels(event, dcGuild),
				() -> sendErrorMessage(event.getChannel(), "Channels are not authorized!")
		);
	}

	private void deleteWhitelistedChannels(final GuildMessageReceivedEvent event, final DiscordGuild dcGuild) {
		final Message message = event.getMessage();
		final List<TextChannel> mentionedChannels = message.getMentionedChannels();
		if (mentionedChannels.size() == 0) {
			sendErrorMessage(event.getChannel(), "Please provide one or more channels to remove from the whitelist.");
			return;
		}

		deauthorizeChannels(dcGuild, mentionedChannels);
		answer(event.getChannel(), "Removed mentioned channels from the whitelist.");
	}

	private void deauthorizeChannels(final DiscordGuild dcGuild, final List<TextChannel> channels) {
		for (TextChannel channel : channels) {
			final long channelId = channel.getIdLong();
			final Optional<WhitelistedChannel> whitelistedChannelOpt = channelRepo.findByChannelIdAndGuild_GuildId(channelId, dcGuild.getGuildId());
			whitelistedChannelOpt.ifPresent(channelRepo::delete);
		}
	}
}
