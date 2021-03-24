package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.Command;
import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.util.DiscordMessageUtil;
import com.motorbesitzen.messagewatcher.util.EnvironmentUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sends a help message with information about all available commands to the channel where the help was requested.
 */
@Service("help")
class Help extends CommandImpl {

	private static final int FIELDS_PER_EMBED = 25;
	private final Map<String, Command> commandMap;

	@Autowired
	Help(final Map<String, Command> commandMap) {
		this.commandMap = commandMap;
	}

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getDescription() {
		return "Shows a list of commands that can be used.";
	}

	/**
	 * Sends a help message.
	 *
	 * @param event The event provided by JDA that a guild message got received.
	 */
	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final TextChannel channel = event.getChannel();
		sendHelpMessage(channel);
	}

	/**
	 * Sends the help message in the channel where the help got requested.
	 *
	 * @param channel The channel in which the command got used.
	 */
	private void sendHelpMessage(final TextChannel channel) {
		final List<Command> commands = new ArrayList<>(commandMap.values());
		if (commands.size() == 0) {
			sendErrorMessage(channel, "No commands found!");
			return;
		}

		final int pages = (commands.size() / FIELDS_PER_EMBED) + 1;
		EmbedBuilder eb = buildEmbedPage(1, pages);
		for (int i = 0; i < commands.size(); i++) {
			if (i > 0 && i % 25 == 0) {
				answer(channel, eb.build());
				eb = buildEmbedPage((i / FIELDS_PER_EMBED) + 1, pages);
			}

			final Command command = commands.get(i);
			addHelpEntry(eb, command);
		}

		answer(channel, eb.build());
	}

	/**
	 * Creates a numerated page for help entries. Can have up to 25 command fields.
	 *
	 * @param page       The current page number.
	 * @param totalPages The total pages needed to display all commands
	 * @return An {@code EmbedBuilder} with page identification if needed.
	 */
	private EmbedBuilder buildEmbedPage(final int page, final int totalPages) {
		return new EmbedBuilder().setColor(
				DiscordMessageUtil.getEmbedColor()
		).setTitle(
				page == 1 && totalPages == 1 ?
						"Commands and their variations" :
						"Commands and their variations [" + page + "/" + totalPages + "]"
		).setDescription(
				"A list of all commands you can use and what they do. " +
						"Note that \"(a|b|c)\" means that a, b or c can be chosen."
		);
	}

	/**
	 * Adds an entry for a command.
	 *
	 * @param eb      The {@code EmbedBuilder} to which each commands help information gets.
	 * @param command The command to add to the help page.
	 */
	private void addHelpEntry(final EmbedBuilder eb, final Command command) {
		final String prefix = EnvironmentUtil.getEnvironmentVariableOrDefault("CMD_PREFIX", "");
		final String title = prefix + command.getUsage();
		eb.addField(title, command.getDescription(), false);
	}
}
