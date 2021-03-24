package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.BlacklistedDomain;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.repo.BlacklistedDomainRepo;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.util.DiscordMessageUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("deldomain")
class DeleteBlacklistedDomain extends CommandImpl {

	private final DiscordGuildRepo guildRepo;
	private final BlacklistedDomainRepo domainRepo;

	@Autowired
	DeleteBlacklistedDomain(DiscordGuildRepo guildRepo, BlacklistedDomainRepo domainRepo) {
		this.guildRepo = guildRepo;
		this.domainRepo = domainRepo;
	}

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

	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final long guildId = event.getGuild().getIdLong();
		final Optional<DiscordGuild> discordGuildOpt = guildRepo.findById(guildId);
		discordGuildOpt.ifPresentOrElse(
				dcGuild -> deleteBlacklistedDomains(event, dcGuild),
				() -> sendErrorMessage(event.getChannel(), "Mentioned domains are not on the blacklist!")
		);
	}

	private void deleteBlacklistedDomains(final GuildMessageReceivedEvent event, final DiscordGuild dcGuild) {
		final Message message = event.getMessage();
		final String content = message.getContentRaw();
		final List<String> domainNames = DiscordMessageUtil.getStringsInQuotationMarks(content);
		if (domainNames.size() == 0) {
			sendErrorMessage(event.getChannel(), "Please mention one or more domains in quotation marks to remove from the blacklist.");
			return;
		}

		deleteMentionedDomains(dcGuild, domainNames);
		answer(event.getChannel(), "Removed the mentioned domains from the blacklist.");
	}

	private void deleteMentionedDomains(final DiscordGuild dcGuild, final List<String> domainNames) {
		for (String domainName : domainNames) {
			if (domainName.isBlank()) {
				continue;
			}

			final Optional<BlacklistedDomain> blacklistedDomainOpt = domainRepo.findByDomainAndGuild_GuildId(domainName, dcGuild.getGuildId());
			blacklistedDomainOpt.ifPresent(domainRepo::delete);
		}
	}
}
