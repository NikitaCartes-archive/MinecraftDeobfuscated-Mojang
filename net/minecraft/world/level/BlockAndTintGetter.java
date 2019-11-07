/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;

public interface BlockAndTintGetter
extends BlockGetter {
    public LevelLightEngine getLightEngine();

    @Environment(value=EnvType.CLIENT)
    public int getBlockTint(BlockPos var1, ColorResolver var2);

    default public int getBrightness(LightLayer lightLayer, BlockPos blockPos) {
        return this.getLightEngine().getLayerListener(lightLayer).getLightValue(blockPos);
    }

    default public int getRawBrightness(BlockPos blockPos, int i) {
        return this.getLightEngine().getRawBrightness(blockPos, i);
    }

    default public boolean canSeeSky(BlockPos blockPos) {
        return this.getBrightness(LightLayer.SKY, blockPos) >= this.getMaxLightLevel();
    }
}

