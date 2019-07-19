/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset0Transformer;

public interface C0Transformer
extends AreaTransformer1,
DimensionOffset0Transformer {
    public int apply(Context var1, int var2);

    @Override
    default public int applyPixel(BigContext<?> bigContext, Area area, int i, int j) {
        return this.apply(bigContext, area.get(this.getParentX(i), this.getParentY(j)));
    }
}

