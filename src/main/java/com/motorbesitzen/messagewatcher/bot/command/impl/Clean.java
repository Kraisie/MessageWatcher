package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.WhitelistedChannel;
import com.motorbesitzen.messagewatcher.data.repo.WhitelistedChannelRepo;
import com.motorbesitzen.messagewatcher.util.LogUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("clean")
class Clean extends CommandImpl {

	private final WhitelistedChannelRepo channelRepo;

	@Autowired
	Clean(WhitelistedChannelRepo channelRepo) {
		this.channelRepo = channelRepo;
	}

	@Override
	public String getName() {
		return "clean";
	}

	@Override
	public String getUsage() {
		return getName() + " #channel";
	}

	@Override
	public String getDescription() {
		return "Cleans a channel by creating a copy of the channel and deleting the original channel. " +
				"Thus preventing famous message log plugins from logging deleted messages.";
	}

	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final TextChannel callerChannel = event.getChannel();
		final Message message = event.getMessage();
		final List<TextChannel> mentionedChannels = message.getMentionedChannels();
		if (mentionedChannels.size() == 0) {
			sendErrorMessage(callerChannel, "Please mention a channel to clean.");
			return;
		}

		final TextChannel mentionedChannel = mentionedChannels.get(0);
		mentionedChannel.createCopy()
				.setPosition(mentionedChannel.getPosition())
				.setNews(mentionedChannel.isNews())
				.queue(
						channelCopy -> {
							answer(channelCopy, "FIRST!!!");
							updateWhitelist(mentionedChannel, channelCopy);
							mentionedChannel.delete().queue(
									v -> {
										if (!mentionedChannel.equals(callerChannel)) {
											answer(callerChannel, "Cleaned #" + mentionedChannel.getName());
										}
									},
									throwable -> {
										answer(callerChannel, "Could not delete the mentioned channel!");
										LogUtil.logError(
												"Could not delete channel in \"" +
														mentionedChannel.getGuild().getName() + "\"!", throwable
										);
									}
							);
						},
						throwable -> {
							answer(callerChannel, "Could not create a copy of the mentioned channel!");
							LogUtil.logError(
									"Could not create a copy of " + mentionedChannel.getName() +
											" in \"" + mentionedChannel.getGuild().getName() + "\"!", throwable
							);
						}
				);
	}

	private void updateWhitelist(final TextChannel originalChannel, final TextChannel copyChannel) {
		final long originalChannelId = originalChannel.getIdLong();
		final long guildId = originalChannel.getGuild().getIdLong();
		final Optional<WhitelistedChannel> whitelistedChannelOpt = channelRepo.findByChannelIdAndGuild_GuildId(originalChannelId, guildId);
		if (whitelistedChannelOpt.isEmpty()) {
			return;
		}

		final WhitelistedChannel whitelistedChannel = whitelistedChannelOpt.get();
		whitelistedChannel.setChannelId(copyChannel.getIdLong());
		channelRepo.save(whitelistedChannel);
	}
}
