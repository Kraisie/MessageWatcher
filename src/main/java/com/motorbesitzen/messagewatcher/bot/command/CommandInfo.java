package com.motorbesitzen.messagewatcher.bot.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Contains information about all commands like the name, usage information, and needed permissions.
 * Case does not matter as {@link #getCommandInfoByName(String)} ignores the case.
 * A command that requires the write permission should also always need the read permission.
 */
public enum CommandInfo {
	ADD_BAD_WORD {
		@Override
		public String getName() {
			return "addbw";
		}

		@Override
		public String getUsage() {
			return getName() + " \"word\" \"replacement\" \"(true|false)\" \"(true|false)\"";
		}

		@Override
		public String getDescription() {
			return "Adds a bad word to the database. A blank (\"\") replacement " +
					"defaults to \"\\*\\*\\*\". The wildcard flag is also optional and " +
					"defaults to false if blank. The last option marks the word as punishable " +
					"so a user will get kicked if he uses it x times. Word supports (Java escaped) regex.";
		}
	}, ADD_BLACKLISTED_DOMAIN {
		@Override
		public String getName() {
			return "blacklistdomain";
		}

		@Override
		public String getUsage() {
			return getName() + " \"domain\"+";
		}

		@Override
		public String getDescription() {
			return "Adds a domain to the blacklist. Any domain on the blacklist will get censored. Can be used with " +
					"multiple domains.";
		}
	}, ADD_MOD_ROLE {
		@Override
		public String getName() {
			return "addmod";
		}

		@Override
		public String getUsage() {
			return getName() + " @role+";
		}

		@Override
		public String getDescription() {
			return "Adds a role that can use the bot commands even without administrator permissions. Can be used with " +
					"multiple roles.";
		}
	}, ADD_WHITELISTED_CHANNEL {
		@Override
		public String getName() {
			return "whitelist";
		}

		@Override
		public String getUsage() {
			return getName() + " #channel+";
		}

		@Override
		public String getDescription() {
			return "Whitelists a channel so the bot does not censor any messages in there. Can be used with " +
					"multiple channels.";
		}
	}, CENSOR_STATS {
		@Override
		public String getName() {
			return "stats";
		}

		@Override
		public String getUsage() {
			return getName() + " (@user|discordid)";
		}

		@Override
		public String getDescription() {
			return "Displays the censor stats of the given user.";
		}
	}, CENSOR_SETTINGS {
		@Override
		public String getName() {
			return "censorsettings";
		}

		@Override
		public String getUsage() {
			return getName() + " punishableCensorsToKick";
		}

		@Override
		public String getDescription() {
			return "Sets the amount of censors of punishable words that triggers a kick of a user.";
		}
	}, DEL_BAD_WORD {
		@Override
		public String getName() {
			return "delbw";
		}

		@Override
		public String getUsage() {
			return getName() + " \"word\"+";
		}

		@Override
		public String getDescription() {
			return "Deletes a bad word from the database. Can be used with multiple words.";
		}
	}, DEL_BLACKLISTED_DOMAIN {
		@Override
		public String getName() {
			return "deldomain";
		}

		@Override
		public String getUsage() {
			return getName() + " \"domain\"+";
		}

		@Override
		public String getDescription() {
			return "Deletes a domain from the blacklist. Can be used with multiple domains.";
		}
	}, DEL_MOD_ROLE {
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
	}, DEL_WHITELISTED_CHANNEL {
		@Override
		public String getName() {
			return "delwhitelist";
		}

		@Override
		public String getUsage() {
			return getName() + " #channel+";
		}

		@Override
		public String getDescription() {
			return "Removes the whitelist status of a channel. Can be used with multiple roles.";
		}
	}, HELP {
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
	}, INFO {
		@Override
		public String getName() {
			return "info";
		}

		@Override
		public String getUsage() {
			return getName();
		}

		@Override
		public String getDescription() {
			return "Lists all whitelisted channels and all mod roles.";
		}
	}, LIST_BAD_WORDS {
		@Override
		public String getName() {
			return "bw";
		}

		@Override
		public String getUsage() {
			return getName();
		}

		@Override
		public String getDescription() {
			return "Lists all bad words for this guild.";
		}
	}, LIST_BLACKLISTED_DOMAINS {
		@Override
		public String getName() {
			return "domains";
		}

		@Override
		public String getUsage() {
			return getName();
		}

		@Override
		public String getDescription() {
			return "Lists all blacklisted domains for this guild.";
		}
	}, REPORT_SETTINGS {
		@Override
		public String getName() {
			return "reportsettings";
		}

		@Override
		public String getUsage() {
			return getName() + "emote threshold #channel";
		}

		@Override
		public String getDescription() {
			return "Sets the report emote to the used emote, the report threshold to the given value and the " +
					"report channel to the mentioned channel. Not setting a value will reset the value.";
		}
	}, TOP_LIST {
		@Override
		public String getName() {
			return "top";
		}

		@Override
		public String getUsage() {
			return getName() + " \"(word|link|rating)\" (1-25)";
		}

		@Override
		public String getDescription() {
			return "Displays the users with the highest censor count in one of the categories. Can display up to 25 users. " +
					"Defaults to a list of 10 users if no valid range is set and is ordered by censored words if no category " +
					"is set.";
		}
	}, UNKNOWN_COMMAND {
		@Override
		public String getName() {
			return "unknown";
		}

		@Override
		public String getUsage() {
			return "Can not be used.";
		}

		@Override
		public String getDescription() {
			return "Represents an unknown command with no functionality.";
		}
	};

	/**
	 * Compares given String and command name to find a matching command while ignoring case.
	 *
	 * @param name The lower case name of the command to find.
	 * @return The {@code CommandInfo} of a command with a fitting name. If there is no match it returns
	 * {@link #UNKNOWN_COMMAND}.
	 */
	public static CommandInfo getCommandInfoByName(final String name) {
		final CommandInfo[] commandInfos = values();
		for (CommandInfo commandInfo : commandInfos) {
			if (commandInfo.getName().equalsIgnoreCase(name)) {
				return commandInfo;
			}
		}
		return UNKNOWN_COMMAND;
	}

	/**
	 * Provides a {@code List<CommandInfo>} of all available commands in alphabetical order. The {@link #UNKNOWN_COMMAND}
	 * will not be present in the returned list.
	 *
	 * @return An alphabetically sorted list of all commands.
	 */
	public static List<CommandInfo> getAllCommandInfos() {
		final CommandInfo[] commandInfos = values();
		List<CommandInfo> allCommandInfos = new ArrayList<>();
		for (CommandInfo commandInfo : commandInfos) {
			if (commandInfo != UNKNOWN_COMMAND) {
				allCommandInfos.add(commandInfo);
			}
		}
		allCommandInfos.sort(Comparator.comparing(CommandInfo::getName));
		return allCommandInfos;
	}

	/**
	 * Get the name of the command. Make sure to use lower case on new commands!
	 *
	 * @return the lower case name of the command.
	 */
	public abstract String getName();

	/**
	 * Get a sample on how to use the command. Can be used in help commands.
	 *
	 * @return an example usage with all variables listed.
	 */
	public abstract String getUsage();

	/**
	 * Get a description about the command. Can be used in help commands.
	 *
	 * @return a short information text about what the command does.
	 */
	public abstract String getDescription();
}
