package com.mojang.blaze3d.vertex;

import com.google.common.collect.Queues;
import java.util.Deque;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class PoseStack {
	private final Deque<PoseStack.Pose> poseStack = Util.make(Queues.<PoseStack.Pose>newArrayDeque(), arrayDeque -> {
		Matrix4f matrix4f = new Matrix4f();
		Matrix3f matrix3f = new Matrix3f();
		arrayDeque.add(new PoseStack.Pose(matrix4f, matrix3f));
	});

	public void translate(double d, double e, double f) {
		this.translate((float)d, (float)e, (float)f);
	}

	public void translate(float f, float g, float h) {
		PoseStack.Pose pose = (PoseStack.Pose)this.poseStack.getLast();
		pose.pose.translate(f, g, h);
	}

	public void scale(float f, float g, float h) {
		PoseStack.Pose pose = (PoseStack.Pose)this.poseStack.getLast();
		pose.pose.scale(f, g, h);
		if (f == g && g == h) {
			if (f > 0.0F) {
				return;
			}

			pose.normal.scale(-1.0F);
		}

		float i = 1.0F / f;
		float j = 1.0F / g;
		float k = 1.0F / h;
		float l = Mth.fastInvCubeRoot(i * j * k);
		pose.normal.scale(l * i, l * j, l * k);
	}

	public void mulPose(Quaternionf quaternionf) {
		PoseStack.Pose pose = (PoseStack.Pose)this.poseStack.getLast();
		pose.pose.rotate(quaternionf);
		pose.normal.rotate(quaternionf);
	}

	public void rotateAround(Quaternionf quaternionf, float f, float g, float h) {
		PoseStack.Pose pose = (PoseStack.Pose)this.poseStack.getLast();
		pose.pose.rotateAround(quaternionf, f, g, h);
		pose.normal.rotate(quaternionf);
	}

	public void pushPose() {
		PoseStack.Pose pose = (PoseStack.Pose)this.poseStack.getLast();
		this.poseStack.addLast(new PoseStack.Pose(new Matrix4f(pose.pose), new Matrix3f(pose.normal)));
	}

	public void popPose() {
		this.poseStack.removeLast();
	}

	public PoseStack.Pose last() {
		return (PoseStack.Pose)this.poseStack.getLast();
	}

	public boolean clear() {
		return this.poseStack.size() == 1;
	}

	public void setIdentity() {
		PoseStack.Pose pose = (PoseStack.Pose)this.poseStack.getLast();
		pose.pose.identity();
		pose.normal.identity();
	}

	public void mulPoseMatrix(Matrix4f matrix4f) {
		((PoseStack.Pose)this.poseStack.getLast()).pose.mul(matrix4f);
	}

	@Environment(EnvType.CLIENT)
	public static final class Pose {
		final Matrix4f pose;
		final Matrix3f normal;

		Pose(Matrix4f matrix4f, Matrix3f matrix3f) {
			this.pose = matrix4f;
			this.normal = matrix3f;
		}

		public Matrix4f pose() {
			return this.pose;
		}

		public Matrix3f normal() {
			return this.normal;
		}
	}
}
