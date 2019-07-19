package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;

@Environment(EnvType.CLIENT)
public class RealmsSharedConstants {
	public static final int TICKS_PER_SECOND = 20;
	public static final char[] ILLEGAL_FILE_CHARACTERS = SharedConstants.ILLEGAL_FILE_CHARACTERS;
}
