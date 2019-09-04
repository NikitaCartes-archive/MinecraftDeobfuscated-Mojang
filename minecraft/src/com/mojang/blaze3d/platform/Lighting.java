package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
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
		RenderSystem.disableLighting();
		RenderSystem.disableLight(0);
		RenderSystem.disableLight(1);
		RenderSystem.disableColorMaterial();
	}

	public static void turnOn() {
		RenderSystem.enableLighting();
		RenderSystem.enableLight(0);
		RenderSystem.enableLight(1);
		RenderSystem.enableColorMaterial();
		RenderSystem.colorMaterial(1032, 5634);
		RenderSystem.light(16384, 4611, getBuffer(LIGHT_0.x(), LIGHT_0.y(), LIGHT_0.z(), 0.0F));
		float f = 0.6F;
		RenderSystem.light(16384, 4609, getBuffer(0.6F, 0.6F, 0.6F, 1.0F));
		RenderSystem.light(16384, 4608, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		RenderSystem.light(16384, 4610, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		RenderSystem.light(16385, 4611, getBuffer(LIGHT_1.x(), LIGHT_1.y(), LIGHT_1.z(), 0.0F));
		RenderSystem.light(16385, 4609, getBuffer(0.6F, 0.6F, 0.6F, 1.0F));
		RenderSystem.light(16385, 4608, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		RenderSystem.light(16385, 4610, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		RenderSystem.shadeModel(7424);
		float g = 0.4F;
		RenderSystem.lightModel(2899, getBuffer(0.4F, 0.4F, 0.4F, 1.0F));
	}

	public static FloatBuffer getBuffer(float f, float g, float h, float i) {
		BUFFER.clear();
		BUFFER.put(f).put(g).put(h).put(i);
		BUFFER.flip();
		return BUFFER;
	}

	public static void turnOnGui() {
		RenderSystem.pushMatrix();
		RenderSystem.rotatef(-30.0F, 0.0F, 1.0F, 0.0F);
		RenderSystem.rotatef(165.0F, 1.0F, 0.0F, 0.0F);
		turnOn();
		RenderSystem.popMatrix();
	}
}
