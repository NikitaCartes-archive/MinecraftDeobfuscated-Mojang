/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.layer.traits.DimensionTransformer;

public interface AreaTransformer1
extends DimensionTransformer {
    default public <R extends Area> AreaFactory<R> run(BigContext<R> bigContext, AreaFactory<R> areaFactory) {
        return () -> {
            Object area = areaFactory.make();
            return bigContext.createResult((i, j) -> {
                bigContext.initRandom(i, j);
                return this.applyPixel(bigContext, (Area)area, i, j);
            }, area);
        };
    }

    public int applyPixel(BigContext<?> var1, Area var2, int var3, int var4);
}

