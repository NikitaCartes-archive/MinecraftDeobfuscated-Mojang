/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class WeightedPlacedFeature {
    public static final Codec<WeightedPlacedFeature> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)PlacedFeature.CODEC.fieldOf("feature")).flatXmap(ExtraCodecs.nonNullSupplierCheck(), ExtraCodecs.nonNullSupplierCheck()).forGetter(weightedPlacedFeature -> weightedPlacedFeature.feature), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("chance")).forGetter(weightedPlacedFeature -> Float.valueOf(weightedPlacedFeature.chance))).apply((Applicative<WeightedPlacedFeature, ?>)instance, WeightedPlacedFeature::new));
    public final Supplier<PlacedFeature> feature;
    public final float chance;

    public WeightedPlacedFeature(PlacedFeature placedFeature, float f) {
        this(() -> placedFeature, f);
    }

    private WeightedPlacedFeature(Supplier<PlacedFeature> supplier, float f) {
        this.feature = supplier;
        this.chance = f;
    }

    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos) {
        return this.feature.get().place(worldGenLevel, chunkGenerator, random, blockPos);
    }
}

