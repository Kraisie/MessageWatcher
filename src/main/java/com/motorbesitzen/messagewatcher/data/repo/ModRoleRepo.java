package com.motorbesitzen.messagewatcher.data.repo;

import com.motorbesitzen.messagewatcher.data.dao.ModRole;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ModRoleRepo extends CrudRepository<ModRole, Long> {
	Optional<ModRole> findByRoleIdAndGuild_GuildId(final long roleId, final long guildId);
}
