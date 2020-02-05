/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSourceSettings;
import org.jetbrains.annotations.Nullable;

public class FixedBiomeSource
extends BiomeSource {
    private final Biome biome;

    public FixedBiomeSource(FixedBiomeSourceSettings fixedBiomeSourceSettings) {
        super(ImmutableSet.of(fixedBiomeSourceSettings.getBiome()));
        this.biome = fixedBiomeSourceSettings.getBiome();
    }

    @Override
    public Biome getNoiseBiome(int i, int j, int k) {
        return this.biome;
    }

    @Override
    @Nullable
    public BlockPos findBiomeHorizontal(int i, int j, int k, int l, int m, List<Biome> list, Random random, boolean bl) {
        if (list.contains(this.biome)) {
            if (bl) {
                return new BlockPos(i, j, k);
            }
            return new BlockPos(i - l + random.nextInt(l * 2 + 1), j, k - l + random.nextInt(l * 2 + 1));
        }
        return null;
    }

    @Override
    public Set<Biome> getBiomesWithin(int i, int j, int k, int l) {
        return Sets.newHashSet(this.biome);
    }
}

