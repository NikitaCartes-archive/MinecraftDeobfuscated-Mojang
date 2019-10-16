package com.mojang.blaze3d.vertex;

import com.google.common.collect.Queues;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Deque;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class PoseStack {
	private final Deque<PoseStack.Pose> poseStack = Util.make(Queues.<PoseStack.Pose>newArrayDeque(), arrayDeque -> {
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.setIdentity();
		Matrix3f matrix3f = new Matrix3f();
		matrix3f.setIdentity();
		arrayDeque.add(new PoseStack.Pose(matrix4f, matrix3f));
	});

	public void translate(double d, double e, double f) {
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.setIdentity();
		matrix4f.translate(new Vector3f((float)d, (float)e, (float)f));
		PoseStack.Pose pose = (PoseStack.Pose)this.poseStack.getLast();
		pose.pose.multiply(matrix4f);
	}

	public void scale(float f, float g, float h) {
		PoseStack.Pose pose = (PoseStack.Pose)this.poseStack.getLast();
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.setIdentity();
		matrix4f.set(0, 0, f);
		matrix4f.set(1, 1, g);
		matrix4f.set(2, 2, h);
		pose.pose.multiply(matrix4f);
		if (f != g || g != h) {
			float i = Mth.fastInvCubeRoot(f * g * h);
			Matrix3f matrix3f = new Matrix3f();
			matrix3f.set(0, 0, i / f);
			matrix3f.set(1, 1, i / g);
			matrix3f.set(2, 2, i / h);
			pose.normal.mul(matrix3f);
		}
	}

	public void mulPose(Quaternion quaternion) {
		PoseStack.Pose pose = (PoseStack.Pose)this.poseStack.getLast();
		pose.pose.multiply(quaternion);
		pose.normal.mul(quaternion);
	}

	public void pushPose() {
		PoseStack.Pose pose = (PoseStack.Pose)this.poseStack.getLast();
		this.poseStack.addLast(new PoseStack.Pose(pose.pose.copy(), pose.normal.copy()));
	}

	public void popPose() {
		this.poseStack.removeLast();
	}

	public Matrix4f getPose() {
		return ((PoseStack.Pose)this.poseStack.getLast()).pose;
	}

	public Matrix3f getNormal() {
		return ((PoseStack.Pose)this.poseStack.getLast()).normal;
	}

	public boolean clear() {
		return this.poseStack.size() == 1;
	}

	@Environment(EnvType.CLIENT)
	static final class Pose {
		private final Matrix4f pose;
		private final Matrix3f normal;

		private Pose(Matrix4f matrix4f, Matrix3f matrix3f) {
			this.pose = matrix4f;
			this.normal = matrix3f;
		}
	}
}
