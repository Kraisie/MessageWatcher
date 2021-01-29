package com.motorbesitzen.messagewatcher.bot.event;

import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.data.repo.ModRoleRepo;
import com.motorbesitzen.messagewatcher.data.repo.WhitelistedChannelRepo;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Optional;

/**
 * Handles text channel and role deletions. If a channel or role was authorized it gets removed
 * from the list of authorized channels/roles for that guild.
 */
public class DeletionListener extends ListenerAdapter {

	private final DiscordGuildRepo guildRepo;
	private final ModRoleRepo roleRepo;
	private final WhitelistedChannelRepo channelRepo;

	public DeletionListener(final DiscordGuildRepo guildRepo, final ModRoleRepo roleRepo, final WhitelistedChannelRepo channelRepo) {
		this.guildRepo = guildRepo;
		this.roleRepo = roleRepo;
		this.channelRepo = channelRepo;
	}

	/**
	 * Removes text channel form authorized channel list on channel deletion.
	 *
	 * @param event The deletion event triggered by Discord.
	 */
	@Override
	public void onTextChannelDelete(final TextChannelDeleteEvent event) {
		final TextChannel deletedChannel = event.getChannel();
		if (channelRepo.existsById(deletedChannel.getIdLong())) {
			channelRepo.deleteById(deletedChannel.getIdLong());
		}
	}

	/**
	 * Removes role form authorized role list on role deletion.
	 *
	 * @param event The deletion event triggered by Discord.
	 */
	@Override
	public void onRoleDelete(final RoleDeleteEvent event) {
		final Role role = event.getRole();
		if (roleRepo.existsById(role.getIdLong())) {
			roleRepo.deleteById(role.getIdLong());
		}
	}

	@Override
	public void onEmoteRemoved(final EmoteRemovedEvent event) {
		final Emote emote = event.getEmote();
		final Optional<DiscordGuild> dcGuildOpt = guildRepo.findById(event.getGuild().getIdLong());
		dcGuildOpt.ifPresent(dcGuild -> {
			if (emote.getIdLong() == dcGuild.getReportEmoteId()) {
				dcGuild.setReportEmoteId(0);
				guildRepo.save(dcGuild);
			}
		});
	}
}
