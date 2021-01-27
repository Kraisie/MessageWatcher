package com.motorbesitzen.messagewatcher.data.repo;

import com.motorbesitzen.messagewatcher.data.dao.BlacklistedDomain;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BlacklistedDomainRepo extends CrudRepository<BlacklistedDomain, Long> {
	Optional<BlacklistedDomain> findByDomainAndGuild_GuildId(final String domain, final long guildId);

	boolean existsByDomainIgnoreCaseAndGuild_GuildId(final String domain, final long guildId);

	void deleteByDomainIgnoreCaseAndGuild_GuildId(final String domain, final long guildId);
}
