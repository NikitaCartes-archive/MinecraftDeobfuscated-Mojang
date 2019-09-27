package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Lighting {
	public static void turnBackOn() {
		RenderSystem.enableLighting();
		RenderSystem.enableColorMaterial();
	}

	public static void turnOff() {
		RenderSystem.disableLighting();
		RenderSystem.disableColorMaterial();
	}

	public static void setupLevel() {
		RenderSystem.setupLevelDiffuseLighting();
	}

	public static void setupGui() {
		RenderSystem.setupGuiDiffuseLighting();
	}
}
