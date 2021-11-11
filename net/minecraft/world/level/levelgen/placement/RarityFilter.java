/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class RarityFilter
extends PlacementFilter {
    public static final Codec<RarityFilter> CODEC = ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("chance")).xmap(RarityFilter::new, rarityFilter -> rarityFilter.chance).codec();
    private final int chance;

    private RarityFilter(int i) {
        this.chance = i;
    }

    public static RarityFilter onAverageOnceEvery(int i) {
        return new RarityFilter(i);
    }

    @Override
    protected boolean shouldPlace(PlacementContext placementContext, Random random, BlockPos blockPos) {
        return random.nextFloat() < 1.0f / (float)this.chance;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.RARITY_FILTER;
    }
}

