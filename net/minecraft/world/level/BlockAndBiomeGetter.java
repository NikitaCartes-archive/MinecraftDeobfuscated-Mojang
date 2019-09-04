/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;

public interface BlockAndBiomeGetter
extends BlockGetter {
    public BiomeManager getBiomeManager();

    public LevelLightEngine getLightEngine();

    default public Biome getBiome(BlockPos blockPos) {
        return this.getBiomeManager().getBiome(blockPos);
    }

    default public int getBrightness(LightLayer lightLayer, BlockPos blockPos) {
        return this.getLightEngine().getLayerListener(lightLayer).getLightValue(blockPos);
    }

    default public int getRawBrightness(BlockPos blockPos, int i) {
        return this.getLightEngine().getRawBrightness(blockPos, i);
    }

    default public boolean canSeeSky(BlockPos blockPos) {
        return this.getBrightness(LightLayer.SKY, blockPos) >= this.getMaxLightLevel();
    }

    @Environment(value=EnvType.CLIENT)
    default public int getLightColor(BlockPos blockPos) {
        return this.getLightColor(this.getBlockState(blockPos), blockPos);
    }

    @Environment(value=EnvType.CLIENT)
    default public int getLightColor(BlockState blockState, BlockPos blockPos) {
        int k;
        if (blockState.emissiveRendering()) {
            return 0xF000F0;
        }
        int i = this.getBrightness(LightLayer.SKY, blockPos);
        int j = this.getBrightness(LightLayer.BLOCK, blockPos);
        if (j < (k = blockState.getLightEmission())) {
            j = k;
        }
        return i << 20 | j << 4;
    }
}

