/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public class CheckerboardColumnBiomeSource
extends BiomeSource {
    public static final Codec<CheckerboardColumnBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Biome.LIST_CODEC.fieldOf("biomes")).forGetter(checkerboardColumnBiomeSource -> checkerboardColumnBiomeSource.allowedBiomes), ((MapCodec)Codec.intRange(0, 62).fieldOf("scale")).orElse(2).forGetter(checkerboardColumnBiomeSource -> checkerboardColumnBiomeSource.size)).apply((Applicative<CheckerboardColumnBiomeSource, ?>)instance, CheckerboardColumnBiomeSource::new));
    private final List<Supplier<Biome>> allowedBiomes;
    private final int bitShift;
    private final int size;

    public CheckerboardColumnBiomeSource(List<Supplier<Biome>> list, int i) {
        super(list.stream());
        this.allowedBiomes = list;
        this.bitShift = i + 2;
        this.size = i;
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
    public Biome getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
        return this.allowedBiomes.get(Math.floorMod((i >> this.bitShift) + (k >> this.bitShift), this.allowedBiomes.size())).get();
    }
}

