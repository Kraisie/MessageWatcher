package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.BadWord;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.repo.BadWordRepo;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.util.DiscordMessageUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("delbw")
class DeleteBadWord extends CommandImpl {

	private final DiscordGuildRepo guildRepo;
	private final BadWordRepo badWordRepo;

	@Autowired
	DeleteBadWord(DiscordGuildRepo guildRepo, BadWordRepo badWordRepo) {
		this.guildRepo = guildRepo;
		this.badWordRepo = badWordRepo;
	}

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

	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final long guildId = event.getGuild().getIdLong();
		final Optional<DiscordGuild> discordGuildOpt = guildRepo.findById(guildId);
		discordGuildOpt.ifPresentOrElse(
				dcGuild -> deleteBadWords(event, dcGuild),
				() -> sendErrorMessage(event.getChannel(), "Mentioned words are not present in the database!")
		);
	}

	private void deleteBadWords(final GuildMessageReceivedEvent event, final DiscordGuild dcGuild) {
		final Message message = event.getMessage();
		final String content = message.getContentRaw();
		final List<String> badWords = DiscordMessageUtil.getStringsInQuotationMarks(content);
		if (badWords.size() == 0) {
			sendErrorMessage(event.getChannel(), "Please mention one or more words in quotation marks to remove from the database.");
			return;
		}

		deleteMentionedWords(event.getChannel(), dcGuild, badWords);
	}

	private void deleteMentionedWords(final TextChannel channel, final DiscordGuild dcGuild, final List<String> badWords) {
		for (String badWord : badWords) {
			if (badWord.isBlank()) {
				continue;
			}

			final List<BadWord> badWordMatches = badWordRepo.findByWordAndGuild_GuildId(badWord, dcGuild.getGuildId());
			if (badWordMatches.size() == 0) {
				sendErrorMessage(channel, "Word does not exist in database!");
				return;
			}

			for (BadWord badWordMatch : badWordMatches) {
				badWordRepo.delete(badWordMatch);
			}
		}

		answer(channel, "Removed the mentioned words from the database.");
	}


}
