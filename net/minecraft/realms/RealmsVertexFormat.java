/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.RealmsVertexFormatElement;

@Environment(value=EnvType.CLIENT)
public class RealmsVertexFormat {
    private VertexFormat v;

    public RealmsVertexFormat(VertexFormat vertexFormat) {
        this.v = vertexFormat;
    }

    public RealmsVertexFormat from(VertexFormat vertexFormat) {
        this.v = vertexFormat;
        return this;
    }

    public VertexFormat getVertexFormat() {
        return this.v;
    }

    public void clear() {
        this.v.clear();
    }

    public RealmsVertexFormat addElement(RealmsVertexFormatElement realmsVertexFormatElement) {
        return this.from(this.v.addElement(realmsVertexFormatElement.getVertexFormatElement()));
    }

    public List<RealmsVertexFormatElement> getElements() {
        ArrayList<RealmsVertexFormatElement> list = Lists.newArrayList();
        for (VertexFormatElement vertexFormatElement : this.v.getElements()) {
            list.add(new RealmsVertexFormatElement(vertexFormatElement));
        }
        return list;
    }

    public boolean equals(Object object) {
        return this.v.equals(object);
    }

    public int hashCode() {
        return this.v.hashCode();
    }

    public String toString() {
        return this.v.toString();
    }
}

