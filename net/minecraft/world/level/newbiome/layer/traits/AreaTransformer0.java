/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface AreaTransformer0 {
    default public <R extends Area> AreaFactory<R> run(BigContext<R> bigContext) {
        return () -> bigContext.createResult((i, j) -> {
            bigContext.initRandom(i, j);
            return this.applyPixel(bigContext, i, j);
        });
    }

    public int applyPixel(Context var1, int var2, int var3);
}

