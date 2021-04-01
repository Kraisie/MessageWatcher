package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.DiscordMember;
import com.motorbesitzen.messagewatcher.data.repo.DiscordMemberRepo;
import com.motorbesitzen.messagewatcher.util.DiscordMessageUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Optional;

@Service("stats")
class CensorStats extends CommandImpl {

	private final DiscordMemberRepo memberRepo;

	@Autowired
	CensorStats(final DiscordMemberRepo memberRepo) {
		this.memberRepo = memberRepo;
	}

	@Override
	public String getName() {
		return "stats";
	}

	@Override
	public String getUsage() {
		return getName() + " (@user|discordid)";
	}

	@Override
	public String getDescription() {
		return "Displays the censor stats of the given user.";
	}

	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final TextChannel channel = event.getChannel();
		final Message message = event.getMessage();
		final long mentionedMemberId = DiscordMessageUtil.getMentionedMemberId(message);
		if (mentionedMemberId == -1) {
			sendErrorMessage(channel, "Please provide a mention or an ID for the stats command.");
			return;
		}

		final Guild guild = event.getGuild();
		final Optional<DiscordMember> dcMemberOpt = memberRepo.findByDiscordIdAndGuild_GuildId(mentionedMemberId, guild.getIdLong());
		dcMemberOpt.ifPresentOrElse(
				dcMember -> sendMemberStats(channel, dcMember),
				() -> sendErrorMessage(channel, "No stats available for that member.")
		);
	}

	private void sendMemberStats(final TextChannel channel, final DiscordMember dcMember) {
		channel.getGuild().retrieveMemberById(dcMember.getDiscordId()).queue(
				member -> sendStats(channel, dcMember, member.getUser().getAsTag()),
				throwable -> sendStats(channel, dcMember, String.valueOf(dcMember.getDiscordId()))
		);
	}

	private void sendStats(final TextChannel channel, final DiscordMember dcMember, final String discordTag) {
		final long msgCount = dcMember.getMessageCount();
		final long wordCount = dcMember.getWordCensorCount();
		final long linkCount = dcMember.getLinkCensorCount();
		final long warningCount = dcMember.getWarningCount();

		final EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Censor stats for " + discordTag + ":");
		eb.setColor(DiscordMessageUtil.getEmbedColor());
		eb.addField("Messages sent:", String.valueOf(msgCount), true);
		eb.addField("Words censored:", String.valueOf(wordCount), true);
		eb.addField("Links censored:", String.valueOf(linkCount), true);
		eb.addField("Censor warnings:", String.valueOf(warningCount), true);
		eb.addBlankField(false);

		final NumberFormat nf = generateNumberFormat();
		final double censorsPerMessage = dcMember.getCensorsPerMessage();
		final double censorRating = dcMember.getCensorRating();
		eb.addField("Censors per message:", nf.format(censorsPerMessage), true);
		eb.addField("Rating:", nf.format(censorRating), true);

		answer(channel, eb.build());
	}

	private NumberFormat generateNumberFormat() {
		final NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(4);
		nf.setMaximumFractionDigits(4);
		nf.setRoundingMode(RoundingMode.HALF_UP);
		return nf;
	}
}
