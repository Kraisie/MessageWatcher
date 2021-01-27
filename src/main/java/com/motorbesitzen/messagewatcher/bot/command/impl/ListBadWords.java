package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.BadWord;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public class ListBadWords extends CommandImpl {

	private final DiscordGuildRepo guildRepo;

	public ListBadWords(final DiscordGuildRepo guildRepo) {
		this.guildRepo = guildRepo;
	}

	private static final int FIELDS_PER_EMBED = 25;

	@Transactional
	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final long guildId = event.getGuild().getIdLong();
		final Optional<DiscordGuild> discordGuildOpt = guildRepo.findById(guildId);
		final DiscordGuild dcGuild = discordGuildOpt.orElseGet(() -> createDiscordGuild(guildId));
		final List<BadWord> badWords = dcGuild.getBadWordsAlphabeticallyOrderd();
		if (badWords.size() == 0) {
			sendErrorMessage(event.getChannel(), "There are no bad words set.");
			return;
		}

		final int pages = (badWords.size() / FIELDS_PER_EMBED) + 1;
		EmbedBuilder eb = buildEmbedPage(1, pages);
		for (int i = 0; i < badWords.size(); i++) {
			if (i > 0 && i % 25 == 0) {
				answer(event.getChannel(), eb.build());
				eb = buildEmbedPage((i / FIELDS_PER_EMBED) + 1, pages);
			}

			final BadWord bw = badWords.get(i);
			eb.addField("", "**\"" + bw.getWord() + "\"** ➝ \"" + bw.getReplacement() + "\" " + (bw.isWildcard() ? "(wildcard)" : ""), false);
		}

		answer(event.getChannel(), eb.build());
	}

	private DiscordGuild createDiscordGuild(final long guildId) {
		final DiscordGuild dcGuild = DiscordGuild.createDefault(guildId);
		guildRepo.save(dcGuild);
		return dcGuild;
	}

	private EmbedBuilder buildEmbedPage(final int page, final int totalPages) {
		final EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(
				page == 1 && totalPages == 1 ?
						"Blocked words" :
						"Blocked words [" + page + "/" + totalPages + "]"
		).setDescription("A list of all blocked words and their replacement.");
		eb.setColor(getEmbedColor());
		return eb;
	}
}
