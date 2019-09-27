/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class VertexMultiConsumer
implements VertexConsumer {
    private final Iterable<VertexConsumer> delegates;

    public VertexMultiConsumer(ImmutableList<VertexConsumer> immutableList) {
        for (int i = 0; i < immutableList.size(); ++i) {
            for (int j = i + 1; j < immutableList.size(); ++j) {
                if (immutableList.get(i) != immutableList.get(j)) continue;
                throw new IllegalArgumentException("Duplicate delegates");
            }
        }
        this.delegates = immutableList;
    }

    @Override
    public VertexConsumer vertex(double d, double e, double f) {
        this.delegates.forEach(vertexConsumer -> vertexConsumer.vertex(d, e, f));
        return this;
    }

    @Override
    public VertexConsumer color(int i, int j, int k, int l) {
        this.delegates.forEach(vertexConsumer -> vertexConsumer.color(i, j, k, l));
        return this;
    }

    @Override
    public VertexConsumer uv(float f, float g) {
        this.delegates.forEach(vertexConsumer -> vertexConsumer.uv(f, g));
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int i, int j) {
        this.delegates.forEach(vertexConsumer -> vertexConsumer.uv2(i, j));
        return this;
    }

    @Override
    public VertexConsumer uv2(int i, int j) {
        this.delegates.forEach(vertexConsumer -> vertexConsumer.uv2(i, j));
        return this;
    }

    @Override
    public VertexConsumer normal(float f, float g, float h) {
        this.delegates.forEach(vertexConsumer -> vertexConsumer.normal(f, g, h));
        return this;
    }

    @Override
    public void endVertex() {
        this.delegates.forEach(VertexConsumer::endVertex);
    }

    @Override
    public void defaultOverlayCoords(int i, int j) {
        this.delegates.forEach(vertexConsumer -> vertexConsumer.defaultOverlayCoords(i, j));
    }

    @Override
    public void unsetDefaultOverlayCoords() {
        this.delegates.forEach(VertexConsumer::unsetDefaultOverlayCoords);
    }
}

