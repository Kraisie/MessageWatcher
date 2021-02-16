package com.motorbesitzen.messagewatcher.data.repo;

import com.motorbesitzen.messagewatcher.data.dao.BadWord;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BadWordRepo extends CrudRepository<BadWord, Long> {
	List<BadWord> findByWordAndGuild_GuildId(final String word, final long guildId);
}
