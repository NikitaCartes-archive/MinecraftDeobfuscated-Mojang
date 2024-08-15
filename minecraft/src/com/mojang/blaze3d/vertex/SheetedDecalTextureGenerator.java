package com.mojang.blaze3d.vertex;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class SheetedDecalTextureGenerator implements VertexConsumer {
	private final VertexConsumer delegate;
	private final Matrix4f cameraInversePose;
	private final Matrix3f normalInversePose;
	private final float textureScale;
	private final Vector3f worldPos = new Vector3f();
	private final Vector3f normal = new Vector3f();
	private float x;
	private float y;
	private float z;

	public SheetedDecalTextureGenerator(VertexConsumer vertexConsumer, PoseStack.Pose pose, float f) {
		this.delegate = vertexConsumer;
		this.cameraInversePose = new Matrix4f(pose.pose()).invert();
		this.normalInversePose = new Matrix3f(pose.normal()).invert();
		this.textureScale = f;
	}

	@Override
	public VertexConsumer addVertex(float f, float g, float h) {
		this.x = f;
		this.y = g;
		this.z = h;
		this.delegate.addVertex(f, g, h);
		return this;
	}

	@Override
	public VertexConsumer setColor(int i, int j, int k, int l) {
		this.delegate.setColor(-1);
		return this;
	}

	@Override
	public VertexConsumer setUv(float f, float g) {
		return this;
	}

	@Override
	public VertexConsumer setUv1(int i, int j) {
		this.delegate.setUv1(i, j);
		return this;
	}

	@Override
	public VertexConsumer setUv2(int i, int j) {
		this.delegate.setUv2(i, j);
		return this;
	}

	@Override
	public VertexConsumer setNormal(float f, float g, float h) {
		this.delegate.setNormal(f, g, h);
		Vector3f vector3f = this.normalInversePose.transform(f, g, h, this.normal);
		Direction direction = Direction.getApproximateNearest(vector3f.x(), vector3f.y(), vector3f.z());
		Vector3f vector3f2 = this.cameraInversePose.transformPosition(this.x, this.y, this.z, this.worldPos);
		vector3f2.rotateY((float) Math.PI);
		vector3f2.rotateX((float) (-Math.PI / 2));
		vector3f2.rotate(direction.getRotation());
		this.delegate.setUv(-vector3f2.x() * this.textureScale, -vector3f2.y() * this.textureScale);
		return this;
	}
}
