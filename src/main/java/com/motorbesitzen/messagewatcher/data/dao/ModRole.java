package com.motorbesitzen.messagewatcher.data.dao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
public class ModRole {

	@Id
	@Min(value = 10000000000000000L)
	private long roleId;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "guildId")
	private DiscordGuild guild;

	protected ModRole() {
	}

	public ModRole(@Min(value = 10000000000000000L) long roleId, @NotNull DiscordGuild guild) {
		this.roleId = roleId;
		this.guild = guild;
	}

	public long getRoleId() {
		return roleId;
	}

	public DiscordGuild getGuild() {
		return guild;
	}

	public void setGuild(final DiscordGuild guild) {
		this.guild = guild;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ModRole modRole = (ModRole) o;

		if (roleId != modRole.roleId) return false;
		return guild.getGuildId() == modRole.guild.getGuildId();
	}

	@Override
	public int hashCode() {
		return (int) (roleId ^ (roleId >>> 32));
	}
}
