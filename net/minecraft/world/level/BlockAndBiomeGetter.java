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

public interface BlockAndBiomeGetter
extends BlockGetter {
    public Biome getBiome(BlockPos var1);

    public int getBrightness(LightLayer var1, BlockPos var2);

    default public boolean canSeeSky(BlockPos blockPos) {
        return this.getBrightness(LightLayer.SKY, blockPos) >= this.getMaxLightLevel();
    }

    @Environment(value=EnvType.CLIENT)
    default public int getLightColor(BlockPos blockPos, int i) {
        int j = this.getBrightness(LightLayer.SKY, blockPos);
        int k = this.getBrightness(LightLayer.BLOCK, blockPos);
        if (k < i) {
            k = i;
        }
        return j << 20 | k << 4;
    }
}

