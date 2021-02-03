package com.motorbesitzen.messagewatcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper functions for logging.
 */
public final class LogUtil {

	private final static Logger LOGGER = LoggerFactory.getLogger(LogUtil.class);

	/**
	 * Used to log an information. Uses Spring (slf4j) info logger ({@code Logger.info(...)}).
	 *
	 * @param message The message to log.
	 */
	public static void logInfo(final String message) {
		LOGGER.info(message);
	}

	/**
	 * Used to log a warning due to some weird behaviour. Uses Spring (slf4j) info logger ({@code Logger.warn(...)}).
	 *
	 * @param message The message to log.
	 */
	public static void logWarning(final String message) {
		LOGGER.warn(message);
	}

	/**
	 * Used to log debug information. Uses Spring (slf4j) debug logger ({@code Logger.debug(...)}).
	 *
	 * @param message The message to print as debug information.
	 */
	public static void logDebug(final String message) {
		LOGGER.debug(message);
	}

	/**
	 * Used to log debug information. Uses Spring (slf4j) debug logger ({@code Logger.debug(...)}).
	 *
	 * @param message The message to print as debug information.
	 * @param t       The thrown error.
	 */
	public static void logDebug(final String message, Throwable t) {
		LOGGER.debug(message, t);
	}

	/**
	 * Used to log errors. Uses Spring (slf4j) debug logger ({@code Logger.debug(...)}).
	 *
	 * @param message The message to describe the error.
	 */
	public static void logError(final String message) {
		LOGGER.error(message);
	}

	/**
	 * Used to log errors and additional info. Uses Spring (slf4j) debug logger ({@code Logger.debug(...)}).
	 *
	 * @param message The message to describe the error.
	 * @param t       The thrown error.
	 */
	public static void logError(final String message, Throwable t) {
		LOGGER.error(message, t);
	}
}
