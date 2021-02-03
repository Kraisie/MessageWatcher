package com.motorbesitzen.messagewatcher.bot.command;

import com.motorbesitzen.messagewatcher.util.EnvironmentUtil;
import com.motorbesitzen.messagewatcher.util.LogUtil;
import com.motorbesitzen.messagewatcher.util.ParseUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.stereotype.Service;

import java.awt.*;

/**
 * Basic implementation of a Command. Has all needed methods to send messages, answer to commands and log (debug) actions.
 * All subclasses (Commands) can use these functions.
 */
@Service
public class CommandImpl implements Command {

	/**
	 * {@inheritDoc}
	 * Placeholder for subclass implementation, does not do anything as this class is not a command which the bot
	 * handles.
	 */
	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		// Perform tasks in subclasses, not here!
		LogUtil.logDebug("Tried to execute base command!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void answer(final TextChannel channel, final String message) {
		sendMessage(channel, message);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void answer(final TextChannel channel, final MessageEmbed message) {
		sendMessage(channel, message);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendMessage(final TextChannel channel, final String message) {
		if (channel.canTalk()) {
			channel.sendMessage(message).queue();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendMessage(final TextChannel channel, final MessageEmbed message) {
		if (channel.canTalk()) {
			channel.sendMessage(message).queue();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Message answerPlaceholder(final TextChannel channel, final String placeholderMessage) {
		if (!channel.canTalk()) {
			return null;
		}

		return channel.sendMessage(placeholderMessage).complete();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void editPlaceholder(final Message message, final String newMessage) {
		if (!message.getTextChannel().canTalk()) {
			return;
		}

		try {
			message.editMessage(newMessage).queue();
		} catch (IllegalStateException e) {
			sendErrorMessage(
					message.getTextChannel(),
					"Can not edit message (" + message.getId() + ") from another user!"
			);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void editPlaceholder(final TextChannel channel, final long messageId, final String newMessage) {
		if (!channel.canTalk()) {
			return;
		}

		Message message = channel.getHistory().getMessageById(messageId);
		if (message == null) {
			sendErrorMessage(
					channel, "Can not edit message!\nMessage ID " + messageId + " not found in " +
							channel.getAsMention() + ".\n New message: \"" + newMessage + "\""
			);
			return;
		}

		editPlaceholder(message, newMessage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendErrorMessage(final TextChannel channel, final String errorMessage) {
		answer(channel, errorMessage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Color getEmbedColor() {
		String envR = EnvironmentUtil.getEnvironmentVariableOrDefault("EMBED_COLOR_R", "222");
		String envG = EnvironmentUtil.getEnvironmentVariableOrDefault("EMBED_COLOR_G", "105");
		String envB = EnvironmentUtil.getEnvironmentVariableOrDefault("EMBED_COLOR_B", "12");

		int r = ParseUtil.safelyParseStringToInt(envR);
		int g = ParseUtil.safelyParseStringToInt(envG);
		int b = ParseUtil.safelyParseStringToInt(envB);

		// make sure r,g,b stay in rgb range of 0-255
		return new Color(
				Math.max(0, r % 256),
				Math.max(0, g % 256),
				Math.max(0, b % 256)
		);
	}
}
