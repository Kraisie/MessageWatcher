package com.motorbesitzen.messagewatcher.util;

import net.dv8tion.jda.api.entities.Message;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper functions for Discord messages.
 */
public final class DiscordMessageUtil {

	/**
	 * Get a mentioned member ID from a message. If the message has a mention it uses the ID of the first mentioned member.
	 * If there is no mention it checks for a numeric ID token and if there are multiple chooses the first one.
	 *
	 * @param message The Discord message object.
	 * @return If there is a member ID found it returns the ID as a {@code long}. If a raw ID exceeds the {@code Long}
	 * limits it returns -1 as well if there is no ID found in the message.
	 */
	public static long getMentionedMemberId(final Message message) {
		if (message.getMentionedMembers().size() != 0) {
			return message.getMentionedMembers().get(0).getIdLong();
		}

		return getMentionedRawId(message);
	}

	/**
	 * Searches the message for a 'raw' numeric ID.
	 *
	 * @param message The Discord message object.
	 * @return If there is a ID found it returns the ID as a {@code long}, if not or if it exceeds the {@code Long}
	 * limits it returns -1.
	 */
	public static long getMentionedRawId(final Message message) {
		final String rawMessage = message.getContentRaw();
		final String[] tokens = rawMessage.split(" ");
		for (String token : tokens) {
			if (token.matches("[0-9]+")) {
				return ParseUtil.safelyParseStringToLong(token);
			}
		}

		return -1;
	}

	public static List<String> getStringsInQuotationMarks(final String content) {
		final Pattern pattern = Pattern.compile("\"[^\"]*\"");
		final Matcher matcher = pattern.matcher(content);
		final List<String> quoted = new ArrayList<>();
		while (matcher.find()) {
			final String quote = matcher.group().replaceAll("\"", "");
			quoted.add(quote);
		}

		return quoted;
	}

	/**
	 * Defines the color used for embeds in Discord (color bar on the left). If no color is set in the environment
	 * variables it returns orange ({@code #de690c} / {@code rgb(222,105,12)}).
	 *
	 * @return The color to use for embeds.
	 */
	public static Color getEmbedColor() {
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
