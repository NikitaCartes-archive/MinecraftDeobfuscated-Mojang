/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;

public class CheckerboardColumnBiomeSource
extends BiomeSource {
    public static final Codec<CheckerboardColumnBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Registry.BIOME.listOf().fieldOf("biomes")).forGetter(checkerboardColumnBiomeSource -> checkerboardColumnBiomeSource.allowedBiomes), ((MapCodec)Codec.INT.fieldOf("scale")).withDefault(2).forGetter(checkerboardColumnBiomeSource -> checkerboardColumnBiomeSource.size)).apply((Applicative<CheckerboardColumnBiomeSource, ?>)instance, CheckerboardColumnBiomeSource::new));
    private final List<Biome> allowedBiomes;
    private final int bitShift;
    private final int size;

    public CheckerboardColumnBiomeSource(List<Biome> list, int i) {
        super(ImmutableList.copyOf(list));
        this.allowedBiomes = list;
        this.bitShift = i + 2;
        this.size = i;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public BiomeSource withSeed(long l) {
        return this;
    }

    @Override
    public Biome getNoiseBiome(int i, int j, int k) {
        return this.allowedBiomes.get(Math.floorMod((i >> this.bitShift) + (k >> this.bitShift), this.allowedBiomes.size()));
    }
}

