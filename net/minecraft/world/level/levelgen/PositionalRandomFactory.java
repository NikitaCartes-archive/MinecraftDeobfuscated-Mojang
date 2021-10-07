/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.RandomSource;

public interface PositionalRandomFactory {
    default public RandomSource at(BlockPos blockPos) {
        return this.at(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    default public RandomSource at(ResourceLocation resourceLocation) {
        return this.at(resourceLocation.toString());
    }

    public RandomSource at(int var1, int var2, int var3);

    public RandomSource at(String var1);
}

