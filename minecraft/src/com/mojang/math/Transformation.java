package com.mojang.math;

import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.Util;
import org.apache.commons.lang3.tuple.Triple;

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
			matrix4f.multiply(Matrix4f.createScaleMatrix(vector3f2.x(), vector3f2.y(), vector3f2.z()));
		}

		if (quaternion2 != null) {
			matrix4f.multiply(new Matrix4f(quaternion2));
		}

		if (vector3f != null) {
			matrix4f.m03 = vector3f.x();
			matrix4f.m13 = vector3f.y();
			matrix4f.m23 = vector3f.z();
		}

		return matrix4f;
	}

	public static Pair<Matrix3f, Vector3f> toAffine(Matrix4f matrix4f) {
		matrix4f.multiply(1.0F / matrix4f.m33);
		Vector3f vector3f = new Vector3f(matrix4f.m03, matrix4f.m13, matrix4f.m23);
		Matrix3f matrix3f = new Matrix3f(matrix4f);
		return Pair.of(matrix3f, vector3f);
	}

	public Matrix4f getMatrix() {
		return this.matrix.copy();
	}

	public Vector3f getTranslation() {
		this.ensureDecomposed();
		return this.translation.copy();
	}

	public Quaternion getLeftRotation() {
		this.ensureDecomposed();
		return this.leftRotation.copy();
	}

	public Vector3f getScale() {
		this.ensureDecomposed();
		return this.scale.copy();
	}

	public Quaternion getRightRotation() {
		this.ensureDecomposed();
		return this.rightRotation.copy();
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

	public Transformation slerp(Transformation transformation, float f) {
		Vector3f vector3f = this.getTranslation();
		Quaternion quaternion = this.getLeftRotation();
		Vector3f vector3f2 = this.getScale();
		Quaternion quaternion2 = this.getRightRotation();
		vector3f.lerp(transformation.getTranslation(), f);
		quaternion.slerp(transformation.getLeftRotation(), f);
		vector3f2.lerp(transformation.getScale(), f);
		quaternion2.slerp(transformation.getRightRotation(), f);
		return new Transformation(vector3f, quaternion, vector3f2, quaternion2);
	}
}
