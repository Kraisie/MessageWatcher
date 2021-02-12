package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.dao.ModRole;
import com.motorbesitzen.messagewatcher.data.dao.WhitelistedChannel;
import com.motorbesitzen.messagewatcher.data.repo.DiscordGuildRepo;
import com.motorbesitzen.messagewatcher.util.DiscordMessageUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.Min;
import java.util.Optional;
import java.util.Set;

@Service("info")
public class Info extends CommandImpl {

	private final DiscordGuildRepo guildRepo;

	@Autowired
	public Info(final DiscordGuildRepo guildRepo) {
		this.guildRepo = guildRepo;
	}

	@Override
	@Transactional
	public void execute(GuildMessageReceivedEvent event) {
		final long guildId = event.getGuild().getIdLong();
		final Optional<DiscordGuild> discordGuildOpt = guildRepo.findById(guildId);
		final DiscordGuild dcGuild = discordGuildOpt.orElseGet(() -> createDiscordGuild(guildId));
		sendInfo(event, dcGuild);
	}

	private DiscordGuild createDiscordGuild(final long guildId) {
		final DiscordGuild dcGuild = DiscordGuild.createDefault(guildId);
		guildRepo.save(dcGuild);
		return dcGuild;
	}

	/**
	 * Gathers all needed data, creates the message and sends it.
	 *
	 * @param event   The event provided by JDA that a guild message got received.
	 * @param dcGuild The Discord guild object which contains the permissions.
	 */
	private void sendInfo(final GuildMessageReceivedEvent event, final DiscordGuild dcGuild) {
		final Set<ModRole> modRoles = dcGuild.getModRoles();
		final Set<WhitelistedChannel> whitelistedChannels = dcGuild.getWhitelistedChannels();

		final MessageEmbed embedInfo = buildEmbed(event, dcGuild, whitelistedChannels, modRoles);
		answer(event.getChannel(), embedInfo);
	}

	/**
	 * Builds the embedded message.
	 *
	 * @param event               The event provided by JDA that a guild message got received.
	 * @param dcGuild             The Discord guild object which contains the permissions.
	 * @param whitelistedChannels A list of all Discord channels the bot does not censor.
	 * @param modRoles            A list of all roles with mod status for the bot.
	 * @return The data as {@code MessageEmbed}.
	 */
	private MessageEmbed buildEmbed(final GuildMessageReceivedEvent event, final DiscordGuild dcGuild,
									final Set<WhitelistedChannel> whitelistedChannels, final Set<ModRole> modRoles) {
		final String channelContent = buildWhitelistedChannelList(whitelistedChannels);
		final String roleContent = buildModRoleList(modRoles);

		final EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Info for \"" + event.getGuild().getName() + "\":");
		eb.setColor(DiscordMessageUtil.getEmbedColor());
		eb.addField("Censors of punishable words to kick:", String.valueOf(dcGuild.getCensorKickThreshold()), false);
		setReportInfo(eb, event.getGuild(), dcGuild);
		eb.addBlankField(false);
		eb.addField("Whitelisted channels: ", channelContent, true);
		eb.addField("Mod roles: ", roleContent, true);
		eb.setFooter("Roles with the administrator permission can always use the bot commands.");
		return eb.build();
	}

	/**
	 * Builds the whitelisted channel list as a list of mentions.
	 *
	 * @param whitelistedChannels A list of all Discord channels the bot does not censor.
	 * @return String that mentions all authorized channels in Discord mention style.
	 */
	private String buildWhitelistedChannelList(final Set<WhitelistedChannel> whitelistedChannels) {
		if (whitelistedChannels.size() == 0) {
			return "No channels authorized.";
		}

		final StringBuilder sb = new StringBuilder();
		for (WhitelistedChannel channel : whitelistedChannels) {
			sb.append("<#").append(channel.getChannelId()).append(">\n");
		}

		return sb.toString();
	}

	/**
	 * Builds the mod role list as a list of mentions.
	 *
	 * @param modRoles A list of all roles with mod status for the bot.
	 * @return String that mentions all authorized roles in Discord mention style.
	 */
	private String buildModRoleList(final Set<ModRole> modRoles) {
		if (modRoles.size() == 0) {
			return "No roles authorized.";
		}

		final StringBuilder sb = new StringBuilder();
		for (ModRole role : modRoles) {
			sb.append("<@&").append(role.getRoleId()).append(">\n");
		}

		return sb.toString();
	}

	private void setReportInfo(final EmbedBuilder eb, final Guild guild, final DiscordGuild dcGuild) {
		final long reportEmoteId = dcGuild.getReportEmoteId();
		final Emote reportEmote = guild.getEmoteById(reportEmoteId);
		final String reportEmoteStr = reportEmote == null ? "No report emote set." : reportEmote.getAsMention();
		eb.addField("Report emote:", reportEmoteStr, true);

		final @Min(0) long reportThreshold = dcGuild.getReportCountThreshold();
		eb.addField("Report threshold:", String.valueOf(reportThreshold), true);

		final long reportChannelId = dcGuild.getReportChannelId();
		final String reportChannel = reportChannelId == 0 ? "No report channel set." : "<#" + reportChannelId + ">";
		eb.addField("Report channel:", reportChannel, true);
	}
}
