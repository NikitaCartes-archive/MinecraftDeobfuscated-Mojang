package com.mojang.blaze3d;

import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogListeners;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.event.Level;

@Environment(EnvType.CLIENT)
public class TracyBootstrap {
	private static boolean setup;

	public static void setup() {
		if (!setup) {
			TracyClient.load();
			if (TracyClient.isAvailable()) {
				LogListeners.addListener("Tracy", (string, level) -> TracyClient.message(string, messageColor(level)));
				setup = true;
			}
		}
	}

	private static int messageColor(Level level) {
		return switch (level) {
			case DEBUG -> 11184810;
			case WARN -> 16777130;
			case ERROR -> 16755370;
			default -> 16777215;
		};
	}
}
