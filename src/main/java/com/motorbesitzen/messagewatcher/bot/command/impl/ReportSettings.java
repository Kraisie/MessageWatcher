package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.util.DiscordMessageUtil;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("reportsettings")
class ReportSettings extends CommandImpl {

	private final DiscordGuildRepo guildRepo;

	@Autowired
	ReportSettings(final DiscordGuildRepo guildRepo) {
		this.guildRepo = guildRepo;
	}

	@Override
	public String getName() {
		return "reportsettings";
	}

	@Override
	public String getUsage() {
		return getName() + " emote threshold #channel";
	}

	@Override
	public String getDescription() {
		return "Sets the report emote to the used emote, the report threshold to the given value and the " +
				"report channel to the mentioned channel. Not setting a value will reset the value.";
	}

	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final long guildId = event.getGuild().getIdLong();
		final Optional<DiscordGuild> discordGuildOpt = guildRepo.findById(guildId);
		final DiscordGuild dcGuild = discordGuildOpt.orElseGet(() -> createDiscordGuild(guildId));
		final Message message = event.getMessage();
		final long emoteId = getEmoteId(message);
		final List<TextChannel> mentionedChannels = message.getMentionedChannels();
		final long channelId = mentionedChannels.size() != 0 ? mentionedChannels.get(0).getIdLong() : 0L;
		final long threshold = Math.max(0, DiscordMessageUtil.getMentionedRawId(message));
		saveSettings(dcGuild, emoteId, channelId, threshold);
		answer(event.getChannel(), "Updated report settings.");
	}

	private DiscordGuild createDiscordGuild(final long guildId) {
		final DiscordGuild dcGuild = DiscordGuild.createDefault(guildId);
		guildRepo.save(dcGuild);
		return dcGuild;
	}

	private long getEmoteId(final Message message) {
		final List<Emote> mentionedEmotes = message.getEmotes();
		if (mentionedEmotes.size() == 0) {
			return 0L;
		}

		final Guild emoteGuild = mentionedEmotes.get(0).getGuild();
		if (emoteGuild == null) {
			sendErrorMessage(message.getTextChannel(), "Only emotes from your own guild are allowed!");
			return 0L;
		}

		final Guild callerGuild = message.getGuild();
		if (emoteGuild.getIdLong() != callerGuild.getIdLong()) {
			sendErrorMessage(message.getTextChannel(), "Only emotes from your own guild are allowed!");
			return 0L;
		}

		final Emote reportEmote = mentionedEmotes.get(0);
		if (!reportEmote.isAvailable()) {
			sendErrorMessage(message.getTextChannel(), "Emote is not available!");
			return 0L;
		}

		return reportEmote.getIdLong();
	}

	private void saveSettings(final DiscordGuild dcGuild, final long emoteId, final long channelId, final long threshold) {
		dcGuild.setReportEmoteId(emoteId);
		dcGuild.setReportChannelId(channelId);
		dcGuild.setReportCountThreshold(threshold);
		guildRepo.save(dcGuild);
	}
}
