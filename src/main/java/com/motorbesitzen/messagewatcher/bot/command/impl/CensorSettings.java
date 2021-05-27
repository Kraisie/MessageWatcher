package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.util.ParseUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("censorsettings")
class CensorSettings extends CommandImpl {

	private final DiscordGuildRepo guildRepo;

	@Autowired
	CensorSettings(final DiscordGuildRepo guildRepo) {
		this.guildRepo = guildRepo;
	}

	@Override
	public String getName() {
		return "censorsettings";
	}

	@Override
	public String getUsage() {
		return getName() + " punishableCensorsToKick punishableCensorsToBan";
	}

	@Override
	public String getDescription() {
		return "Sets the amount of censors of punishable words that triggers a kick or a ban of a user.";
	}

	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final Message message = event.getMessage();
		final String content = message.getContentRaw();
		final long guildId = event.getGuild().getIdLong();
		final Optional<DiscordGuild> discordGuildOpt = guildRepo.findById(guildId);
		final DiscordGuild dcGuild = discordGuildOpt.orElseGet(() -> createDiscordGuild(guildId));
		final String[] tokens = content.split(" ");
		if (tokens.length <= 2) {
			sendErrorMessage(event.getChannel(), "Pleas use the correct syntax!");
			return;
		}

		final String censorsToKickText = tokens[tokens.length - 2];
		final String censorsToBanText = tokens[tokens.length - 1];
		final long censorsToKick = Math.max(0, ParseUtil.safelyParseStringToLong(censorsToKickText));
		final long censorsToBan = Math.max(0, ParseUtil.safelyParseStringToLong(censorsToBanText));
		if (censorsToKick < 0 || censorsToBan < 0) {
			sendErrorMessage(event.getChannel(), "Please use valid values (>= 0)!");
		}

		dcGuild.setCensorKickThreshold(censorsToKick);
		dcGuild.setCensorBanThreshold(censorsToBan);
		guildRepo.save(dcGuild);
		answer(event.getChannel(), "Updated the censor settings.");
	}

	private DiscordGuild createDiscordGuild(final long guildId) {
		final DiscordGuild dcGuild = DiscordGuild.createDefault(guildId);
		guildRepo.save(dcGuild);
		return dcGuild;
	}
}
