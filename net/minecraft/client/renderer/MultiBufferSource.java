/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.client.renderer.RenderType;

@Environment(value=EnvType.CLIENT)
public interface MultiBufferSource {
    public static BufferSource immediate(BufferBuilder bufferBuilder) {
        return MultiBufferSource.immediateWithBuffers(ImmutableMap.of(), bufferBuilder);
    }

    public static BufferSource immediateWithBuffers(Map<RenderType, BufferBuilder> map, BufferBuilder bufferBuilder) {
        return new BufferSource(bufferBuilder, map);
    }

    public VertexConsumer getBuffer(RenderType var1);

    @Environment(value=EnvType.CLIENT)
    public static class BufferSource
    implements MultiBufferSource {
        protected final BufferBuilder builder;
        protected final Map<RenderType, BufferBuilder> fixedBuffers;
        protected Optional<RenderType> lastState = Optional.empty();
        protected final Set<BufferBuilder> startedBuffers = Sets.newHashSet();

        protected BufferSource(BufferBuilder bufferBuilder, Map<RenderType, BufferBuilder> map) {
            this.builder = bufferBuilder;
            this.fixedBuffers = map;
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            Optional<RenderType> optional = renderType.asOptional();
            BufferBuilder bufferBuilder = this.getBuilderRaw(renderType);
            if (!Objects.equals(this.lastState, optional) || !renderType.canConsolidateConsecutiveGeometry()) {
                RenderType renderType2;
                if (this.lastState.isPresent() && !this.fixedBuffers.containsKey(renderType2 = this.lastState.get())) {
                    this.endBatch(renderType2);
                }
                if (this.startedBuffers.add(bufferBuilder)) {
                    bufferBuilder.begin(renderType.mode(), renderType.format());
                }
                this.lastState = optional;
            }
            return bufferBuilder;
        }

        private BufferBuilder getBuilderRaw(RenderType renderType) {
            return this.fixedBuffers.getOrDefault(renderType, this.builder);
        }

        public void endLastBatch() {
            if (this.lastState.isPresent()) {
                RenderType renderType = this.lastState.get();
                if (!this.fixedBuffers.containsKey(renderType)) {
                    this.endBatch(renderType);
                }
                this.lastState = Optional.empty();
            }
        }

        public void endBatch() {
            this.lastState.ifPresent(renderType -> {
                VertexConsumer vertexConsumer = this.getBuffer((RenderType)renderType);
                if (vertexConsumer == this.builder) {
                    this.endBatch((RenderType)renderType);
                }
            });
            for (RenderType renderType2 : this.fixedBuffers.keySet()) {
                this.endBatch(renderType2);
            }
        }

        public void endBatch(RenderType renderType) {
            BufferBuilder bufferBuilder = this.getBuilderRaw(renderType);
            boolean bl = Objects.equals(this.lastState, renderType.asOptional());
            if (!bl && bufferBuilder == this.builder) {
                return;
            }
            if (!this.startedBuffers.remove(bufferBuilder)) {
                return;
            }
            renderType.end(bufferBuilder, 0, 0, 0);
            if (bl) {
                this.lastState = Optional.empty();
            }
        }
    }
}

