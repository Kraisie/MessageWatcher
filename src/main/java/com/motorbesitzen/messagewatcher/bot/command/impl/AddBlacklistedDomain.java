package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.BlacklistedDomain;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.util.DiscordMessageUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public class AddBlacklistedDomain extends CommandImpl {

	private final DiscordGuildRepo guildRepo;

	public AddBlacklistedDomain(final DiscordGuildRepo guildRepo) {
		this.guildRepo = guildRepo;
	}

	@Transactional
	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final long guildId = event.getGuild().getIdLong();
		final Optional<DiscordGuild> discordGuildOpt = guildRepo.findById(guildId);
		final DiscordGuild dcGuild = discordGuildOpt.orElseGet(() -> createDiscordGuild(guildId));
		final Message message = event.getMessage();
		final String content = message.getContentRaw();
		final List<String> domainNames = DiscordMessageUtil.getStringsInQuotationMarks(content);
		if (domainNames.size() == 0) {
			sendErrorMessage(event.getChannel(), "Please mention one or more domains in quotation marks to blacklist.");
			return;
		}

		saveMentionedDomains(dcGuild, domainNames);
		answer(event.getChannel(), "Blacklisted the mentioned domains.");
	}

	private DiscordGuild createDiscordGuild(final long guildId) {
		final DiscordGuild dcGuild = DiscordGuild.createDefault(guildId);
		guildRepo.save(dcGuild);
		return dcGuild;
	}

	private void saveMentionedDomains(final DiscordGuild dcGuild, final List<String> domainNames) {
		for (String domainName : domainNames) {
			final BlacklistedDomain domain = new BlacklistedDomain(domainName, dcGuild);
			dcGuild.addBlacklistedDomain(domain);
		}

		guildRepo.save(dcGuild);
	}
}
