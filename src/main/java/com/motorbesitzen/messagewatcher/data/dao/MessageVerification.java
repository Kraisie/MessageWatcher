package com.motorbesitzen.messagewatcher.data.dao;

import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Table(name = "MessageVerification")
@Entity
public class MessageVerification {

	// DB: guild <-> verification : id, sender, originalChannelId, originalMsgId, verifyMsgId, content

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotNull
	@OneToOne
	private DiscordMember sender;

	@NotNull
	private long originalChannelId;

	@NotNull
	private long originalMessageId;

	@NotNull
	private long verifyMessageId;

	@NotBlank
	@Length(max = 4000)
	private String messageContent;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "guildId")
	private DiscordGuild guild;

	public MessageVerification() {
	}

	public MessageVerification(DiscordMember sender, String messageContent, DiscordGuild guild) {
		this.sender = sender;
		this.messageContent = messageContent;
		this.guild = guild;
	}

	public MessageVerification(DiscordMember sender, long originalChannelId, long originalMessageId, long verifyMessageId, String messageContent, DiscordGuild guild) {
		this.sender = sender;
		this.originalChannelId = originalChannelId;
		this.originalMessageId = originalMessageId;
		this.verifyMessageId = verifyMessageId;
		this.messageContent = messageContent;
		this.guild = guild;
	}

	public DiscordMember getSender() {
		return sender;
	}

	public long getOriginalChannelId() {
		return originalChannelId;
	}

	public void setOriginalChannelId(long originalChannelId) {
		this.originalChannelId = originalChannelId;
	}

	public long getOriginalMessageId() {
		return originalMessageId;
	}

	public void setOriginalMessageId(long originalMessageId) {
		this.originalMessageId = originalMessageId;
	}

	public long getVerifyMessageId() {
		return verifyMessageId;
	}

	public void setVerifyMessageId(long verifyMessageId) {
		this.verifyMessageId = verifyMessageId;
	}

	public String getMessageContent() {
		return messageContent;
	}

	public DiscordGuild getGuild() {
		return guild;
	}
}
