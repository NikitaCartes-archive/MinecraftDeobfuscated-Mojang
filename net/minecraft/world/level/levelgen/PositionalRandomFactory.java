/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public interface PositionalRandomFactory {
    default public RandomSource at(BlockPos blockPos) {
        return this.at(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    default public RandomSource fromHashOf(ResourceLocation resourceLocation) {
        return this.fromHashOf(resourceLocation.toString());
    }

    public RandomSource fromHashOf(String var1);

    public RandomSource at(int var1, int var2, int var3);

    @VisibleForTesting
    public void parityConfigString(StringBuilder var1);
}

