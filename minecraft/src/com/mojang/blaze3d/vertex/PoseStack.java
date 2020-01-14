package com.mojang.blaze3d.vertex;

import com.google.common.collect.Queues;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
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
		PoseStack.Pose pose = (PoseStack.Pose)this.poseStack.getLast();
		pose.pose.multiply(Matrix4f.createTranslateMatrix((float)d, (float)e, (float)f));
	}

	public void scale(float f, float g, float h) {
		PoseStack.Pose pose = (PoseStack.Pose)this.poseStack.getLast();
		pose.pose.multiply(Matrix4f.createScaleMatrix(f, g, h));
		if (f == g && g == h) {
			if (f > 0.0F) {
				return;
			}

			pose.normal.mul(-1.0F);
		}

		float i = 1.0F / f;
		float j = 1.0F / g;
		float k = 1.0F / h;
		float l = Mth.fastInvCubeRoot(i * j * k);
		pose.normal.mul(Matrix3f.createScaleMatrix(l * i, l * j, l * k));
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

	public PoseStack.Pose last() {
		return (PoseStack.Pose)this.poseStack.getLast();
	}

	public boolean clear() {
		return this.poseStack.size() == 1;
	}

	@Environment(EnvType.CLIENT)
	public static final class Pose {
		private final Matrix4f pose;
		private final Matrix3f normal;

		private Pose(Matrix4f matrix4f, Matrix3f matrix3f) {
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
