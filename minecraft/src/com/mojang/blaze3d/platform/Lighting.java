package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Lighting {
	private static Matrix4f lightPoseForFlatItems = new Matrix4f();
	private static Matrix4f lightPoseFor3DItems = new Matrix4f();

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

	public static void setupGui(Matrix4f matrix4f) {
		lightPoseForFlatItems = matrix4f.copy();
		lightPoseFor3DItems = matrix4f.copy();
		lightPoseFor3DItems.multiply(Vector3f.YP.rotationDegrees(62.0F));
		lightPoseFor3DItems.multiply(Vector3f.XP.rotationDegrees(185.5F));
		RenderSystem.setupGuiDiffuseLighting(matrix4f);
	}

	public static void setupForFlatItems() {
		RenderSystem.setupGuiDiffuseLighting(lightPoseForFlatItems);
	}

	public static void setupFor3DItems() {
		RenderSystem.setupGuiDiffuseLighting(lightPoseFor3DItems);
	}
}
