package com.motorbesitzen.messagewatcher.bot.config;

import com.motorbesitzen.messagewatcher.bot.command.Command;
import com.motorbesitzen.messagewatcher.bot.event.DeletionListener;
import com.motorbesitzen.messagewatcher.bot.event.GuildJoinListener;
import com.motorbesitzen.messagewatcher.bot.event.GuildMessageListener;
import com.motorbesitzen.messagewatcher.bot.event.ReactionListener;
import com.motorbesitzen.messagewatcher.bot.service.Censor;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.data.repo.ModRoleRepo;
import com.motorbesitzen.messagewatcher.data.repo.WhitelistedChannelRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ListenerBeanConfig {

	@Bean
	@Autowired
	DeletionListener deletionListener(final DiscordGuildRepo guildRepo, final ModRoleRepo roleRepo, final WhitelistedChannelRepo channelRepo) {
		return new DeletionListener(guildRepo, roleRepo, channelRepo);
	}

	@Bean
	@Autowired
	GuildJoinListener guildJoinListener(final DiscordGuildRepo guildRepo) {
		return new GuildJoinListener(guildRepo);
	}

	@Bean
	@Autowired
	GuildMessageListener guildMessageListener(final Censor censor, final Map<String, Command> commandMap,
											  final WhitelistedChannelRepo channelRepo, final ModRoleRepo roleRepo) {
		return new GuildMessageListener(censor, commandMap, channelRepo, roleRepo);
	}

	@Bean
	@Autowired
	ReactionListener reactionListener(final DiscordGuildRepo guildRepo) {
		return new ReactionListener(guildRepo);
	}

}
