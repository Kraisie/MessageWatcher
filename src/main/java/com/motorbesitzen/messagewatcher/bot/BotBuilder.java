package com.motorbesitzen.messagewatcher.bot;

import com.motorbesitzen.messagewatcher.bot.event.DeletionListener;
import com.motorbesitzen.messagewatcher.bot.event.GuildJoinListener;
import com.motorbesitzen.messagewatcher.bot.event.GuildMessageListener;
import com.motorbesitzen.messagewatcher.bot.event.ReactionListener;
import com.motorbesitzen.messagewatcher.util.EnvironmentUtil;
import com.motorbesitzen.messagewatcher.util.LogUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

@Service
public class BotBuilder implements ApplicationListener<ApplicationReadyEvent> {

	private final ApplicationContext applicationContext;
	private final GuildMessageListener msgListener;
	private final GuildJoinListener joinListener;
	private final DeletionListener deletionListener;
	private final ReactionListener reactionListener;

	@Autowired
	private BotBuilder(final ApplicationContext applicationContext, final GuildMessageListener msgListener,
					   final GuildJoinListener joinListener, final DeletionListener deletionListener,
					   final ReactionListener reactionListener) {
		this.applicationContext = applicationContext;
		this.msgListener = msgListener;
		this.joinListener = joinListener;
		this.deletionListener = deletionListener;
		this.reactionListener = reactionListener;
	}

	/**
	 * Gets called by Spring as late as conceivably possible to indicate that the application is ready.
	 * Starts the RoleUpdater and by that the underlying bot.
	 *
	 * @param event Provided by Spring when the Spring application is ready.
	 */
	@Override
	public void onApplicationEvent(final @NotNull ApplicationReadyEvent event) {
		LogUtil.logInfo("Application ready, starting bot...");
		startBot();
	}

	private void startBot() {
		final String discordToken = getToken();
		final JDABuilder jdaBuilder = buildBot(discordToken);
		final JDA jda = botLogin(jdaBuilder);
		if (jda == null) {
			shutdown();
		}
	}

	/**
	 * Gets the token from the environment variables. Stops the application if no token is set.
	 *
	 * @return The token as a {@code String}.
	 */
	private String getToken() {
		final String discordToken = EnvironmentUtil.getEnvironmentVariable("DC_TOKEN");
		if (discordToken == null) {
			LogUtil.logError("RoleWatcher Discord token is null! Please check the environment variables and add a token.");
			shutdown();
			return null;
		}

		if (discordToken.isBlank()) {
			LogUtil.logError("RoleWatcher Discord token is empty! Please check the environment variables and add a token.");
			shutdown();
			return null;
		}

		return discordToken;
	}

	private JDABuilder buildBot(String discordToken) {
		Activity activity = getBotActivity();
		return JDABuilder.createDefault(discordToken)
				.setStatus(OnlineStatus.ONLINE)
				.setActivity(activity)
				.addEventListeners(msgListener, joinListener, reactionListener, deletionListener);
	}

	/**
	 * Generates the activity for the bot to display in the Discord member list according
	 * to information in the environment variables. Can be turned off by not including
	 * {@code BOT_ACTIVITY} or {@code BOT_ACTIVITY_TEXT} in the environment file.
	 *
	 * @return A Discord {@code Activity} object.
	 */
	private Activity getBotActivity() {
		String activityType = EnvironmentUtil.getEnvironmentVariable("BOT_ACTIVITY");
		String activityText = EnvironmentUtil.getEnvironmentVariable("BOT_ACTIVITY_TEXT");
		String activityStreamingUrl = EnvironmentUtil.getEnvironmentVariable("BOT_ACTIVITY_STREAMING_URL");

		if (activityType == null || activityText == null) {
			LogUtil.logInfo("Activity or activity text not given, ignoring activity settings.");
			return null;
		}

		if (activityType.isBlank() || activityText.isBlank()) {
			LogUtil.logWarning("Activity or activity text not given, ignoring activity settings.");
			return null;
		}

		if (activityType.equalsIgnoreCase("streaming") && activityStreamingUrl == null) {
			LogUtil.logWarning("Streaming activity does not have a stream URL given, ignoring activity settings.");
			return null;
		}

		return buildActivity(activityType, activityText, activityStreamingUrl);
	}

	/**
	 * Generates the {@code Activity} object for the bot to use.
	 *
	 * @param type The activity type.
	 * @param text The text to display next to the activity.
	 * @param url  The URL to the stream, only needed when the {@code ActivityType} is set to 'streaming'.
	 * @return A Discord {@code Activity} object.
	 */
	private Activity buildActivity(String type, String text, String url) {
		switch (type.toLowerCase()) {
			case "playing":
				return Activity.playing(text);
			case "watching":
				return Activity.watching(text);
			case "listening":
				return Activity.listening(text);
			case "streaming":
				return Activity.streaming(text, url);
			case "competing":
				return Activity.competing(text);
			default:
				return Activity.watching("user roles");
		}
	}

	/**
	 * Logs in the bot to the API.
	 *
	 * @param builder The builder that is supposed to generate the JDA instance.
	 * @return The JDA instance, the 'core' of the API/the bot.
	 */
	private JDA botLogin(JDABuilder builder) {
		try {
			return builder.build();
		} catch (LoginException e) {
			LogUtil.logError("Token is invalid! Please check your token and add a valid Discord token.");
			LogUtil.logDebug(e.getMessage());
		}

		return null;
	}

	/**
	 * Gracefully stops the Spring application and the JVM afterwards.
	 */
	private void shutdown() {
		SpringApplication.exit(applicationContext, () -> 1);
		System.exit(1);
	}

}
