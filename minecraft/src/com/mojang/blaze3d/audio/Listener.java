package com.mojang.blaze3d.audio;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;

@Environment(EnvType.CLIENT)
public class Listener {
	public static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);
	private float gain = 1.0F;

	public void setListenerPosition(Vec3 vec3) {
		AL10.alListener3f(4100, (float)vec3.x, (float)vec3.y, (float)vec3.z);
	}

	public void setListenerOrientation(Vec3 vec3, Vec3 vec32) {
		AL10.alListenerfv(4111, new float[]{(float)vec3.x, (float)vec3.y, (float)vec3.z, (float)vec32.x, (float)vec32.y, (float)vec32.z});
	}

	public void setGain(float f) {
		AL10.alListenerf(4106, f);
		this.gain = f;
	}

	public float getGain() {
		return this.gain;
	}

	public void reset() {
		this.setListenerPosition(Vec3.ZERO);
		this.setListenerOrientation(new Vec3(0.0, 0.0, -1.0), UP);
	}
}
