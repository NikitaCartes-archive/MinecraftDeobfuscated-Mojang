package com.mojang.math;

import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import org.apache.commons.lang3.tuple.Triple;

@Environment(EnvType.CLIENT)
public final class Transformation {
	private final Matrix4f matrix;
	private boolean decomposed;
	@Nullable
	private Vector3f translation;
	@Nullable
	private Quaternion leftRotation;
	@Nullable
	private Vector3f scale;
	@Nullable
	private Quaternion rightRotation;
	private static final Transformation IDENTITY = Util.make(() -> {
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.setIdentity();
		Transformation transformation = new Transformation(matrix4f);
		transformation.getLeftRotation();
		return transformation;
	});

	public Transformation(@Nullable Matrix4f matrix4f) {
		if (matrix4f == null) {
			this.matrix = IDENTITY.matrix;
		} else {
			this.matrix = matrix4f;
		}
	}

	public Transformation(@Nullable Vector3f vector3f, @Nullable Quaternion quaternion, @Nullable Vector3f vector3f2, @Nullable Quaternion quaternion2) {
		this.matrix = compose(vector3f, quaternion, vector3f2, quaternion2);
		this.translation = vector3f != null ? vector3f : new Vector3f();
		this.leftRotation = quaternion != null ? quaternion : Quaternion.ONE.copy();
		this.scale = vector3f2 != null ? vector3f2 : new Vector3f(1.0F, 1.0F, 1.0F);
		this.rightRotation = quaternion2 != null ? quaternion2 : Quaternion.ONE.copy();
		this.decomposed = true;
	}

	public static Transformation identity() {
		return IDENTITY;
	}

	public Transformation compose(Transformation transformation) {
		Matrix4f matrix4f = this.getMatrix();
		matrix4f.multiply(transformation.getMatrix());
		return new Transformation(matrix4f);
	}

	@Nullable
	public Transformation inverse() {
		if (this == IDENTITY) {
			return this;
		} else {
			Matrix4f matrix4f = this.getMatrix();
			return matrix4f.invert() ? new Transformation(matrix4f) : null;
		}
	}

	private void ensureDecomposed() {
		if (!this.decomposed) {
			Pair<Matrix3f, Vector3f> pair = toAffine(this.matrix);
			Triple<Quaternion, Vector3f, Quaternion> triple = pair.getFirst().svdDecompose();
			this.translation = pair.getSecond();
			this.leftRotation = triple.getLeft();
			this.scale = triple.getMiddle();
			this.rightRotation = triple.getRight();
			this.decomposed = true;
		}
	}

	private static Matrix4f compose(@Nullable Vector3f vector3f, @Nullable Quaternion quaternion, @Nullable Vector3f vector3f2, @Nullable Quaternion quaternion2) {
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.setIdentity();
		if (quaternion != null) {
			matrix4f.multiply(new Matrix4f(quaternion));
		}

		if (vector3f2 != null) {
			Matrix4f matrix4f2 = new Matrix4f();
			matrix4f2.setIdentity();
			matrix4f2.set(0, 0, vector3f2.x());
			matrix4f2.set(1, 1, vector3f2.y());
			matrix4f2.set(2, 2, vector3f2.z());
			matrix4f.multiply(matrix4f2);
		}

		if (quaternion2 != null) {
			matrix4f.multiply(new Matrix4f(quaternion2));
		}

		if (vector3f != null) {
			matrix4f.set(0, 3, vector3f.x());
			matrix4f.set(1, 3, vector3f.y());
			matrix4f.set(2, 3, vector3f.z());
		}

		return matrix4f;
	}

	public static Pair<Matrix3f, Vector3f> toAffine(Matrix4f matrix4f) {
		matrix4f.multiply(1.0F / matrix4f.get(3, 3));
		Vector3f vector3f = new Vector3f(matrix4f.get(0, 3), matrix4f.get(1, 3), matrix4f.get(2, 3));
		Matrix3f matrix3f = new Matrix3f(matrix4f);
		return Pair.of(matrix3f, vector3f);
	}

	public Matrix4f getMatrix() {
		return this.matrix.copy();
	}

	public Quaternion getLeftRotation() {
		this.ensureDecomposed();
		return this.leftRotation.copy();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			Transformation transformation = (Transformation)object;
			return Objects.equals(this.matrix, transformation.matrix);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.matrix});
	}
}
