/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset1Transformer;

public interface CastleTransformer
extends AreaTransformer1,
DimensionOffset1Transformer {
    public int apply(Context var1, int var2, int var3, int var4, int var5, int var6);

    @Override
    default public int applyPixel(BigContext<?> bigContext, Area area, int i, int j) {
        return this.apply(bigContext, area.get(this.getParentX(i + 1), this.getParentY(j + 0)), area.get(this.getParentX(i + 2), this.getParentY(j + 1)), area.get(this.getParentX(i + 1), this.getParentY(j + 2)), area.get(this.getParentX(i + 0), this.getParentY(j + 1)), area.get(this.getParentX(i + 1), this.getParentY(j + 1)));
    }
}

