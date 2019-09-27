/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public abstract class DefaultedVertexConsumer
implements VertexConsumer {
    protected boolean defaultColorSet = false;
    protected int defaultR = 255;
    protected int defaultG = 255;
    protected int defaultB = 255;
    protected int defaultA = 255;
    protected boolean defaultOverlayCoordsSet = false;
    protected int defaultOverlayU = 0;
    protected int defaultOverlayV = 10;

    public void defaultColor(int i, int j, int k, int l) {
        this.defaultR = i;
        this.defaultG = j;
        this.defaultB = k;
        this.defaultA = l;
        this.defaultColorSet = true;
    }

    @Override
    public void defaultOverlayCoords(int i, int j) {
        this.defaultOverlayU = i;
        this.defaultOverlayV = j;
        this.defaultOverlayCoordsSet = true;
    }

    @Override
    public void unsetDefaultOverlayCoords() {
        this.defaultOverlayCoordsSet = false;
    }
}

