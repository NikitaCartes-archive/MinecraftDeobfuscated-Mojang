package com.mojang.blaze3d.vertex;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EntityOutlineGenerator extends DefaultedVertexConsumer {
	private final VertexConsumer delegate;

	public EntityOutlineGenerator(VertexConsumer vertexConsumer) {
		this.delegate = vertexConsumer;
	}

	public void setColor(int i, int j, int k, int l) {
		super.defaultColor(i, j, k, l);
	}

	@Override
	public void defaultColor(int i, int j, int k, int l) {
	}

	@Override
	public void defaultOverlayCoords(int i, int j) {
	}

	@Override
	public void unsetDefaultOverlayCoords() {
	}

	@Override
	public VertexConsumer vertex(double d, double e, double f) {
		this.delegate.vertex(d, e, f).color(this.defaultR, this.defaultG, this.defaultB, this.defaultA).endVertex();
		return this;
	}

	@Override
	public VertexConsumer color(int i, int j, int k, int l) {
		return this;
	}

	@Override
	public VertexConsumer uv(float f, float g) {
		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int i, int j) {
		return this;
	}

	@Override
	public VertexConsumer uv2(int i, int j) {
		return this;
	}

	@Override
	public VertexConsumer normal(float f, float g, float h) {
		return this;
	}

	@Override
	public void endVertex() {
	}
}
