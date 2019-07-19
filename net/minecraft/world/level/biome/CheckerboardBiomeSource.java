/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.CheckerboardBiomeSourceSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;

public class CheckerboardBiomeSource
extends BiomeSource {
    private final Biome[] allowedBiomes;
    private final int bitShift;

    public CheckerboardBiomeSource(CheckerboardBiomeSourceSettings checkerboardBiomeSourceSettings) {
        this.allowedBiomes = checkerboardBiomeSourceSettings.getAllowedBiomes();
        this.bitShift = checkerboardBiomeSourceSettings.getSize() + 4;
    }

    @Override
    public Biome getBiome(int i, int j) {
        return this.allowedBiomes[Math.abs(((i >> this.bitShift) + (j >> this.bitShift)) % this.allowedBiomes.length)];
    }

    @Override
    public Biome[] getBiomeBlock(int i, int j, int k, int l, boolean bl) {
        Biome[] biomes = new Biome[k * l];
        for (int m = 0; m < l; ++m) {
            for (int n = 0; n < k; ++n) {
                Biome biome;
                int o = Math.abs(((i + m >> this.bitShift) + (j + n >> this.bitShift)) % this.allowedBiomes.length);
                biomes[m * k + n] = biome = this.allowedBiomes[o];
            }
        }
        return biomes;
    }

    @Override
    @Nullable
    public BlockPos findBiome(int i, int j, int k, List<Biome> list, Random random) {
        return null;
    }

    @Override
    public boolean canGenerateStructure(StructureFeature<?> structureFeature2) {
        return this.supportedStructures.computeIfAbsent(structureFeature2, structureFeature -> {
            for (Biome biome : this.allowedBiomes) {
                if (!biome.isValidStart(structureFeature)) continue;
                return true;
            }
            return false;
        });
    }

    @Override
    public Set<BlockState> getSurfaceBlocks() {
        if (this.surfaceBlocks.isEmpty()) {
            for (Biome biome : this.allowedBiomes) {
                this.surfaceBlocks.add(biome.getSurfaceBuilderConfig().getTopMaterial());
            }
        }
        return this.surfaceBlocks;
    }

    @Override
    public Set<Biome> getBiomesWithin(int i, int j, int k) {
        return Sets.newHashSet(this.allowedBiomes);
    }
}

