package net.minecraft.world.level.storage;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class LevelStorageException extends Exception {
	public LevelStorageException(String string) {
		super(string);
	}
}
