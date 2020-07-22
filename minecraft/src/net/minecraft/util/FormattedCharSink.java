package net.minecraft.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Style;

@FunctionalInterface
public interface FormattedCharSink {
	@Environment(EnvType.CLIENT)
	boolean accept(int i, Style style, int j);
}
