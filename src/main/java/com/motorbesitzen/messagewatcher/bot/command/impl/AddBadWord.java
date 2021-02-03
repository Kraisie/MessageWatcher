package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.BadWord;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.util.DiscordMessageUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Service("addbw")
public class AddBadWord extends CommandImpl {

	private final DiscordGuildRepo guildRepo;

	@Autowired
	public AddBadWord(final DiscordGuildRepo guildRepo) {
		this.guildRepo = guildRepo;
	}

	@Transactional
	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final long guildId = event.getGuild().getIdLong();
		final Optional<DiscordGuild> discordGuildOpt = guildRepo.findById(guildId);
		final DiscordGuild dcGuild = discordGuildOpt.orElseGet(() -> createDiscordGuild(guildId));
		final TextChannel channel = event.getChannel();
		final Message message = event.getMessage();
		final String content = message.getContentRaw();
		final List<String> badWordProperties = DiscordMessageUtil.getStringsInQuotationMarks(content);
		if (badWordProperties.size() != 4) {
			sendErrorMessage(channel, "Please use the correct syntax of the command.");
			return;
		}

		if (badWordProperties.get(0).length() > 100 || badWordProperties.get(1).length() > 100) {
			sendErrorMessage(channel, "Words are limited to 100 characters. Please do not exceed that limit.");
			return;
		}

		try {
			Pattern.compile(badWordProperties.get(0));
		} catch (PatternSyntaxException e) {
			sendErrorMessage(channel, "Invalid regex! " +
					e.getMessage().split("\n")[0] + " of \"" + badWordProperties.get(0).replaceAll("\\*", "\\\\*") + "\".");
			return;
		}

		saveBadWord(dcGuild, badWordProperties);
		answer(channel, "Added the word to the database.");
	}

	private DiscordGuild createDiscordGuild(final long guildId) {
		final DiscordGuild dcGuild = DiscordGuild.createDefault(guildId);
		guildRepo.save(dcGuild);
		return dcGuild;
	}

	private void saveBadWord(final DiscordGuild dcGuild, final List<String> badWordProperties) {
		final String word = badWordProperties.get(0);
		final String replacement =
				badWordProperties.get(1).isBlank() ?
						"\\*\\*\\*" :
						badWordProperties.get(1);
		final boolean wildcard = badWordProperties.get(2).equalsIgnoreCase("true");
		final boolean punishable = badWordProperties.get(3).equalsIgnoreCase("true");

		final BadWord badWord = new BadWord(word, replacement, wildcard, punishable, dcGuild);
		dcGuild.addBadWord(badWord);
		guildRepo.save(dcGuild);
	}
}
