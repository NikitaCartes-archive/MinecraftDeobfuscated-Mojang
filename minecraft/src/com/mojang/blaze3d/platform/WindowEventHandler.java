package com.mojang.blaze3d.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface WindowEventHandler {
	void setWindowActive(boolean bl);

	void resizeDisplay();

	void cursorEntered();
}
