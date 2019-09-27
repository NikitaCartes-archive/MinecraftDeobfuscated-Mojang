package com.mojang.blaze3d.vertex;

import com.google.common.collect.Queues;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Deque;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;

@Environment(EnvType.CLIENT)
public class PoseStack {
	private final Deque<Matrix4f> poseStack = Util.make(Queues.<Matrix4f>newArrayDeque(), arrayDeque -> {
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.setIdentity();
		arrayDeque.add(matrix4f);
	});

	public void translate(double d, double e, double f) {
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.setIdentity();
		matrix4f.translate(new Vector3f((float)d, (float)e, (float)f));
		this.mulPose(matrix4f);
	}

	public void scale(float f, float g, float h) {
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.setIdentity();
		matrix4f.set(0, 0, f);
		matrix4f.set(1, 1, g);
		matrix4f.set(2, 2, h);
		this.mulPose(matrix4f);
	}

	public void mulPose(Matrix4f matrix4f) {
		Matrix4f matrix4f2 = (Matrix4f)this.poseStack.getLast();
		matrix4f2.multiply(matrix4f);
	}

	public void mulPose(Quaternion quaternion) {
		Matrix4f matrix4f = (Matrix4f)this.poseStack.getLast();
		matrix4f.multiply(quaternion);
	}

	public void pushPose() {
		this.poseStack.addLast(((Matrix4f)this.poseStack.getLast()).copy());
	}

	public void popPose() {
		this.poseStack.removeLast();
	}

	public Matrix4f getPose() {
		return (Matrix4f)this.poseStack.getLast();
	}

	public boolean clear() {
		return this.poseStack.size() == 1;
	}
}
