/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class PlacedFeature {
    public static final Codec<PlacedFeature> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ConfiguredFeature.CODEC.fieldOf("feature")).forGetter(placedFeature -> placedFeature.feature), ((MapCodec)PlacementModifier.CODEC.listOf().fieldOf("placement")).forGetter(placedFeature -> placedFeature.placement)).apply((Applicative<PlacedFeature, ?>)instance, PlacedFeature::new));
    public static final Codec<Supplier<PlacedFeature>> CODEC = RegistryFileCodec.create(Registry.PLACED_FEATURE_REGISTRY, DIRECT_CODEC);
    public static final Codec<List<Supplier<PlacedFeature>>> LIST_CODEC = RegistryFileCodec.homogeneousList(Registry.PLACED_FEATURE_REGISTRY, DIRECT_CODEC);
    private final Supplier<ConfiguredFeature<?, ?>> feature;
    private final List<PlacementModifier> placement;

    public PlacedFeature(Supplier<ConfiguredFeature<?, ?>> supplier, List<PlacementModifier> list) {
        this.feature = supplier;
        this.placement = list;
    }

    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos) {
        return this.placeWithContext(new PlacementContext(worldGenLevel, chunkGenerator, Optional.empty()), random, blockPos);
    }

    public boolean placeWithBiomeCheck(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos) {
        return this.placeWithContext(new PlacementContext(worldGenLevel, chunkGenerator, Optional.of(this)), random, blockPos);
    }

    private boolean placeWithContext(PlacementContext placementContext, Random random, BlockPos blockPos2) {
        Stream<BlockPos> stream = Stream.of(blockPos2);
        for (PlacementModifier placementModifier : this.placement) {
            stream = stream.flatMap(blockPos -> placementModifier.getPositions(placementContext, random, (BlockPos)blockPos));
        }
        ConfiguredFeature<?, ?> configuredFeature = this.feature.get();
        MutableBoolean mutableBoolean = new MutableBoolean();
        stream.forEach(blockPos -> {
            if (configuredFeature.place(placementContext.getLevel(), placementContext.generator(), random, (BlockPos)blockPos)) {
                mutableBoolean.setTrue();
            }
        });
        return mutableBoolean.isTrue();
    }

    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return this.feature.get().getFeatures();
    }

    @VisibleForDebug
    public List<PlacementModifier> getPlacement() {
        return this.placement;
    }

    public String toString() {
        return "Placed " + Registry.FEATURE.getKey((Feature<?>)this.feature.get().feature());
    }
}

