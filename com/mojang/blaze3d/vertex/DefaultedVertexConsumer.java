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

    public void defaultColor(int i, int j, int k, int l) {
        this.defaultR = i;
        this.defaultG = j;
        this.defaultB = k;
        this.defaultA = l;
        this.defaultColorSet = true;
    }
}

