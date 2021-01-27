package com.motorbesitzen.messagewatcher.data.repo;

import com.motorbesitzen.messagewatcher.data.dao.WhitelistedChannel;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface WhitelistedChannelRepo extends CrudRepository<WhitelistedChannel, Long> {
	Optional<WhitelistedChannel> findByChannelIdAndGuild_GuildId(final long channelId, final long guildId);
}
