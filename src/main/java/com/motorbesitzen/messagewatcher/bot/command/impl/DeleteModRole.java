package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.dao.ModRole;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.data.repo.ModRoleRepo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("delmod")
class DeleteModRole extends CommandImpl {

	private final DiscordGuildRepo guildRepo;
	private final ModRoleRepo roleRepo;

	@Autowired
	DeleteModRole(final DiscordGuildRepo guildRepo, final ModRoleRepo roleRepo) {
		this.guildRepo = guildRepo;
		this.roleRepo = roleRepo;
	}

	@Override
	public String getName() {
		return "delmod";
	}

	@Override
	public String getUsage() {
		return getName() + " @role+";
	}

	@Override
	public String getDescription() {
		return "Deletes a mod role from the database. Can be used with multiple roles.";
	}

	@Transactional
	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final long guildId = event.getGuild().getIdLong();
		final Optional<DiscordGuild> discordGuildOpt = guildRepo.findById(guildId);
		discordGuildOpt.ifPresentOrElse(
				dcGuild -> deleteModRoles(event, dcGuild),
				() -> sendErrorMessage(event.getChannel(), "Roles are not authorized!")
		);
	}

	private void deleteModRoles(final GuildMessageReceivedEvent event, final DiscordGuild dcGuild) {
		final Message message = event.getMessage();
		final List<Role> mentionedRoles = message.getMentionedRoles();
		if (mentionedRoles.size() == 0) {
			sendErrorMessage(event.getChannel(), "Please provide one or more roles to remove from the mod roles.");
			return;
		}

		deauthorizeRoles(dcGuild, mentionedRoles);
		answer(event.getChannel(), "Deauthorized mentioned roles.");
	}

	private void deauthorizeRoles(final DiscordGuild dcGuild, final List<Role> roles) {
		for (Role role : roles) {
			final long roleId = role.getIdLong();
			final Optional<ModRole> modRoleOpt = roleRepo.findByRoleIdAndGuild_GuildId(roleId, dcGuild.getGuildId());
			modRoleOpt.ifPresent(roleRepo::delete);
		}
	}
}
