package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.dao.WhitelistedChannel;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("whitelist")
class AddWhitelistedChannel extends CommandImpl {

	private final DiscordGuildRepo guildRepo;

	@Autowired
	AddWhitelistedChannel(final DiscordGuildRepo guildRepo) {
		this.guildRepo = guildRepo;
	}

	@Override
	public String getName() {
		return "whitelist";
	}

	@Override
	public String getUsage() {
		return getName() + " #channel+";
	}

	@Override
	public String getDescription() {
		return "Whitelists a channel so the bot does not censor any messages in there. Can be used with " +
				"multiple channels.";
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

		authorizeChannels(dcGuild, mentionedChannels);
		answer(event.getChannel(), "Whitelisted mentioned channels.");
	}

	private DiscordGuild createDiscordGuild(final long guildId) {
		final DiscordGuild dcGuild = DiscordGuild.createDefault(guildId);
		guildRepo.save(dcGuild);
		return dcGuild;
	}

	private void authorizeChannels(final DiscordGuild dcGuild, final List<TextChannel> channels) {
		for (TextChannel channel : channels) {
			final long channelId = channel.getIdLong();
			final WhitelistedChannel whitelistedChannel = new WhitelistedChannel(channelId, dcGuild);
			dcGuild.addWhitelistedChannel(whitelistedChannel);
		}

		guildRepo.save(dcGuild);
	}
}
