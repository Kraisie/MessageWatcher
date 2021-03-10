package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.util.LogUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("clean")
public class Clean extends CommandImpl {

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
		mentionedChannel.createCopy().setPosition(mentionedChannel.getPosition()).queue(
				channelCopy -> {
					answer(channelCopy, "FIRST!!!");
					mentionedChannel.delete().queue(
							v -> LogUtil.logDebug("Cleaned channel " + mentionedChannel.getName()),
							throwable -> {
								answer(callerChannel, "Could not create a copy of the mentioned channel!");
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
}
