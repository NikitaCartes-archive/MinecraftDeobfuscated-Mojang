package com.mojang.blaze3d.shaders;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

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

	public void set(int i) {
	}

	public void set(int i, int j) {
	}

	public void set(int i, int j, int k) {
	}

	public void set(int i, int j, int k, int l) {
	}

	public void set(float[] fs) {
	}

	public void set(Vector3f vector3f) {
	}

	public void set(Vector4f vector4f) {
	}

	public void setMat2x2(float f, float g, float h, float i) {
	}

	public void setMat2x3(float f, float g, float h, float i, float j, float k) {
	}

	public void setMat2x4(float f, float g, float h, float i, float j, float k, float l, float m) {
	}

	public void setMat3x2(float f, float g, float h, float i, float j, float k) {
	}

	public void setMat3x3(float f, float g, float h, float i, float j, float k, float l, float m, float n) {
	}

	public void setMat3x4(float f, float g, float h, float i, float j, float k, float l, float m, float n, float o, float p, float q) {
	}

	public void setMat4x2(float f, float g, float h, float i, float j, float k, float l, float m) {
	}

	public void setMat4x3(float f, float g, float h, float i, float j, float k, float l, float m, float n, float o, float p, float q) {
	}

	public void setMat4x4(
		float f, float g, float h, float i, float j, float k, float l, float m, float n, float o, float p, float q, float r, float s, float t, float u
	) {
	}

	public void set(Matrix4f matrix4f) {
	}

	public void set(Matrix3f matrix3f) {
	}
}
