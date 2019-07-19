/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.context;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.PixelTransformer;

public interface BigContext<R extends Area>
extends Context {
    public void initRandom(long var1, long var3);

    public R createResult(PixelTransformer var1);

    default public R createResult(PixelTransformer pixelTransformer, R area) {
        return this.createResult(pixelTransformer);
    }

    default public R createResult(PixelTransformer pixelTransformer, R area, R area2) {
        return this.createResult(pixelTransformer);
    }

    default public int random(int i, int j) {
        return this.nextRandom(2) == 0 ? i : j;
    }

    default public int random(int i, int j, int k, int l) {
        int m = this.nextRandom(4);
        if (m == 0) {
            return i;
        }
        if (m == 1) {
            return j;
        }
        if (m == 2) {
            return k;
        }
        return l;
    }
}

