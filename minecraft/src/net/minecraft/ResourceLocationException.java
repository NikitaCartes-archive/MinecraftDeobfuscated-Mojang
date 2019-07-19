package net.minecraft;

public class ResourceLocationException extends RuntimeException {
	public ResourceLocationException(String string) {
		super(string);
	}

	public ResourceLocationException(String string, Throwable throwable) {
		super(string, throwable);
	}
}
