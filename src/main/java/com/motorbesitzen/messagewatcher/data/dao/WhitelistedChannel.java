package com.motorbesitzen.messagewatcher.data.dao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
public class WhitelistedChannel {

	@Id
	@Min(value = 10000000000000000L)
	private long channelId;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "guildId")
	private DiscordGuild guild;

	protected WhitelistedChannel() {
	}

	public WhitelistedChannel(@Min(value = 10000000000000000L) long channelId, @NotNull DiscordGuild guild) {
		this.channelId = channelId;
		this.guild = guild;
	}

	public long getChannelId() {
		return channelId;
	}

	public DiscordGuild getGuild() {
		return guild;
	}

	public void setGuild(DiscordGuild guild) {
		this.guild = guild;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WhitelistedChannel that = (WhitelistedChannel) o;

		if (channelId != that.channelId) return false;
		return guild.getGuildId() == that.guild.getGuildId();
	}

	@Override
	public int hashCode() {
		return (int) (channelId ^ (channelId >>> 32));
	}
}
