package com.mojang.blaze3d.vertex;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public class SheetedDecalTextureGenerator extends DefaultedVertexConsumer {
	private final VertexConsumer delegate;
	private final Matrix4f cameraInversePose;
	private final Matrix3f normalInversePose;
	private float x;
	private float y;
	private float z;
	private int overlayU;
	private int overlayV;
	private int lightCoords;
	private float nx;
	private float ny;
	private float nz;

	public SheetedDecalTextureGenerator(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f) {
		this.delegate = vertexConsumer;
		this.cameraInversePose = new Matrix4f(matrix4f).invert();
		this.normalInversePose = new Matrix3f(matrix3f).invert();
		this.resetState();
	}

	private void resetState() {
		this.x = 0.0F;
		this.y = 0.0F;
		this.z = 0.0F;
		this.overlayU = 0;
		this.overlayV = 10;
		this.lightCoords = 15728880;
		this.nx = 0.0F;
		this.ny = 1.0F;
		this.nz = 0.0F;
	}

	@Override
	public void endVertex() {
		Vector3f vector3f = this.normalInversePose.transform(new Vector3f(this.nx, this.ny, this.nz));
		Direction direction = Direction.getNearest(vector3f.x(), vector3f.y(), vector3f.z());
		Vector4f vector4f = this.cameraInversePose.transform(new Vector4f(this.x, this.y, this.z, 1.0F));
		vector4f.rotateY((float) Math.PI);
		vector4f.rotateX((float) (-Math.PI / 2));
		vector4f.rotate(direction.getRotation());
		float f = -vector4f.x();
		float g = -vector4f.y();
		this.delegate
			.vertex((double)this.x, (double)this.y, (double)this.z)
			.color(1.0F, 1.0F, 1.0F, 1.0F)
			.uv(f, g)
			.overlayCoords(this.overlayU, this.overlayV)
			.uv2(this.lightCoords)
			.normal(this.nx, this.ny, this.nz)
			.endVertex();
		this.resetState();
	}

	@Override
	public VertexConsumer vertex(double d, double e, double f) {
		this.x = (float)d;
		this.y = (float)e;
		this.z = (float)f;
		return this;
	}

	@Override
	public VertexConsumer color(int i, int j, int k, int l) {
		return this;
	}

	@Override
	public VertexConsumer uv(float f, float g) {
		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int i, int j) {
		this.overlayU = i;
		this.overlayV = j;
		return this;
	}

	@Override
	public VertexConsumer uv2(int i, int j) {
		this.lightCoords = i | j << 16;
		return this;
	}

	@Override
	public VertexConsumer normal(float f, float g, float h) {
		this.nx = f;
		this.ny = g;
		this.nz = h;
		return this;
	}
}
