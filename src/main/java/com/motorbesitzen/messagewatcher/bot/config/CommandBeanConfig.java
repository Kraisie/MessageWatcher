package com.motorbesitzen.messagewatcher.bot.config;

import com.motorbesitzen.messagewatcher.bot.command.impl.*;
import com.motorbesitzen.messagewatcher.data.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class defines the bot commands as beans so Spring can control them and generate them when needed.
 * The method name like "help()" will result in the bean name "help" so make sure to name the method
 * as the lower case version of the command. A command like "?AuthoRize #channel" would need "authorize()"
 * as a method. Do not use camel case here, even though it is against the Java practice.
 * If the command needs access to a repository make sure to let Spring autowire it as parameters. This also
 * works for any other Spring controlled class.
 */
@Configuration
public class CommandBeanConfig {

	@Bean
	@Autowired
	AddBadWord addbw(final DiscordGuildRepo guildRepo) {
		return new AddBadWord(guildRepo);
	}

	@Bean
	@Autowired
	AddBlacklistedDomain blacklistdomain(final DiscordGuildRepo guildRepo) {
		return new AddBlacklistedDomain(guildRepo);
	}

	@Bean
	@Autowired
	AddModRole addmod(final DiscordGuildRepo guildRepo) {
		return new AddModRole(guildRepo);
	}

	@Bean
	@Autowired
	AddWhitelistedChannel whitelist(final DiscordGuildRepo guildRepo) {
		return new AddWhitelistedChannel(guildRepo);
	}

	@Bean
	@Autowired
	DeleteBadWord delbw(final DiscordGuildRepo guildRepo, final BadWordRepo badWordRepo) {
		return new DeleteBadWord(guildRepo, badWordRepo);
	}

	@Bean
	@Autowired
	DeleteBlacklistedDomain deldomain(final DiscordGuildRepo guildRepo, final BlacklistedDomainRepo domainRepo) {
		return new DeleteBlacklistedDomain(guildRepo, domainRepo);
	}

	@Bean
	@Autowired
	DeleteModRole delmod(final DiscordGuildRepo guildRepo, final ModRoleRepo roleRepo) {
		return new DeleteModRole(guildRepo, roleRepo);
	}

	@Bean
	@Autowired
	DeleteWhitelistedChannel delwhitelist(final DiscordGuildRepo guildRepo, final WhitelistedChannelRepo channelRepo) {
		return new DeleteWhitelistedChannel(guildRepo, channelRepo);
	}

	@Bean
	Help help() {
		return new Help();
	}

	@Bean
	@Autowired
	Info info(final DiscordGuildRepo guildRepo) {
		return new Info(guildRepo);
	}

	@Bean
	@Autowired
	ListBadWords bw(final DiscordGuildRepo guildRepo) {
		return new ListBadWords(guildRepo);
	}

	@Bean
	@Autowired
	ListBlacklistedDomains domains(final DiscordGuildRepo guildRepo) {
		return new ListBlacklistedDomains(guildRepo);
	}

	@Bean
	@Autowired
	TopList top(final DiscordMemberRepo memberRepo) {
		return new TopList(memberRepo);
	}

	@Bean
	@Autowired
	ReportSettings reportsettings(final DiscordGuildRepo guildRepo) {
		return new ReportSettings(guildRepo);
	}

	@Bean
	@Autowired
	CensorStats stats(final DiscordMemberRepo memberRepo) {
		return new CensorStats(memberRepo);
	}
}

