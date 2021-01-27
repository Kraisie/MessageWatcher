package com.motorbesitzen.messagewatcher.data.repo;

import com.motorbesitzen.messagewatcher.data.dao.BadWord;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BadWordRepo extends CrudRepository<BadWord, Long> {
	Optional<BadWord> findByWordAndGuild_GuildId(final String word, final long guildId);
}
