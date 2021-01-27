package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.DiscordMember;
import com.motorbesitzen.messagewatcher.data.repo.DiscordMemberRepo;
import com.motorbesitzen.messagewatcher.util.DiscordMessageUtil;
import com.motorbesitzen.messagewatcher.util.ParseUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.data.domain.PageRequest;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;

public class TopList extends CommandImpl {

	private final DiscordMemberRepo memberRepo;

	public TopList(final DiscordMemberRepo memberRepo) {
		this.memberRepo = memberRepo;
	}

	@Override
	public void execute(GuildMessageReceivedEvent event) {
		final Guild guild = event.getGuild();
		final Message message = event.getMessage();
		final List<DiscordMember> topMembers = getTopList(guild.getIdLong(), message.getContentRaw());
		if (topMembers.size() == 0) {
			sendErrorMessage(event.getChannel(), "Could not generate top list!");
			return;
		}

		final EmbedBuilder eb = buildTopMessage(guild, topMembers);
		answer(event.getChannel(), eb.build());
	}

	private List<DiscordMember> getTopList(final long guildId, final String message) {
		final int topAmount = getAmount(message);
		final PageRequest pageRequest = PageRequest.of(0, topAmount);
		switch (getMode(message)) {
			case "rating":
				return memberRepo.findAllByGuild_GuildIdOrderByRating(guildId, pageRequest);
			case "link":
				return memberRepo.findAllByGuild_GuildIdOrderByLinkCensorCountDesc(guildId, pageRequest);
			case "word":
			default:
				return memberRepo.findAllByGuild_GuildIdOrderByWordCensorCountDesc(guildId, pageRequest);
		}
	}

	private int getAmount(String message) {
		final String[] tokens = message.split(" ");
		final int topAmount = ParseUtil.safelyParseStringToInt(tokens[tokens.length - 1]);
		if (topAmount > 25 || topAmount < 1) {
			return 10;
		}

		return topAmount;
	}

	private String getMode(final String message) {
		final List<String> modes = DiscordMessageUtil.getStringsInQuotationMarks(message);
		if (modes.size() == 0) {
			return "";
		}

		return modes.get(0);
	}

	private EmbedBuilder buildTopMessage(final Guild guild, final List<DiscordMember> topMembers) {
		final EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Top " + topMembers.size() + " of " + guild.getName() + ":");
		eb.setColor(getEmbedColor());
		addMemberFields(eb, topMembers);

		return eb;
	}

	private void addMemberFields(final EmbedBuilder eb, final List<DiscordMember> dcMembers) {
		int pos = 1;
		final NumberFormat nf = generateNumberFormat();
		for (DiscordMember dcMember : dcMembers) {
			final long messagesSent = dcMember.getMessageCount();
			final long wordCensors = dcMember.getWordCensorCount();
			final long linkCensors = dcMember.getLinkCensorCount();
			final double censorsPerMessage = dcMember.getCensorsPerMessage();
			final double censorRating = dcMember.getCensorRating();
			eb.addField("",
					"**" + pos++ + "**. " + "<@" + dcMember.getDiscordId() + ">\n" +
							"Messages sent: " + messagesSent + "\n" +
							"Words censored: " + wordCensors + "\n" +
							"Links censored: " + linkCensors + "\n" +
							"Censors per message: " + nf.format(censorsPerMessage) + "\n" +
							"Censor Rating: " + nf.format(censorRating),
					true
			);
		}
	}

	private NumberFormat generateNumberFormat() {
		final NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		nf.setRoundingMode(RoundingMode.HALF_UP);
		return nf;
	}
}
