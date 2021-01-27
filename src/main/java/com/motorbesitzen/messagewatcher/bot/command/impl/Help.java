package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.bot.command.CommandInfo;
import com.motorbesitzen.messagewatcher.util.EnvironmentUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

/**
 * Sends a help message with information about all available commands to the channel where the help was requested.
 */
public class Help extends CommandImpl {

	/**
	 * Sends a help message.
	 *
	 * @param event The event provided by JDA that a guild message got received.
	 */
	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final TextChannel channel = event.getChannel();
		final List<CommandInfo> commands = CommandInfo.getAllCommandInfos();
		sendHelpMessage(channel, commands);
	}

	/**
	 * Sends the help message in the channel where the help got requested.
	 *
	 * @param channel  The channel in which the command got used.
	 * @param commands The list of commands the bot can execute.
	 */
	private void sendHelpMessage(final TextChannel channel, final List<CommandInfo> commands) {
		final EmbedBuilder eb = buildHelpMessage(commands);
		final MessageEmbed embed = eb.build();
		answer(channel, embed);
	}

	/**
	 * Builds the help message.
	 *
	 * @param commands The list of commands the bot can execute.
	 * @return An {@code EmbedBuilder} that contains the help information.
	 */
	private EmbedBuilder buildHelpMessage(final List<CommandInfo> commands) {
		final EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(getEmbedColor());
		eb.setTitle("Commands and their variations")
				.setDescription("A list of all commands the bot offers and what they do. " +
						"Note that \"(a|b|c)\" means that a, b or c can be chosen.");

		addHelpEntries(commands, eb);
		return eb;
	}

	/**
	 * Adds an entry for each command.
	 *
	 * @param commands The list of commands the bot can execute.
	 * @param eb       The {@code EmbedBuilder} to which each commands help information gets.
	 */
	private void addHelpEntries(final List<CommandInfo> commands, final EmbedBuilder eb) {
		final String prefix = EnvironmentUtil.getEnvironmentVariableOrDefault("CMD_PREFIX", "");
		for (CommandInfo command : commands) {
			String title = prefix + command.getUsage();
			eb.addField(title, command.getDescription(), false);
		}
	}
}
