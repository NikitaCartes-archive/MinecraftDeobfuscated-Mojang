package com.mojang.blaze3d.audio;

import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;

@Environment(EnvType.CLIENT)
public class Listener {
	private float gain = 1.0F;

	public void setListenerPosition(Vec3 vec3) {
		AL10.alListener3f(4100, (float)vec3.x, (float)vec3.y, (float)vec3.z);
	}

	public void setListenerOrientation(Vector3f vector3f, Vector3f vector3f2) {
		AL10.alListenerfv(4111, new float[]{vector3f.x(), vector3f.y(), vector3f.z(), vector3f2.x(), vector3f2.y(), vector3f2.z()});
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
		this.setListenerOrientation(Vector3f.ZN, Vector3f.YP);
	}
}
