package net.minecraft.client.main;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SilentInitException extends RuntimeException {
	public SilentInitException(String string) {
		super(string);
	}

	public SilentInitException(String string, Throwable throwable) {
		super(string, throwable);
	}
}
