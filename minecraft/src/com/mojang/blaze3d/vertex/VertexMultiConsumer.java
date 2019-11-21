package com.mojang.blaze3d.vertex;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VertexMultiConsumer {
	public static VertexConsumer create(VertexConsumer vertexConsumer, VertexConsumer vertexConsumer2) {
		return new VertexMultiConsumer.Double(vertexConsumer, vertexConsumer2);
	}

	@Environment(EnvType.CLIENT)
	static class Double implements VertexConsumer {
		private final VertexConsumer first;
		private final VertexConsumer second;

		public Double(VertexConsumer vertexConsumer, VertexConsumer vertexConsumer2) {
			if (vertexConsumer == vertexConsumer2) {
				throw new IllegalArgumentException("Duplicate delegates");
			} else {
				this.first = vertexConsumer;
				this.second = vertexConsumer2;
			}
		}

		@Override
		public VertexConsumer vertex(double d, double e, double f) {
			this.first.vertex(d, e, f);
			this.second.vertex(d, e, f);
			return this;
		}

		@Override
		public VertexConsumer color(int i, int j, int k, int l) {
			this.first.color(i, j, k, l);
			this.second.color(i, j, k, l);
			return this;
		}

		@Override
		public VertexConsumer uv(float f, float g) {
			this.first.uv(f, g);
			this.second.uv(f, g);
			return this;
		}

		@Override
		public VertexConsumer overlayCoords(int i, int j) {
			this.first.overlayCoords(i, j);
			this.second.overlayCoords(i, j);
			return this;
		}

		@Override
		public VertexConsumer uv2(int i, int j) {
			this.first.uv2(i, j);
			this.second.uv2(i, j);
			return this;
		}

		@Override
		public VertexConsumer normal(float f, float g, float h) {
			this.first.normal(f, g, h);
			this.second.normal(f, g, h);
			return this;
		}

		@Override
		public void vertex(float f, float g, float h, float i, float j, float k, float l, float m, float n, int o, int p, float q, float r, float s) {
			this.first.vertex(f, g, h, i, j, k, l, m, n, o, p, q, r, s);
			this.second.vertex(f, g, h, i, j, k, l, m, n, o, p, q, r, s);
		}

		@Override
		public void endVertex() {
			this.first.endVertex();
			this.second.endVertex();
		}
	}
}
