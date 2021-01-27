package com.motorbesitzen.messagewatcher.data.repo;

import com.motorbesitzen.messagewatcher.data.dao.DiscordMember;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface DiscordMemberRepo extends CrudRepository<DiscordMember, Long> {
	Optional<DiscordMember> findByDiscordIdAndGuild_GuildId(final long memberId, final long guildId);

	List<DiscordMember> findAllByGuild_GuildIdOrderByWordCensorCountDesc(final long guildId, final Pageable pageable);

	List<DiscordMember> findAllByGuild_GuildIdOrderByLinkCensorCountDesc(final long guildId, final Pageable pageable);

	@Query("select d " +
			"from DiscordMember d " +
			"where d.guild.guildId = ?1 " +
			"order by ((d.wordCensorCount + d.linkCensorCount) * ((d.wordCensorCount + d.linkCensorCount) / d.messageCount)) desc")
	List<DiscordMember> findAllByGuild_GuildIdOrderByRating(long guildId, Pageable pageable);

}
