package net.minecraft;

import org.apache.commons.lang3.StringEscapeUtils;

public class ResourceLocationException extends RuntimeException {
	public ResourceLocationException(String string) {
		super(StringEscapeUtils.escapeJava(string));
	}

	public ResourceLocationException(String string, Throwable throwable) {
		super(StringEscapeUtils.escapeJava(string), throwable);
	}
}
