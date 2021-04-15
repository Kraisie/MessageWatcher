package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.bot.service.EnvSettings;
import com.motorbesitzen.messagewatcher.data.dao.DiscordMember;
import com.motorbesitzen.messagewatcher.data.repo.DiscordMemberRepo;
import com.motorbesitzen.messagewatcher.util.DiscordMessageUtil;
import com.motorbesitzen.messagewatcher.util.ParseUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;

@Service("top")
class TopList extends CommandImpl {

	private final EnvSettings envSettings;
	private final DiscordMemberRepo memberRepo;

	@Autowired
	TopList(final EnvSettings envSettings, final DiscordMemberRepo memberRepo) {
		this.envSettings = envSettings;
		this.memberRepo = memberRepo;
	}

	@Override
	public String getName() {
		return "top";
	}

	@Override
	public String getUsage() {
		return getName() + " \"(word|link|rating|warn|cpm|msg)\" (1-25)";
	}

	@Override
	public String getDescription() {
		return "Displays the users with the highest censor count in one of the categories. Can display up to 25 users. " +
				"Defaults to a list of 10 users if no valid range is set and is ordered by censored words if no category " +
				"is set.";
	}

	@Override
	public void execute(final GuildMessageReceivedEvent event) {
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
			case "warn":
				return memberRepo.findAllByGuild_GuildIdOrderByWarningCountDesc(guildId, pageRequest);
			case "cpm":
				return memberRepo.findAllByGuild_GuildIdOrderByCensorsPerMessage(guildId, pageRequest);
			case "msg":
				return memberRepo.findAllByGuild_GuildIdOrderByMessageCount(guildId, pageRequest);
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
		eb.setColor(envSettings.getEmbedColor());
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
			final long warningCount = dcMember.getWarningCount();
			eb.addField("",
					"**" + pos++ + "**. " + "<@" + dcMember.getDiscordId() + ">\n" +
							"Messages sent: " + messagesSent + "\n" +
							"Words censored: " + wordCensors + "\n" +
							"Links censored: " + linkCensors + "\n" +
							"CPM: " + nf.format(censorsPerMessage) + "\n" +
							"Rating: " + nf.format(censorRating) + "\n" +
							"Warnings: " + warningCount,
					true
			);
		}
	}

	private NumberFormat generateNumberFormat() {
		final NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(4);
		nf.setRoundingMode(RoundingMode.HALF_UP);
		return nf;
	}
}
