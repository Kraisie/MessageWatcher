package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.bot.service.EnvSettings;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Command to send an announcement via the bot in a specific channel.
 */
@Service("announce")
class Announce extends CommandImpl {

	private final EnvSettings envSettings;

	@Autowired
	Announce(final EnvSettings envSettings) {
		this.envSettings = envSettings;
	}

	@Override
	public String getName() {
		return "announce";
	}

	@Override
	public String getUsage() {
		return getName() + " (everyone|here|@role) #channel \"message\"";
	}

	@Override
	public String getDescription() {
		return "Sends a message in the tagged channel pinging @everyone, @here or a tagged role. " +
				"Avoiding pings can be done by not using everyone, here or a role tag. Providing " +
				"multiple role tags will only ping the first one.";
	}

	/**
	 * Sends an announcement in a given channel. Can optionally tag roles, everyone or here.
	 *
	 * @param event The received command message event.
	 */
	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final Message message = event.getMessage();
		final TextChannel callerChannel = event.getChannel();
		final Optional<TextChannel> announceChannel = getMentionedChannel(message);
		if (announceChannel.isEmpty()) {
			sendErrorMessage(callerChannel, "Please mention a channel to send the announcement in.");
			return;
		}

		final String rawMessage = message.getContentRaw();
		final Optional<String> announceMessage = getAnnouncementMessage(rawMessage);
		if (announceMessage.isEmpty()) {
			sendErrorMessage(callerChannel, "Please provide a message in quotation marks (\"...\").");
			return;
		}

		final List<Role> mentionedRoles = message.getMentionedRoles();
		final String announcement = buildAnnouncement(mentionedRoles, rawMessage, announceMessage.get());
		sendMessage(announceChannel.get(), announcement);
		answer(callerChannel, "Done ✅");
	}

	/**
	 * Filters the mentioned channel from the raw command message. If there are multiple channels mentioned only the
	 * first one gets used.
	 *
	 * @param message The original Message object provided by JDA.
	 * @return {@code Optional<TextChannel>} which is empty if there is no channel mentioned. If there is a channel
	 * mentioned the {@code Optional<TextChannel>} contains the first mentioned channel.
	 */
	private Optional<TextChannel> getMentionedChannel(final Message message) {
		final List<TextChannel> mentionedChannels = message.getMentionedChannels();
		if (mentionedChannels.size() == 0) {
			return Optional.empty();
		}

		return Optional.ofNullable(mentionedChannels.get(0));
	}

	/**
	 * Filters the announcement message from the raw command message.
	 *
	 * @param rawMessage The raw command message without Discord displaying content differently.
	 * @return {@code Optional<String>} which is empty if there is no message given. If there is a message given the
	 * {@code Optional<String>} contains the announcement message.
	 */
	private Optional<String> getAnnouncementMessage(final String rawMessage) {
		if (!rawMessage.matches(".*\"(.|\\n)*\".*")) {
			return Optional.empty();
		}

		return Optional.of(rawMessage.substring(rawMessage.indexOf("\"") + 1, rawMessage.lastIndexOf("\"")));
	}

	/**
	 * Builds the announcement with a mention on top (if given) and the announcement message below.
	 *
	 * @param mentionedRoles  A List of mentioned roles in the command message. Only the first one gets used if there are
	 *                        multiple mentioned roles. List can be empty if @everyone, @here or no tag shall be used.
	 * @param rawMessage      The raw command message without Discord displaying content differently.
	 * @param announceMessage The announcement message defined in the command.
	 * @return The full announcement with mention and the message text.
	 */
	private String buildAnnouncement(final List<Role> mentionedRoles, final String rawMessage, final String announceMessage) {
		final String ping = getMention(mentionedRoles, rawMessage);
		return ping + announceMessage;
	}

	/**
	 * Defines the mention for the announcement. The mention can be a specific role, @everyone or @here. If none of these
	 * are given the mention will be an empty String. Each mention is followed by a newline ({@code '\n'}).
	 *
	 * @param mentionedRoles A List of mentioned roles in the command message. Only the first one gets used if there are
	 *                       multiple mentioned roles. List can be empty if @everyone, @here or no tag shall be used.
	 * @param rawMessage     The raw command message without Discord displaying content differently.
	 * @return The desired mention as a String.
	 */
	private String getMention(final List<Role> mentionedRoles, final String rawMessage) {
		if (mentionedRoles.size() >= 1) {
			final Role mentionedRole = mentionedRoles.get(0);
			return mentionedRole.getAsMention() + "\n";
		}

		final String prefix = envSettings.getCommandPrefix();
		if (rawMessage.toLowerCase().startsWith(prefix + "announce here")) {
			return "@here\n";
		}

		if (rawMessage.toLowerCase().startsWith(prefix + "announce everyone")) {
			return "@everyone\n";
		}

		return "";    // no ping at all
	}
}
