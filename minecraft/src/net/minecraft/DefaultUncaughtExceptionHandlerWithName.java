package net.minecraft;

import java.lang.Thread.UncaughtExceptionHandler;
import org.slf4j.Logger;

public class DefaultUncaughtExceptionHandlerWithName implements UncaughtExceptionHandler {
	private final Logger logger;

	public DefaultUncaughtExceptionHandlerWithName(Logger logger) {
		this.logger = logger;
	}

	public void uncaughtException(Thread thread, Throwable throwable) {
		this.logger.error("Caught previously unhandled exception :");
		this.logger.error(thread.getName(), throwable);
	}
}
