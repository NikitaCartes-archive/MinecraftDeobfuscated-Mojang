package com.mojang.blaze3d.platform;

import com.mojang.math.Vector3f;
import java.nio.FloatBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Lighting {
	private static final FloatBuffer BUFFER = MemoryTracker.createFloatBuffer(4);
	private static final Vector3f LIGHT_0 = createVector(0.2F, 1.0F, -0.7F);
	private static final Vector3f LIGHT_1 = createVector(-0.2F, 1.0F, 0.7F);

	private static Vector3f createVector(float f, float g, float h) {
		Vector3f vector3f = new Vector3f(f, g, h);
		vector3f.normalize();
		return vector3f;
	}

	public static void turnOff() {
		GlStateManager.disableLighting();
		GlStateManager.disableLight(0);
		GlStateManager.disableLight(1);
		GlStateManager.disableColorMaterial();
	}

	public static void turnOn() {
		GlStateManager.enableLighting();
		GlStateManager.enableLight(0);
		GlStateManager.enableLight(1);
		GlStateManager.enableColorMaterial();
		GlStateManager.colorMaterial(1032, 5634);
		GlStateManager.light(16384, 4611, getBuffer(LIGHT_0.x(), LIGHT_0.y(), LIGHT_0.z(), 0.0F));
		float f = 0.6F;
		GlStateManager.light(16384, 4609, getBuffer(0.6F, 0.6F, 0.6F, 1.0F));
		GlStateManager.light(16384, 4608, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		GlStateManager.light(16384, 4610, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		GlStateManager.light(16385, 4611, getBuffer(LIGHT_1.x(), LIGHT_1.y(), LIGHT_1.z(), 0.0F));
		GlStateManager.light(16385, 4609, getBuffer(0.6F, 0.6F, 0.6F, 1.0F));
		GlStateManager.light(16385, 4608, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		GlStateManager.light(16385, 4610, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		GlStateManager.shadeModel(7424);
		float g = 0.4F;
		GlStateManager.lightModel(2899, getBuffer(0.4F, 0.4F, 0.4F, 1.0F));
	}

	public static FloatBuffer getBuffer(float f, float g, float h, float i) {
		BUFFER.clear();
		BUFFER.put(f).put(g).put(h).put(i);
		BUFFER.flip();
		return BUFFER;
	}

	public static void turnOnGui() {
		GlStateManager.pushMatrix();
		GlStateManager.rotatef(-30.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef(165.0F, 1.0F, 0.0F, 0.0F);
		turnOn();
		GlStateManager.popMatrix();
	}
}
