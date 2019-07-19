/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSourceSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;

public class FixedBiomeSource
extends BiomeSource {
    private final Biome biome;

    public FixedBiomeSource(FixedBiomeSourceSettings fixedBiomeSourceSettings) {
        this.biome = fixedBiomeSourceSettings.getBiome();
    }

    @Override
    public Biome getBiome(int i, int j) {
        return this.biome;
    }

    @Override
    public Biome[] getBiomeBlock(int i, int j, int k, int l, boolean bl) {
        Object[] biomes = new Biome[k * l];
        Arrays.fill(biomes, 0, k * l, this.biome);
        return biomes;
    }

    @Override
    @Nullable
    public BlockPos findBiome(int i, int j, int k, List<Biome> list, Random random) {
        if (list.contains(this.biome)) {
            return new BlockPos(i - k + random.nextInt(k * 2 + 1), 0, j - k + random.nextInt(k * 2 + 1));
        }
        return null;
    }

    @Override
    public boolean canGenerateStructure(StructureFeature<?> structureFeature) {
        return this.supportedStructures.computeIfAbsent(structureFeature, this.biome::isValidStart);
    }

    @Override
    public Set<BlockState> getSurfaceBlocks() {
        if (this.surfaceBlocks.isEmpty()) {
            this.surfaceBlocks.add(this.biome.getSurfaceBuilderConfig().getTopMaterial());
        }
        return this.surfaceBlocks;
    }

    @Override
    public Set<Biome> getBiomesWithin(int i, int j, int k) {
        return Sets.newHashSet(this.biome);
    }
}

