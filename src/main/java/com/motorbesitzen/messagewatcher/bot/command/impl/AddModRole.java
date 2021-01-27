package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.dao.ModRole;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public class AddModRole extends CommandImpl {

	private final DiscordGuildRepo guildRepo;

	public AddModRole(final DiscordGuildRepo guildRepo) {
		this.guildRepo = guildRepo;
	}

	@Transactional
	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final long guildId = event.getGuild().getIdLong();
		final Optional<DiscordGuild> discordGuildOpt = guildRepo.findById(guildId);
		final DiscordGuild dcGuild = discordGuildOpt.orElseGet(() -> createDiscordGuild(guildId));
		final Message message = event.getMessage();
		final List<Role> mentionedRoles = message.getMentionedRoles();
		if (mentionedRoles.size() == 0) {
			sendErrorMessage(event.getChannel(), "Please provide one or more roles to assign mod status for this bot.");
			return;
		}

		authorizeRoles(dcGuild, mentionedRoles);
		answer(event.getChannel(), "Authorized mentioned roles.");
	}

	private DiscordGuild createDiscordGuild(final long guildId) {
		final DiscordGuild dcGuild = DiscordGuild.createDefault(guildId);
		guildRepo.save(dcGuild);
		return dcGuild;
	}

	private void authorizeRoles(final DiscordGuild dcGuild, final List<Role> roles) {
		for (Role role : roles) {
			final long roleId = role.getIdLong();
			final ModRole modRole = new ModRole(roleId, dcGuild);
			dcGuild.addModRole(modRole);
		}

		guildRepo.save(dcGuild);
	}
}
