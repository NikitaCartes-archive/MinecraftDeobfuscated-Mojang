package com.mojang.blaze3d.platform;

import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DisplayData {
	public final int width;
	public final int height;
	public final OptionalInt fullscreenWidth;
	public final OptionalInt fullscreenHeight;
	public final boolean isFullscreen;

	public DisplayData(int i, int j, OptionalInt optionalInt, OptionalInt optionalInt2, boolean bl) {
		this.width = i;
		this.height = j;
		this.fullscreenWidth = optionalInt;
		this.fullscreenHeight = optionalInt2;
		this.isFullscreen = bl;
	}
}
