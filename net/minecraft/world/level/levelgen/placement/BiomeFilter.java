/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class BiomeFilter
extends PlacementFilter {
    private static final BiomeFilter INSTANCE = new BiomeFilter();
    public static Codec<BiomeFilter> CODEC = Codec.unit(() -> INSTANCE);

    private BiomeFilter() {
    }

    public static BiomeFilter biome() {
        return INSTANCE;
    }

    @Override
    protected boolean shouldPlace(PlacementContext placementContext, Random random, BlockPos blockPos) {
        PlacedFeature placedFeature = placementContext.topFeature().orElseThrow(() -> new IllegalStateException("Tried to biome check an unregistered feature"));
        Biome biome = placementContext.getLevel().getBiome(blockPos);
        return biome.getGenerationSettings().hasFeature(placedFeature);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.BIOME_FILTER;
    }
}

