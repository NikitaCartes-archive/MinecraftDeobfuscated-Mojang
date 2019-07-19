package net.minecraft;

import java.lang.Thread.UncaughtExceptionHandler;
import org.apache.logging.log4j.Logger;

public class DefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {
	private final Logger logger;

	public DefaultUncaughtExceptionHandler(Logger logger) {
		this.logger = logger;
	}

	public void uncaughtException(Thread thread, Throwable throwable) {
		this.logger.error("Caught previously unhandled exception :", throwable);
	}
}
