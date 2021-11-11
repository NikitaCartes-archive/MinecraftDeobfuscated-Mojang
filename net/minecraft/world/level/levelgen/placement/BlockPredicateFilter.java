/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class BlockPredicateFilter
extends PlacementFilter {
    public static final Codec<BlockPredicateFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockPredicate.CODEC.fieldOf("predicate")).forGetter(blockPredicateFilter -> blockPredicateFilter.predicate)).apply((Applicative<BlockPredicateFilter, ?>)instance, BlockPredicateFilter::new));
    private final BlockPredicate predicate;

    private BlockPredicateFilter(BlockPredicate blockPredicate) {
        this.predicate = blockPredicate;
    }

    public static BlockPredicateFilter forPredicate(BlockPredicate blockPredicate) {
        return new BlockPredicateFilter(blockPredicate);
    }

    @Override
    protected boolean shouldPlace(PlacementContext placementContext, Random random, BlockPos blockPos) {
        return this.predicate.test(placementContext.getLevel(), blockPos);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.BLOCK_PREDICATE_FILTER;
    }
}

