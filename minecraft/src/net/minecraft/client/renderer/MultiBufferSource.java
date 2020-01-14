package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface MultiBufferSource {
	static MultiBufferSource.BufferSource immediate(BufferBuilder bufferBuilder) {
		return immediateWithBuffers(ImmutableMap.of(), bufferBuilder);
	}

	static MultiBufferSource.BufferSource immediateWithBuffers(Map<RenderType, BufferBuilder> map, BufferBuilder bufferBuilder) {
		return new MultiBufferSource.BufferSource(bufferBuilder, map);
	}

	VertexConsumer getBuffer(RenderType renderType);

	@Environment(EnvType.CLIENT)
	public static class BufferSource implements MultiBufferSource {
		protected final BufferBuilder builder;
		protected final Map<RenderType, BufferBuilder> fixedBuffers;
		protected Optional<RenderType> lastState = Optional.empty();
		protected final Set<BufferBuilder> startedBuffers = Sets.<BufferBuilder>newHashSet();

		protected BufferSource(BufferBuilder bufferBuilder, Map<RenderType, BufferBuilder> map) {
			this.builder = bufferBuilder;
			this.fixedBuffers = map;
		}

		@Override
		public VertexConsumer getBuffer(RenderType renderType) {
			Optional<RenderType> optional = renderType.asOptional();
			BufferBuilder bufferBuilder = this.getBuilderRaw(renderType);
			if (!Objects.equals(this.lastState, optional)) {
				if (this.lastState.isPresent()) {
					RenderType renderType2 = (RenderType)this.lastState.get();
					if (!this.fixedBuffers.containsKey(renderType2)) {
						this.endBatch(renderType2);
					}
				}

				if (this.startedBuffers.add(bufferBuilder)) {
					bufferBuilder.begin(renderType.mode(), renderType.format());
				}

				this.lastState = optional;
			}

			return bufferBuilder;
		}

		private BufferBuilder getBuilderRaw(RenderType renderType) {
			return (BufferBuilder)this.fixedBuffers.getOrDefault(renderType, this.builder);
		}

		public void endBatch() {
			this.lastState.ifPresent(renderTypex -> {
				VertexConsumer vertexConsumer = this.getBuffer(renderTypex);
				if (vertexConsumer == this.builder) {
					this.endBatch(renderTypex);
				}
			});

			for (RenderType renderType : this.fixedBuffers.keySet()) {
				this.endBatch(renderType);
			}
		}

		public void endBatch(RenderType renderType) {
			BufferBuilder bufferBuilder = this.getBuilderRaw(renderType);
			boolean bl = Objects.equals(this.lastState, renderType.asOptional());
			if (bl || bufferBuilder != this.builder) {
				if (this.startedBuffers.remove(bufferBuilder)) {
					renderType.end(bufferBuilder, 0, 0, 0);
					if (bl) {
						this.lastState = Optional.empty();
					}
				}
			}
		}
	}
}
