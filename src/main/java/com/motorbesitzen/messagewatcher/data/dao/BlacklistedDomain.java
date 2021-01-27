package com.motorbesitzen.messagewatcher.data.dao;

import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
public class BlacklistedDomain {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long domainId;

	@NotNull
	@NotBlank
	@Length(max = 2000)
	private String domain;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "guildId")
	private DiscordGuild guild;

	protected BlacklistedDomain() {
	}

	public BlacklistedDomain(@NotNull @NotBlank @Length(max = 2000) String domain, @NotNull DiscordGuild guild) {
		this.domain = domain;
		this.guild = guild;
	}

	public long getDomainId() {
		return domainId;
	}

	public String getDomain() {
		return domain;
	}

	public DiscordGuild getGuild() {
		return guild;
	}
}
