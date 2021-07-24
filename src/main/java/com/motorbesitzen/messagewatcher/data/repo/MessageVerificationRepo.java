package com.motorbesitzen.messagewatcher.data.repo;

import com.motorbesitzen.messagewatcher.data.dao.MessageVerification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageVerificationRepo extends CrudRepository<MessageVerification, Long> {
	boolean existsBySender_DiscordIdAndGuild_GuildId(long senderId, long guildId);

	Optional<MessageVerification> findByVerifyMessageIdAndGuild_GuildId(long verifyMessageId, long guildId);
}