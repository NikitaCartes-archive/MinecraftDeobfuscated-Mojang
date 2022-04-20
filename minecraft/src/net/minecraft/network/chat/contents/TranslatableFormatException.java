package net.minecraft.network.chat.contents;

public class TranslatableFormatException extends IllegalArgumentException {
	public TranslatableFormatException(TranslatableContents translatableContents, String string) {
		super(String.format("Error parsing: %s: %s", translatableContents, string));
	}

	public TranslatableFormatException(TranslatableContents translatableContents, int i) {
		super(String.format("Invalid index %d requested for %s", i, translatableContents));
	}

	public TranslatableFormatException(TranslatableContents translatableContents, Throwable throwable) {
		super(String.format("Error while parsing: %s", translatableContents), throwable);
	}
}
