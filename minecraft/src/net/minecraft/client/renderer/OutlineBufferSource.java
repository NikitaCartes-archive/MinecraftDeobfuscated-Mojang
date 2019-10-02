package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class OutlineBufferSource implements MultiBufferSource {
	private final MultiBufferSource.BufferSource bufferSource;
	private final MultiBufferSource.BufferSource outlineBufferSource = MultiBufferSource.immediate(new BufferBuilder(256));
	private int teamR = 255;
	private int teamG = 255;
	private int teamB = 255;
	private int teamA = 255;

	public OutlineBufferSource(MultiBufferSource.BufferSource bufferSource) {
		this.bufferSource = bufferSource;
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		VertexConsumer vertexConsumer = this.bufferSource.getBuffer(renderType);
		Optional<ResourceLocation> optional = renderType.outlineTexture();
		if (optional.isPresent()) {
			VertexConsumer vertexConsumer2 = this.outlineBufferSource.getBuffer(RenderType.OUTLINE((ResourceLocation)optional.get()));
			OutlineBufferSource.EntityOutlineGenerator entityOutlineGenerator = new OutlineBufferSource.EntityOutlineGenerator(
				vertexConsumer2, this.teamR, this.teamG, this.teamB, this.teamA
			);
			return new VertexMultiConsumer(ImmutableList.of(entityOutlineGenerator, vertexConsumer));
		} else {
			return vertexConsumer;
		}
	}

	public void setColor(int i, int j, int k, int l) {
		this.teamR = i;
		this.teamG = j;
		this.teamB = k;
		this.teamA = l;
	}

	public void endOutlineBatch() {
		this.outlineBufferSource.endBatch();
	}

	@Environment(EnvType.CLIENT)
	static class EntityOutlineGenerator extends DefaultedVertexConsumer {
		private final VertexConsumer delegate;
		private double x;
		private double y;
		private double z;
		private float u;
		private float v;

		private EntityOutlineGenerator(VertexConsumer vertexConsumer, int i, int j, int k, int l) {
			this.delegate = vertexConsumer;
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
			this.x = d;
			this.y = e;
			this.z = f;
			return this;
		}

		@Override
		public VertexConsumer color(int i, int j, int k, int l) {
			return this;
		}

		@Override
		public VertexConsumer uv(float f, float g) {
			this.u = f;
			this.v = g;
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
			this.delegate.vertex(this.x, this.y, this.z).color(this.defaultR, this.defaultG, this.defaultB, this.defaultA).uv(this.u, this.v).endVertex();
		}
	}
}
