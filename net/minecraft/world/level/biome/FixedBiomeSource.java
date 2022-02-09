/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.Nullable;

public class FixedBiomeSource
extends BiomeSource
implements BiomeManager.NoiseBiomeSource {
    public static final Codec<FixedBiomeSource> CODEC = ((MapCodec)Biome.CODEC.fieldOf("biome")).xmap(FixedBiomeSource::new, fixedBiomeSource -> fixedBiomeSource.biome).stable().codec();
    private final Holder<Biome> biome;

    public FixedBiomeSource(Holder<Biome> holder) {
        super(ImmutableList.of(holder));
        this.biome = holder;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long l) {
        return this;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
        return this.biome;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int i, int j, int k) {
        return this.biome;
    }

    @Override
    @Nullable
    public BlockPos findBiomeHorizontal(int i, int j, int k, int l, int m, Predicate<Holder<Biome>> predicate, Random random, boolean bl, Climate.Sampler sampler) {
        if (predicate.test(this.biome)) {
            if (bl) {
                return new BlockPos(i, j, k);
            }
            return new BlockPos(i - l + random.nextInt(l * 2 + 1), j, k - l + random.nextInt(l * 2 + 1));
        }
        return null;
    }

    @Override
    public Set<Holder<Biome>> getBiomesWithin(int i, int j, int k, int l, Climate.Sampler sampler) {
        return Sets.newHashSet(Set.of(this.biome));
    }
}

