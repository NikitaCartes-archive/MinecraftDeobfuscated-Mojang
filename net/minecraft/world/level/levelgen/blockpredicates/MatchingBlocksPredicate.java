/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.blockpredicates.StateTestingPredicate;

class MatchingBlocksPredicate
extends StateTestingPredicate {
    private final List<Block> blocks;
    public static final Codec<MatchingBlocksPredicate> CODEC = RecordCodecBuilder.create(instance -> MatchingBlocksPredicate.stateTestingCodec(instance).and(((MapCodec)Registry.BLOCK.listOf().fieldOf("blocks")).forGetter(matchingBlocksPredicate -> matchingBlocksPredicate.blocks)).apply((Applicative<MatchingBlocksPredicate, ?>)instance, MatchingBlocksPredicate::new));

    public MatchingBlocksPredicate(BlockPos blockPos, List<Block> list) {
        super(blockPos);
        this.blocks = list;
    }

    @Override
    protected boolean test(BlockState blockState) {
        return this.blocks.contains(blockState.getBlock());
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.MATCHING_BLOCKS;
    }
}

