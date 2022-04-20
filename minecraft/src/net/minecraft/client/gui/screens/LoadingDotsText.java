package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class LoadingDotsText {
	private static final String[] FRAMES = new String[]{"O o o", "o O o", "o o O", "o O o"};
	private static final long INTERVAL_MS = 300L;

	public static String get(long l) {
		int i = (int)(l / 300L % (long)FRAMES.length);
		return FRAMES[i];
	}
}
