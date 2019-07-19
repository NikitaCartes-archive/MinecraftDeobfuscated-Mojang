package net.minecraft.server;

import java.io.OutputStream;

public class DebugLoggedPrintStream extends LoggedPrintStream {
	public DebugLoggedPrintStream(String string, OutputStream outputStream) {
		super(string, outputStream);
	}

	@Override
	protected void logLine(String string) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		StackTraceElement stackTraceElement = stackTraceElements[Math.min(3, stackTraceElements.length)];
		LOGGER.info("[{}]@.({}:{}): {}", this.name, stackTraceElement.getFileName(), stackTraceElement.getLineNumber(), string);
	}
}
