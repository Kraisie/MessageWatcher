package com.motorbesitzen.messagewatcher.bot.command.impl;

import com.motorbesitzen.messagewatcher.bot.command.CommandImpl;
import com.motorbesitzen.messagewatcher.data.dao.DiscordGuild;
import com.motorbesitzen.messagewatcher.data.dao.DiscordMember;
import com.motorbesitzen.messagewatcher.data.repo.DiscordMemberRepo;
import com.motorbesitzen.messagewatcher.util.DiscordMessageUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("untrust")
class UntrustMember extends CommandImpl {

	private final DiscordMemberRepo memberRepo;

	private UntrustMember(final DiscordMemberRepo memberRepo) {
		this.memberRepo = memberRepo;
	}

	@Override
	public String getName() {
		return "untrust";
	}

	@Override
	public String getUsage() {
		return getName() + " (@user|id)";
	}

	@Override
	public String getDescription() {
		return "Enables or disables that all messages from the mentioned member need to get verified in the future. " +
				"Only works if a verification channel is set!";
	}

	@Override
	public void execute(final GuildMessageReceivedEvent event) {
		final TextChannel channel = event.getChannel();
		final Message message = event.getMessage();
		final long mentionedMemberId = DiscordMessageUtil.getMentionedMemberId(message);
		if (mentionedMemberId == -1) {
			sendErrorMessage(channel, "Please provide a mention or an ID.");
			return;
		}

		final Guild guild = event.getGuild();
		final Optional<DiscordMember> dcMemberOpt = memberRepo.findByDiscordIdAndGuild_GuildId(mentionedMemberId, guild.getIdLong());
		final DiscordMember dcMember = dcMemberOpt.orElseGet(() -> DiscordMember.createDefault(mentionedMemberId, DiscordGuild.createDefault(guild.getIdLong())));
		dcMember.setUntrusted(!dcMember.isUntrusted());
		memberRepo.save(dcMember);
		answer(channel, (dcMember.isUntrusted() ? "Added untrusted status to member." : "Revoked untrusted status of member."));
	}
}
