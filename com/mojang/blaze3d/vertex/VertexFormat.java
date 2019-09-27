/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class VertexFormat {
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<VertexFormatElement> elements = Lists.newArrayList();
    private final IntList offsets = new IntArrayList();
    private int vertexSize;

    public VertexFormat(VertexFormat vertexFormat) {
        this();
        for (int i = 0; i < vertexFormat.getElementCount(); ++i) {
            this.addElement(vertexFormat.getElement(i));
        }
        this.vertexSize = vertexFormat.getVertexSize();
    }

    public VertexFormat() {
    }

    public void clear() {
        this.elements.clear();
        this.offsets.clear();
        this.vertexSize = 0;
    }

    public VertexFormat addElement(VertexFormatElement vertexFormatElement) {
        if (vertexFormatElement.isPosition() && this.hasPositionElement()) {
            LOGGER.warn("VertexFormat error: Trying to add a position VertexFormatElement when one already exists, ignoring.");
            return this;
        }
        this.elements.add(vertexFormatElement);
        this.offsets.add(this.vertexSize);
        this.vertexSize += vertexFormatElement.getByteSize();
        return this;
    }

    public String toString() {
        return "format: " + this.elements.size() + " elements: " + this.elements.stream().map(Object::toString).collect(Collectors.joining(" "));
    }

    private boolean hasPositionElement() {
        return this.elements.stream().anyMatch(VertexFormatElement::isPosition);
    }

    public int getIntegerSize() {
        return this.getVertexSize() / 4;
    }

    public int getVertexSize() {
        return this.vertexSize;
    }

    public List<VertexFormatElement> getElements() {
        return this.elements;
    }

    public int getElementCount() {
        return this.elements.size();
    }

    public VertexFormatElement getElement(int i) {
        return this.elements.get(i);
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
        if (!this.elements.equals(vertexFormat.elements)) {
            return false;
        }
        return this.offsets.equals(vertexFormat.offsets);
    }

    public int hashCode() {
        int i = this.elements.hashCode();
        i = 31 * i + this.offsets.hashCode();
        i = 31 * i + this.vertexSize;
        return i;
    }

    public void setupBufferState(long l) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this.setupBufferState(l));
            return;
        }
        int i = this.getVertexSize();
        List<VertexFormatElement> list = this.getElements();
        for (int j = 0; j < list.size(); ++j) {
            list.get(j).setupBufferState(l + (long)this.offsets.getInt(j), i);
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

