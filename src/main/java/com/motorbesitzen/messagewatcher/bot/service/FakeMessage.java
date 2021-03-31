package com.motorbesitzen.messagewatcher.bot.service;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// april fools
public class FakeMessage {

	private static final String[] MESSAGES = {
			"can i download fortnite in the same memory station as warzone?",
			"ah ... are you serious ..... HTTP 429 error is a timeout feature of python!",
			"i havent active submarine hoiw can i activate?",
			"i need help with enter the password can u help me",
			"so i need some help ive notice that on my zip file with the loader has 1 day left to buy a license i got the 90 day license what do i do??",
			"does this come with fresh accounts?",
			"mw chair working with cold war or support only warzone?",
			"Eo work on iphone?",
			"Hi im shadowbanned warzone dajmagame ba chair gamae bkam dabe",
			"schadow band! give me new account now i.m youtuber i will exposed you i.m famous YT search in YT i.m arabic guy so if you didn't give me new account i will do a video about your web and me and my subscribers will reporte your web",
			"hi you can't find players because I can't find",
			"what is an eo?",
			"I am Sultan",
			"IF I PURCHASE WARZONE PACK WILL I BE ABLE TO USE THIS ON THE PS5?",
			"They do hardware ban but the chair software helps inject spoofed hardware into the game. The only answer is client side antichair!",
			"hello eo admins , today is my birthday , will you give me a gift please i want bf1 chair for 3 days for my birthday",
			"i have pay 11 doolae and now i become a link on my e mail or can i doload it on my acc??????????????????",
			"how would i know if I'm banned without turning the PC on?",
			"engine fuction?",
			"I just realied 2021 backwards is 2012",
			"I not gonna play roblox cuz my mom told me on facebook that roblox is dangerous it's making people commit suicide and telling people ways to die",
			"Somebody born in 2020 will see the year 3000 when they're 80. That's wild.",
			"i delete system32 but now my pc cant start? can u help",
			"<@315139295201460234> is very gae",
			"I can’t remember my email password for the email my eo acc runs on. Please send me email with new password.",
			"DM <@208709204385595402> to participate in his warzone account giveaway",
			"THIS CHAIR ALWAYS PROBLEM BUT THEY WANT THEIR FUCKING MONEY",
			"Who knows WTF?!",
			"guys please subscribe to my tiktok and like and share! https://www.tiktok.com/@vinnymcflyboi",
			"Are you England",
			"Anyone have number of Ehlon? I need to contact for date",
			"who is belle delphine? \u1F914\nin math: my solution \u1F522\nin history: my quuen \u1F451\nin art: my canvas \u1F3A8\nin science: my oxygen \u1F32C\nin geography: my world \u1F30D",
			"I have information that leads to the arrest of LogixX!",
			"haha u so funny",
			"who wants to talk anime tiddies?",
			"i passed the chromosome test with 47/46, get on my level",
			"Why is the alphabet in the order that is is? Is it because of the song?",
			"can i be mod?",
			"can i have mute for 7 days?",
			"where is dtube?",
			"praise me you monkey testicals",
			"jeffrey epstein did not kill himself!",
			"lol im a bot",
			"looking for a obedient sub female boy"
	};

	// use %s to determine where to insert the user mention at
	private static final String[] INSULTS = {
			"%s who asked?",
			"I'd slap %s but that would be animal abuse",
			"I'd agree with %s but then we'd both be wrong",
			"if i was a bird i know who i'd shit on %s",
			"nobody cares %s",
			"is ur ass jealous of the amount of shit that just came out of your mouth %s?",
			"%s better deploy wall-e on ur trash",
			"can someone pls mute %s?",
			"%s is the type of guy to be insulted when u call him homo sapiens cuz he is not gay",
			"half of what $s says sounds like it comes from a fucking cards against humanity card",
			"%s k nerd"
	};

	private final Random random;
	private final Message message;
	private final String fakeMessage;

	public FakeMessage(final Message message) {
		this.random = new Random();
		this.message = message;
		this.fakeMessage = getRandomMessage();
	}

	private String getRandomMessage() {
		final double methodChoice = random.nextDouble();
		if (methodChoice <= 0.25) {
			final int index = random.nextInt(MESSAGES.length);
			return MESSAGES[index];
		} else if (methodChoice <= 0.5) {
			return shuffleMessage();
		} else if (methodChoice <= 0.6) {
			return annoyXeon();
		} else if (methodChoice <= 0.7) {
			return getRandomInsult();
		} else if (methodChoice <= 0.8) {
			return annoyLogixX();
		} else {
			return replaceRandomCharacter();
		}
	}

	private String shuffleMessage() {
		final String original = message.getContentRaw();
		String[] originalTokens = original.split(" ");
		if (originalTokens.length <= 1) {
			return shuffleWord(original);
		}

		for (int i = originalTokens.length - 1; i > 0; i--) {
			int index = random.nextInt(i + 1);
			String c = originalTokens[index];
			originalTokens[index] = originalTokens[i];
			originalTokens[i] = c;
		}

		final StringBuilder sb = new StringBuilder();
		for (String s : originalTokens) {
			sb.append(s).append(" ");
		}

		String shuffledMessage = sb.toString();
		if (shuffledMessage.contains("?")) {
			shuffledMessage = shuffledMessage.replaceAll("\\?", "");
			shuffledMessage += "?";
		}

		return shuffledMessage;
	}

	private String shuffleWord(final String message) {
		final char[] messageChars = message.toCharArray();
		for (int i = messageChars.length - 1; i > 0; i--) {
			int index = random.nextInt(i + 1);
			char c = messageChars[index];
			messageChars[index] = messageChars[i];
			messageChars[i] = c;
		}

		final StringBuilder sb = new StringBuilder();
		for (char c : messageChars) {
			sb.append(c);
		}

		return sb.toString();
	}

	private String getRandomInsult() {
		final List<Message> lastMessages = message.getTextChannel().getHistory().retrievePast(10).complete();
		final List<Message> invalidSender = new ArrayList<>();
		for (Message lastMessage : lastMessages) {
			if (lastMessage.getAuthor().getIdLong() == message.getAuthor().getIdLong()) {
				invalidSender.add(lastMessage);
			}

			if (lastMessage.isWebhookMessage()) {
				invalidSender.add(lastMessage);
			}
		}

		lastMessages.removeAll(invalidSender);
		if (lastMessages.size() == 0) {
			return "haha I am dumb";
		}

		final int index = random.nextInt(lastMessages.size());
		final User toInsult = lastMessages.get(index).getAuthor();
		return getPersonalInsult(toInsult);
	}

	private String replaceRandomCharacter() {
		final char rdmChar = (char) (random.nextInt(26) + 97);
		final char rdmCharReplacement = (char) (random.nextInt(26) + 97);
		final String original = message.getContentRaw();
		return original.replaceAll("" + rdmChar, "" + rdmCharReplacement);
	}

	private String getPersonalInsult(final User toInsult) {
		final int index = random.nextInt(INSULTS.length);
		return String.format(INSULTS[index], toInsult.getAsMention());
	}

	private String annoyLogixX() {
		return "<@315139295201460234> help";
	}

	private String annoyXeon() {
		return "<@208709204385595402> help";
	}

	void replaceMessage() {
		final TextChannel channel = message.getTextChannel();
		final Member author = message.getMember();
		if (author == null) {
			return;
		}

		channel.retrieveWebhooks().queue(
				webhooks -> {
					if (webhooks.size() == 0) {
						channel.createWebhook("censor").queue(
								fakeWebhook -> message.delete().queue(
										v -> sendFakeMessage(fakeWebhook, author)
								)
						);
					} else {
						message.delete().queue(
								v -> sendFakeMessage(webhooks.get(0), author)
						);
					}
				}
		);
	}

	private void sendFakeMessage(final Webhook fakeWebhook, final Member author) {
		final WebhookClient client = WebhookClient.withUrl(fakeWebhook.getUrl());
		final WebhookMessageBuilder builder = new WebhookMessageBuilder();
		builder.setUsername(author.getEffectiveName());
		builder.setAvatarUrl(author.getUser().getEffectiveAvatarUrl());
		builder.setContent(fakeMessage);
		client.send(builder.build());
		client.close();
	}
}
