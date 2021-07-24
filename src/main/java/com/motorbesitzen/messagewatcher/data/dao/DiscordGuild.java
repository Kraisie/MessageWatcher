package com.motorbesitzen.messagewatcher.data.dao;

import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.*;

@Table(name = "DiscordGuild")
@Entity
public class DiscordGuild {

	@Id
	private long guildId;

	@Min(0)
	@ColumnDefault("0")
	private long reportEmoteId;

	@Min(0)
	@ColumnDefault("5")
	private long reportCountThreshold;

	@Min(0)
	@ColumnDefault("0")
	private long reportChannelId;

	@Min(0)
	@ColumnDefault("0")
	private long verifyChannelId;

	@Min(0)
	@ColumnDefault("5")
	private long censorKickThreshold;

	@Min(0)
	@ColumnDefault("15")
	private long censorBanThreshold;

	@ColumnDefault("false")
	private boolean censorInvites;

	@OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
	private Set<BadWord> badWords;

	@OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
	private Set<BlacklistedDomain> blacklistedDomains;

	@OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
	private Set<DiscordMember> members;

	@OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
	private Set<ModRole> modRoles;

	@OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
	private Set<WhitelistedChannel> whitelistedChannels;

	@OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
	private Set<MessageVerification> messageVerifications;

	protected DiscordGuild() {
	}

	private DiscordGuild(final long guildId) {
		this.guildId = guildId;
		this.badWords = new HashSet<>();
		this.blacklistedDomains = new HashSet<>();
		this.members = new HashSet<>();
		this.modRoles = new HashSet<>();
		this.whitelistedChannels = new HashSet<>();
	}

	public static DiscordGuild createDefault(final long guildId) {
		return new DiscordGuild(guildId);
	}

	public long getGuildId() {
		return guildId;
	}

	public void setGuildId(long guildId) {
		this.guildId = guildId;
	}

	public long getReportEmoteId() {
		return reportEmoteId;
	}

	public void setReportEmoteId(long reportEmoteId) {
		this.reportEmoteId = reportEmoteId;
	}

	public long getReportCountThreshold() {
		return reportCountThreshold;
	}

	public void setReportCountThreshold(long reportCountThreshold) {
		this.reportCountThreshold = reportCountThreshold;
	}

	public long getReportChannelId() {
		return reportChannelId;
	}

	public void setReportChannelId(long reportChannelId) {
		this.reportChannelId = reportChannelId;
	}

	public long getVerifyChannelId() {
		return verifyChannelId;
	}

	public void setVerifyChannelId(long verifyChannelId) {
		this.verifyChannelId = verifyChannelId;
	}

	public long getCensorKickThreshold() {
		return censorKickThreshold;
	}

	public void setCensorKickThreshold(long censorKickThreshold) {
		this.censorKickThreshold = censorKickThreshold;
	}

	public long getCensorBanThreshold() {
		return censorBanThreshold;
	}

	public void setCensorBanThreshold(long censorBanThreshold) {
		this.censorBanThreshold = censorBanThreshold;
	}

	public boolean shouldCensorInvites() {
		return censorInvites;
	}

	public void setCensorInvites(boolean censorInvites) {
		this.censorInvites = censorInvites;
	}

	public Set<BadWord> getBadWords() {
		return badWords;
	}

	public void setBadWords(Set<BadWord> badWords) {
		this.badWords = badWords;
	}

	public Set<BlacklistedDomain> getBlacklistedDomains() {
		return blacklistedDomains;
	}

	public void setBlacklistedDomains(Set<BlacklistedDomain> blacklistedDomains) {
		this.blacklistedDomains = blacklistedDomains;
	}

	public Set<DiscordMember> getMembers() {
		return members;
	}

	public void setMembers(Set<DiscordMember> members) {
		this.members = members;
	}

	public Set<ModRole> getModRoles() {
		return modRoles;
	}

	public void setModRoles(Set<ModRole> modRoles) {
		this.modRoles = modRoles;
	}

	public Set<WhitelistedChannel> getWhitelistedChannels() {
		return whitelistedChannels;
	}

	public void setWhitelistedChannels(Set<WhitelistedChannel> whitelistedChannels) {
		this.whitelistedChannels = whitelistedChannels;
	}

	public Set<MessageVerification> getMessageVerifications() {
		return messageVerifications;
	}

	public void setMessageVerifications(Set<MessageVerification> messageVerifications) {
		this.messageVerifications = messageVerifications;
	}

	public List<BadWord> getBadWordsAlphabeticallyOrderd() {
		List<BadWord> badWordList = new ArrayList<>(getBadWords());
		badWordList.sort(Comparator.comparing(BadWord::getWord));
		return badWordList;
	}

	public List<BadWord> getBadWordsOrderdByWordLengthDesc() {
		List<BadWord> badWordList = new ArrayList<>(getBadWords());
		badWordList.sort(Comparator.comparingInt(e -> e.getWord().length()));
		Collections.reverse(badWordList);
		return badWordList;
	}

	public List<BlacklistedDomain> getBlacklistedDomainsAlphabeticallyOrdered() {
		List<BlacklistedDomain> blacklistedDomains = new ArrayList<>(getBlacklistedDomains());
		blacklistedDomains.sort(Comparator.comparing(BlacklistedDomain::getDomain));
		return blacklistedDomains;
	}

	public void addModRole(final ModRole role) {
		modRoles.add(role);
	}

	public void addWhitelistedChannel(final WhitelistedChannel channel) {
		whitelistedChannels.add(channel);
	}

	public void addBlacklistedDomain(final BlacklistedDomain domain) {
		blacklistedDomains.add(domain);
	}

	public void addBadWord(final BadWord badWord) {
		badWords.add(badWord);
	}


}