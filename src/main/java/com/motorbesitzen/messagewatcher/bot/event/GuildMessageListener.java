package com.motorbesitzen.messagewatcher.bot.event;

import com.motorbesitzen.messagewatcher.bot.command.Command;
import com.motorbesitzen.messagewatcher.bot.service.Censor;
import com.motorbesitzen.messagewatcher.bot.service.EnvSettings;
import com.motorbesitzen.messagewatcher.bot.service.Verification;
import com.motorbesitzen.messagewatcher.data.dao.DiscordMember;
import com.motorbesitzen.messagewatcher.data.repo.DiscordMemberRepo;
import com.motorbesitzen.messagewatcher.data.repo.ModRoleRepo;
import com.motorbesitzen.messagewatcher.data.repo.WhitelistedChannelRepo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GuildMessageListener extends ListenerAdapter {

	private final Censor censor;
	private final EnvSettings envSettings;
	private final Map<String, Command> commandMap;
	private final WhitelistedChannelRepo channelRepo;
	private final ModRoleRepo roleRepo;
	private final DiscordMemberRepo memberRepo;
	private final Verification verification;

	@Autowired
	GuildMessageListener(final Censor censor, final EnvSettings envSettings, final Map<String, Command> commandMap,
						 final WhitelistedChannelRepo channelRepo, final ModRoleRepo roleRepo,
						 final DiscordMemberRepo memberRepo, final Verification verification) {
		this.censor = censor;
		this.envSettings = envSettings;
		this.commandMap = commandMap;
		this.channelRepo = channelRepo;
		this.roleRepo = roleRepo;
		this.memberRepo = memberRepo;
		this.verification = verification;
	}

	@Override
	public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
		// handle bot/webhook messages
		final Message message = event.getMessage();
		if (isInvalidMessage(message)) {
			return;
		}

		// handle channel the bot can not talk in
		final TextChannel channel = event.getChannel();
		if (!channel.canTalk()) {
			return;
		}

		// check for needed verification
		if (isUntrustedSender(message)) {
			if (verification.isRequired(message)) {
				verification.start(event);
			} else {
				message.delete().queue();
			}
			return;
		}

		// check for command
		final String prefix = envSettings.getCommandPrefix();
		final String rawMessage = message.getContentRaw();
		if (rawMessage.startsWith(prefix)) {
			final Command command = identifyCommand(prefix, rawMessage);
			if (command != null) {
				executeCommand(event, command);
				return;
			}
		}

		// handle whitelisted channel
		if (!isWhitelistedChannel(channel)) {
			censor.censorMessage(message);
		}
	}

	private boolean isInvalidMessage(final Message message) {
		final User author = message.getAuthor();
		return message.isWebhookMessage() || author.isBot();
	}

	private boolean isUntrustedSender(final Message message) {
		final long authorId = message.getAuthor().getIdLong();
		final long guildId = message.getGuild().getIdLong();
		final Optional<DiscordMember> authorOpt = memberRepo.findByDiscordIdAndGuild_GuildId(authorId, guildId);
		return authorOpt.map(DiscordMember::isUntrusted).orElse(false);
	}

	private boolean isWhitelistedChannel(final TextChannel channel) {
		final long channelId = channel.getIdLong();
		return channelRepo.existsById(channelId);
	}

	private Command identifyCommand(final String cmdPrefix, final String rawMessage) {
		final String commandName = identifyCommandName(cmdPrefix, rawMessage);
		return commandMap.get(commandName);
	}

	private String identifyCommandName(final String cmdPrefix, final String messageContent) {
		final String[] tokens = messageContent.split(" ");
		final String fullCommand = tokens[0];
		final String commandName = fullCommand.replace(cmdPrefix, "");
		return commandName.toLowerCase();        // lower case is needed for the matching to work in any case! DO NOT remove it!
	}

	/**
	 * Executes a command and handles exception if the bot does not have the needed permissions to
	 * execute that command in the channel/guild.
	 *
	 * @param event   The GuildMessageReceivedEvent provided by JDA.
	 * @param command The command to execute.
	 */
	private void executeCommand(final GuildMessageReceivedEvent event, final Command command) {
		if (!isAuthorizedMember(event.getMember())) {
			return;
		}

		try {
			command.execute(event);
		} catch (InsufficientPermissionException e) {
			String message = "Bot does not have the needed permission " + e.getPermission() + " for that command.";
			event.getChannel().sendMessage(message).queue();
		}
	}

	private boolean isAuthorizedMember(final Member member) {
		if (member == null) {
			return false;
		}

		if (member.hasPermission(Permission.ADMINISTRATOR)) {
			return true;
		}

		return hasModRole(member);
	}

	private boolean hasModRole(final Member member) {
		final List<Role> memberRoles = member.getRoles();
		for (Role role : memberRoles) {
			if (roleRepo.existsById(role.getIdLong())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void onGuildMessageUpdate(@NotNull final GuildMessageUpdateEvent event) {
		// handle bot/webhook messages
		final Message message = event.getMessage();
		if (isInvalidMessage(message)) {
			return;
		}

		// handle whitelisted channel
		final TextChannel channel = event.getChannel();
		if (isWhitelistedChannel(channel)) {
			return;
		}

		// handle channel the bot can not talk in
		if (!channel.canTalk()) {
			return;
		}

		censor.censorMessage(message);
	}
}
