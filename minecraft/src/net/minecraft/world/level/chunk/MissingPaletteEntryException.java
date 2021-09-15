package net.minecraft.world.level.chunk;

public class MissingPaletteEntryException extends RuntimeException {
	public MissingPaletteEntryException(int i) {
		super("Missing Palette entry for index " + i + ".");
	}
}
