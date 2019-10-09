/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class VertexFormat {
    private final ImmutableList<VertexFormatElement> elements;
    private final IntList offsets = new IntArrayList();
    private final int vertexSize;

    public VertexFormat(ImmutableList<VertexFormatElement> immutableList) {
        this.elements = immutableList;
        int i = 0;
        for (VertexFormatElement vertexFormatElement : immutableList) {
            this.offsets.add(i);
            i += vertexFormatElement.getByteSize();
        }
        this.vertexSize = i;
    }

    public String toString() {
        return "format: " + this.elements.size() + " elements: " + this.elements.stream().map(Object::toString).collect(Collectors.joining(" "));
    }

    public int getIntegerSize() {
        return this.getVertexSize() / 4;
    }

    public int getVertexSize() {
        return this.vertexSize;
    }

    public ImmutableList<VertexFormatElement> getElements() {
        return this.elements;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        VertexFormat vertexFormat = (VertexFormat)object;
        if (this.vertexSize != vertexFormat.vertexSize) {
            return false;
        }
        return this.elements.equals(vertexFormat.elements);
    }

    public int hashCode() {
        return this.elements.hashCode();
    }

    public void setupBufferState(long l) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this.setupBufferState(l));
            return;
        }
        int i = this.getVertexSize();
        ImmutableList<VertexFormatElement> list = this.getElements();
        for (int j = 0; j < list.size(); ++j) {
            ((VertexFormatElement)list.get(j)).setupBufferState(l + (long)this.offsets.getInt(j), i);
        }
    }

    public void clearBufferState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::clearBufferState);
            return;
        }
        for (VertexFormatElement vertexFormatElement : this.getElements()) {
            vertexFormatElement.clearBufferState();
        }
    }
}

