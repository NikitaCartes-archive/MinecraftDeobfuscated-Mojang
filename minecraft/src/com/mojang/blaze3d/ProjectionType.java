package com.mojang.blaze3d;

import com.mojang.blaze3d.vertex.VertexSorting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public enum ProjectionType {
	PERSPECTIVE(VertexSorting.DISTANCE_TO_ORIGIN, (matrix4f, f) -> matrix4f.scale(1.0F - f / 4096.0F)),
	ORTHOGRAPHIC(VertexSorting.ORTHOGRAPHIC_Z, (matrix4f, f) -> matrix4f.translate(0.0F, 0.0F, f / 512.0F));

	private final VertexSorting vertexSorting;
	private final ProjectionType.LayeringTransform layeringTransform;

	private ProjectionType(final VertexSorting vertexSorting, final ProjectionType.LayeringTransform layeringTransform) {
		this.vertexSorting = vertexSorting;
		this.layeringTransform = layeringTransform;
	}

	public VertexSorting vertexSorting() {
		return this.vertexSorting;
	}

	public void applyLayeringTransform(Matrix4f matrix4f, float f) {
		this.layeringTransform.apply(matrix4f, f);
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	interface LayeringTransform {
		void apply(Matrix4f matrix4f, float f);
	}
}
