package com.motorbesitzen.messagewatcher.data.dao;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
public class BadWord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long wordId;

	@NotNull
	@NotBlank
	@Length(max = 100)
	private String word;

	@NotNull
	@NotBlank
	@Length(max = 100)
	@ColumnDefault("'\\*\\*\\*'")        // \*\*\* in Discord to prevent Discord italic/bold text formatting
	private String replacement;

	@NotNull
	@ColumnDefault("false")
	private boolean wildcard;

	@NotNull
	@ColumnDefault("false")
	private boolean punishable;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "guildId")
	private DiscordGuild guild;

	protected BadWord() {
	}

	public BadWord(@NotNull @NotBlank @Length(max = 100) String word, @NotNull @NotBlank @Length(max = 100) String replacement,
				   @NotNull boolean wildcard, @NotNull boolean punishable, @NotNull DiscordGuild guild) {
		this.word = word;
		this.replacement = replacement;
		this.wildcard = wildcard;
		this.punishable = punishable;
		this.guild = guild;
	}

	public long getWordId() {
		return wordId;
	}

	public String getWord() {
		return word;
	}

	public String getReplacement() {
		return replacement;
	}

	public boolean isWildcard() {
		return wildcard;
	}

	public boolean isPunishable() {
		return punishable;
	}

	public DiscordGuild getGuild() {
		return guild;
	}
}
