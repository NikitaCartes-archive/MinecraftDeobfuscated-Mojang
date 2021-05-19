package net.minecraft.client.gui.narration;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum NarratedElementType {
	TITLE,
	POSITION,
	HINT,
	USAGE;
}
