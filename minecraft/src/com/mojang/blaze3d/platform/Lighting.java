package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Lighting {
	public static void turnBackOn() {
		RenderSystem.enableLighting();
		RenderSystem.enableColorMaterial();
		RenderSystem.colorMaterial(1032, 5634);
	}

	public static void turnOff() {
		RenderSystem.disableLighting();
		RenderSystem.disableColorMaterial();
	}

	public static void setupLevel(Matrix4f matrix4f) {
		RenderSystem.setupLevelDiffuseLighting(matrix4f);
	}

	public static void setupForFlatItems() {
		RenderSystem.setupGuiFlatDiffuseLighting();
	}

	public static void setupFor3DItems() {
		RenderSystem.setupGui3DDiffuseLighting();
	}
}
