package com.mojang.blaze3d.vertex;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;

@Environment(EnvType.CLIENT)
public class BreakingTextureGenerator extends DefaultedVertexConsumer {
	private final VertexConsumer delegate;
	private final Matrix4f cameraInversePose;
	private final Matrix3f normalPose;
	private float x;
	private float y;
	private float z;
	private int r;
	private int g;
	private int b;
	private int a;
	private int overlayU;
	private int overlayV;
	private int lightCoords;
	private float nx;
	private float ny;
	private float nz;

	public BreakingTextureGenerator(VertexConsumer vertexConsumer, PoseStack.Pose pose) {
		this.delegate = vertexConsumer;
		this.cameraInversePose = pose.pose().copy();
		this.cameraInversePose.invert();
		this.normalPose = pose.normal().copy();
		this.normalPose.invert();
		this.resetState();
	}

	private void resetState() {
		this.x = 0.0F;
		this.y = 0.0F;
		this.z = 0.0F;
		this.r = this.defaultR;
		this.g = this.defaultG;
		this.b = this.defaultB;
		this.a = this.defaultA;
		this.overlayU = 0;
		this.overlayV = 10;
		this.lightCoords = 15728880;
		this.nx = 0.0F;
		this.ny = 1.0F;
		this.nz = 0.0F;
	}

	@Override
	public void endVertex() {
		Vector3f vector3f = new Vector3f(this.nx, this.ny, this.nz);
		vector3f.transform(this.normalPose);
		Direction direction = Direction.getNearest(vector3f.x(), vector3f.y(), vector3f.z());
		Vector4f vector4f = new Vector4f(this.x, this.y, this.z, 1.0F);
		vector4f.transform(this.cameraInversePose);
		vector4f.transform(Vector3f.YP.rotationDegrees(180.0F));
		vector4f.transform(Vector3f.XP.rotationDegrees(-90.0F));
		vector4f.transform(direction.getRotation());
		float f = -vector4f.x();
		float g = -vector4f.y();
		this.delegate
			.vertex((double)this.x, (double)this.y, (double)this.z)
			.color(this.r, this.g, this.b, this.a)
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
		if (this.defaultColorSet) {
			throw new IllegalStateException();
		} else {
			this.r = i;
			this.g = j;
			this.b = k;
			this.a = l;
			return this;
		}
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
