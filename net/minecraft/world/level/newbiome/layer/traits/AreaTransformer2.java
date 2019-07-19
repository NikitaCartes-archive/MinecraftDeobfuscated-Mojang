/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.DimensionTransformer;

public interface AreaTransformer2
extends DimensionTransformer {
    default public <R extends Area> AreaFactory<R> run(BigContext<R> bigContext, AreaFactory<R> areaFactory, AreaFactory<R> areaFactory2) {
        return () -> {
            Object area = areaFactory.make();
            Object area2 = areaFactory2.make();
            return bigContext.createResult((i, j) -> {
                bigContext.initRandom(i, j);
                return this.applyPixel(bigContext, (Area)area, (Area)area2, i, j);
            }, area, area2);
        };
    }

    public int applyPixel(Context var1, Area var2, Area var3, int var4, int var5);
}

