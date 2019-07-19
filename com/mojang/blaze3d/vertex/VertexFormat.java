/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class VertexFormat {
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<VertexFormatElement> elements = Lists.newArrayList();
    private final List<Integer> offsets = Lists.newArrayList();
    private int vertexSize;
    private int colorOffset = -1;
    private final List<Integer> texOffset = Lists.newArrayList();
    private int normalOffset = -1;

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
        this.colorOffset = -1;
        this.texOffset.clear();
        this.normalOffset = -1;
        this.vertexSize = 0;
    }

    public VertexFormat addElement(VertexFormatElement vertexFormatElement) {
        if (vertexFormatElement.isPosition() && this.hasPositionElement()) {
            LOGGER.warn("VertexFormat error: Trying to add a position VertexFormatElement when one already exists, ignoring.");
            return this;
        }
        this.elements.add(vertexFormatElement);
        this.offsets.add(this.vertexSize);
        switch (vertexFormatElement.getUsage()) {
            case NORMAL: {
                this.normalOffset = this.vertexSize;
                break;
            }
            case COLOR: {
                this.colorOffset = this.vertexSize;
                break;
            }
            case UV: {
                this.texOffset.add(vertexFormatElement.getIndex(), this.vertexSize);
                break;
            }
        }
        this.vertexSize += vertexFormatElement.getByteSize();
        return this;
    }

    public boolean hasNormal() {
        return this.normalOffset >= 0;
    }

    public int getNormalOffset() {
        return this.normalOffset;
    }

    public boolean hasColor() {
        return this.colorOffset >= 0;
    }

    public int getColorOffset() {
        return this.colorOffset;
    }

    public boolean hasUv(int i) {
        return this.texOffset.size() - 1 >= i;
    }

    public int getUvOffset(int i) {
        return this.texOffset.get(i);
    }

    public String toString() {
        String string = "format: " + this.elements.size() + " elements: ";
        for (int i = 0; i < this.elements.size(); ++i) {
            string = string + this.elements.get(i).toString();
            if (i == this.elements.size() - 1) continue;
            string = string + " ";
        }
        return string;
    }

    private boolean hasPositionElement() {
        int j = this.elements.size();
        for (int i = 0; i < j; ++i) {
            VertexFormatElement vertexFormatElement = this.elements.get(i);
            if (!vertexFormatElement.isPosition()) continue;
            return true;
        }
        return false;
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

    public int getOffset(int i) {
        return this.offsets.get(i);
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
}

