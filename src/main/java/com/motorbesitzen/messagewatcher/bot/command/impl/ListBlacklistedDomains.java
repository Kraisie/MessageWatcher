package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.BlacklistedDomain;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.util.DiscordMessageUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("domains")
public class ListBlacklistedDomains extends CommandImpl {

	private static final int FIELDS_PER_EMBED = 25;
	private final DiscordGuildRepo guildRepo;

	@Autowired
	public ListBlacklistedDomains(DiscordGuildRepo guildRepo) {
		this.guildRepo = guildRepo;
	}

	@Transactional
	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final long guildId = event.getGuild().getIdLong();
		final Optional<DiscordGuild> discordGuildOpt = guildRepo.findById(guildId);
		final DiscordGuild dcGuild = discordGuildOpt.orElseGet(() -> createDiscordGuild(guildId));
		final List<BlacklistedDomain> blacklistedDomains = dcGuild.getBlacklistedDomainsAlphabeticallyOrdered();
		if (blacklistedDomains.size() == 0) {
			sendErrorMessage(event.getChannel(), "There are no domains on the blacklist.");
			return;
		}

		final int pages = (blacklistedDomains.size() / FIELDS_PER_EMBED) + 1;
		EmbedBuilder eb = buildEmbedPage(1, pages);
		for (int i = 0; i < blacklistedDomains.size(); i++) {
			if (i > 0 && i % 25 == 0) {
				answer(event.getChannel(), eb.build());
				eb = buildEmbedPage((i / FIELDS_PER_EMBED) + 1, pages);
			}

			final BlacklistedDomain bw = blacklistedDomains.get(i);
			eb.addField("", "**[" + (i + 1) + "]** " + bw.getDomain(), true);
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
						"Blacklisted domains" :
						"Blacklisted domains [" + page + "/" + totalPages + "]"
		).setDescription("A list of all blacklisted domains.");
		eb.setColor(DiscordMessageUtil.getEmbedColor());
		return eb;
	}
}
