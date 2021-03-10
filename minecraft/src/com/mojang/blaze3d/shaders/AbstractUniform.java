package com.mojang.blaze3d.shaders;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AbstractUniform {
	public void set(float f) {
	}

	public void set(float f, float g) {
	}

	public void set(float f, float g, float h) {
	}

	public void set(float f, float g, float h, float i) {
	}

	public void setSafe(float f, float g, float h, float i) {
	}

	public void setSafe(int i, int j, int k, int l) {
	}

	public void set(float[] fs) {
	}

	public void set(Vector3f vector3f) {
	}

	public void set(Matrix4f matrix4f) {
	}
}
