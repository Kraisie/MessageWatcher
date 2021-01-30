package com.motorbesitzen.messagewatcher.data.dao;

import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
public class DiscordMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotNull
	@Min(value = 10000000000000000L)
	private long discordId;

	@NotNull
	@ColumnDefault("0")
	private long messageCount;

	@NotNull
	@ColumnDefault("0")
	private long wordCensorCount;

	@NotNull
	@ColumnDefault("0")
	private long linkCensorCount;

	@NotNull
	@ColumnDefault("0")
	private long warningCount;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "guildId")
	private DiscordGuild guild;

	protected DiscordMember() {
	}

	private DiscordMember(final long discordId, final DiscordGuild guild) {
		this.discordId = discordId;
		this.guild = guild;
	}

	public static DiscordMember createDefault(final long discordId, final DiscordGuild guild) {
		return new DiscordMember(discordId, guild);
	}

	public void increaseMessageCount() {
		messageCount++;
	}

	public void increaseWordCensorCount() {
		wordCensorCount++;
	}

	public void increaseLinkCensorCount() {
		linkCensorCount++;
	}

	public void increaseWarningCount() {
		warningCount++;
	}

	public long getId() {
		return id;
	}

	public long getDiscordId() {
		return discordId;
	}

	public long getMessageCount() {
		return messageCount;
	}

	public long getWordCensorCount() {
		return wordCensorCount;
	}

	public long getLinkCensorCount() {
		return linkCensorCount;
	}

	public long getWarningCount() {
		return warningCount;
	}

	public double getCensorsPerMessage() {
		return (double) (wordCensorCount + linkCensorCount) / (double) messageCount;
	}

	public double getCensorRating() {
		return (double) (wordCensorCount + linkCensorCount) * getCensorsPerMessage();
	}

	public DiscordGuild getGuild() {
		return guild;
	}
}
