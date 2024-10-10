package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;
import java.util.HashMap;
import java.util.Map;
import java.util.SequencedMap;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface MultiBufferSource {
	static MultiBufferSource.BufferSource immediate(ByteBufferBuilder byteBufferBuilder) {
		return immediateWithBuffers(Object2ObjectSortedMaps.<RenderType, ByteBufferBuilder>emptyMap(), byteBufferBuilder);
	}

	static MultiBufferSource.BufferSource immediateWithBuffers(SequencedMap<RenderType, ByteBufferBuilder> sequencedMap, ByteBufferBuilder byteBufferBuilder) {
		return new MultiBufferSource.BufferSource(byteBufferBuilder, sequencedMap);
	}

	VertexConsumer getBuffer(RenderType renderType);

	@Environment(EnvType.CLIENT)
	public static class BufferSource implements MultiBufferSource {
		protected final ByteBufferBuilder sharedBuffer;
		protected final SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers;
		protected final Map<RenderType, BufferBuilder> startedBuilders = new HashMap();
		@Nullable
		protected RenderType lastSharedType;

		protected BufferSource(ByteBufferBuilder byteBufferBuilder, SequencedMap<RenderType, ByteBufferBuilder> sequencedMap) {
			this.sharedBuffer = byteBufferBuilder;
			this.fixedBuffers = sequencedMap;
		}

		@Override
		public VertexConsumer getBuffer(RenderType renderType) {
			BufferBuilder bufferBuilder = (BufferBuilder)this.startedBuilders.get(renderType);
			if (bufferBuilder != null && !renderType.canConsolidateConsecutiveGeometry()) {
				this.endBatch(renderType, bufferBuilder);
				bufferBuilder = null;
			}

			if (bufferBuilder != null) {
				return bufferBuilder;
			} else {
				ByteBufferBuilder byteBufferBuilder = (ByteBufferBuilder)this.fixedBuffers.get(renderType);
				if (byteBufferBuilder != null) {
					bufferBuilder = new BufferBuilder(byteBufferBuilder, renderType.mode(), renderType.format());
				} else {
					if (this.lastSharedType != null) {
						this.endBatch(this.lastSharedType);
					}

					bufferBuilder = new BufferBuilder(this.sharedBuffer, renderType.mode(), renderType.format());
					this.lastSharedType = renderType;
				}

				this.startedBuilders.put(renderType, bufferBuilder);
				return bufferBuilder;
			}
		}

		public void endLastBatch() {
			if (this.lastSharedType != null) {
				this.endBatch(this.lastSharedType);
				this.lastSharedType = null;
			}
		}

		public void endBatch() {
			this.endLastBatch();

			for (RenderType renderType : this.fixedBuffers.keySet()) {
				this.endBatch(renderType);
			}
		}

		public void endBatch(RenderType renderType) {
			BufferBuilder bufferBuilder = (BufferBuilder)this.startedBuilders.remove(renderType);
			if (bufferBuilder != null) {
				this.endBatch(renderType, bufferBuilder);
			}
		}

		private void endBatch(RenderType renderType, BufferBuilder bufferBuilder) {
			MeshData meshData = bufferBuilder.build();
			if (meshData != null) {
				if (renderType.sortOnUpload()) {
					ByteBufferBuilder byteBufferBuilder = (ByteBufferBuilder)this.fixedBuffers.getOrDefault(renderType, this.sharedBuffer);
					meshData.sortQuads(byteBufferBuilder, RenderSystem.getProjectionType().vertexSorting());
				}

				renderType.draw(meshData);
			}

			if (renderType.equals(this.lastSharedType)) {
				this.lastSharedType = null;
			}
		}
	}
}
