package com.mojang.blaze3d.vertex;

import com.google.common.collect.Queues;
import com.mojang.math.MatrixUtil;
import java.util.Deque;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
		if (Math.abs(f) == Math.abs(g) && Math.abs(g) == Math.abs(h)) {
			if (f < 0.0F || g < 0.0F || h < 0.0F) {
				pose.normal.scale(Math.signum(f), Math.signum(g), Math.signum(h));
			}
		} else {
			pose.normal.scale(1.0F / f, 1.0F / g, 1.0F / h);
			pose.trustedNormals = false;
		}
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
		this.poseStack.addLast(new PoseStack.Pose((PoseStack.Pose)this.poseStack.getLast()));
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
		pose.trustedNormals = true;
	}

	public void mulPose(Matrix4f matrix4f) {
		PoseStack.Pose pose = (PoseStack.Pose)this.poseStack.getLast();
		pose.pose.mul(matrix4f);
		if (!MatrixUtil.isPureTranslation(matrix4f)) {
			if (MatrixUtil.isOrthonormal(matrix4f)) {
				pose.normal.mul(new Matrix3f(matrix4f));
			} else {
				pose.computeNormalMatrix();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static final class Pose {
		final Matrix4f pose;
		final Matrix3f normal;
		boolean trustedNormals = true;

		Pose(Matrix4f matrix4f, Matrix3f matrix3f) {
			this.pose = matrix4f;
			this.normal = matrix3f;
		}

		Pose(PoseStack.Pose pose) {
			this.pose = new Matrix4f(pose.pose);
			this.normal = new Matrix3f(pose.normal);
			this.trustedNormals = pose.trustedNormals;
		}

		void computeNormalMatrix() {
			this.normal.set(this.pose).invert().transpose();
			this.trustedNormals = false;
		}

		public Matrix4f pose() {
			return this.pose;
		}

		public Matrix3f normal() {
			return this.normal;
		}

		public Vector3f transformNormal(Vector3f vector3f, Vector3f vector3f2) {
			return this.transformNormal(vector3f.x, vector3f.y, vector3f.z, vector3f2);
		}

		public Vector3f transformNormal(float f, float g, float h, Vector3f vector3f) {
			Vector3f vector3f2 = this.normal.transform(f, g, h, vector3f);
			return this.trustedNormals ? vector3f2 : vector3f2.normalize();
		}

		public PoseStack.Pose copy() {
			return new PoseStack.Pose(this);
		}
	}
}
