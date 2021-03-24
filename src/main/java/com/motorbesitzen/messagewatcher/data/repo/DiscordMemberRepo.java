package com.motorbesitzen.messagewatcher.data.repo;

import com.motorbesitzen.messagewatcher.data.dao.DiscordMember;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface DiscordMemberRepo extends CrudRepository<DiscordMember, Long> {
	Optional<DiscordMember> findByDiscordIdAndGuild_GuildId(final long memberId, final long guildId);

	@Query("select d " +
			"from DiscordMember d " +
			"where d.guild.guildId = ?1 and d.wordCensorCount > 0 " +
			"order by d.wordCensorCount desc, d.messageCount desc")
	List<DiscordMember> findAllByGuild_GuildIdOrderByWordCensorCountDesc(final long guildId, final Pageable pageable);

	@Query("select d " +
			"from DiscordMember d " +
			"where d.guild.guildId = ?1 and d.linkCensorCount > 0 " +
			"order by d.linkCensorCount desc, d.messageCount desc")
	List<DiscordMember> findAllByGuild_GuildIdOrderByLinkCensorCountDesc(final long guildId, final Pageable pageable);

	@Query("select d " +
			"from DiscordMember d " +
			"where d.guild.guildId = ?1 and d.warningCount > 0 " +
			"order by d.warningCount desc, d.messageCount desc")
	List<DiscordMember> findAllByGuild_GuildIdOrderByWarningCountDesc(final long guildId, final Pageable pageable);

	@Query("select d " +
			"from DiscordMember d " +
			"where d.guild.guildId = ?1 and d.messageCount > 25 " +
			"order by ((2 * CAST((d.wordCensorCount + d.linkCensorCount) as double) + 3 * CAST(d.warningCount as double)) / " +
			"(CAST(d.messageCount as double) / (CAST((2 * (d.warningCount + 1)) as double)))) desc, d.messageCount desc")
	List<DiscordMember> findAllByGuild_GuildIdOrderByRating(long guildId, Pageable pageable);

	@Query("select d " +
			"from DiscordMember d " +
			"where d.guild.guildId = ?1 and d.messageCount > 25 " +
			"order by ((cast(d.wordCensorCount as double) + cast(d.linkCensorCount as double)) / cast(d.messageCount as double)) desc, d.messageCount desc")
	List<DiscordMember> findAllByGuild_GuildIdOrderByCensorsPerMessage(long guildId, Pageable pageable);

	@Query("select d " +
			"from DiscordMember d " +
			"where d.guild.guildId = ?1 and d.messageCount > 25 " +
			"order by d.messageCount desc")
	List<DiscordMember> findAllByGuild_GuildIdOrderByMessageCount(long guildId, Pageable pageable);


}
