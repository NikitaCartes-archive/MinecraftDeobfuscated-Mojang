/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.AllOfPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.AnyOfPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.blockpredicates.MatchingBlocksPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.MatchingFluidsPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.NotPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.ReplaceablePredicate;
import net.minecraft.world.level.material.Fluid;

public interface BlockPredicate
extends BiPredicate<WorldGenLevel, BlockPos> {
    public static final Codec<BlockPredicate> CODEC = Registry.BLOCK_PREDICATE_TYPES.dispatch(BlockPredicate::type, BlockPredicateType::codec);

    public BlockPredicateType<?> type();

    public static BlockPredicate allOf(List<BlockPredicate> list) {
        return new AllOfPredicate(list);
    }

    public static BlockPredicate allOf(BlockPredicate ... blockPredicates) {
        return BlockPredicate.allOf(List.of(blockPredicates));
    }

    public static BlockPredicate allOf(BlockPredicate blockPredicate, BlockPredicate blockPredicate2) {
        return BlockPredicate.allOf(List.of(blockPredicate, blockPredicate2));
    }

    public static BlockPredicate anyOf(List<BlockPredicate> list) {
        return new AnyOfPredicate(list);
    }

    public static BlockPredicate anyOf(BlockPredicate ... blockPredicates) {
        return BlockPredicate.anyOf(List.of(blockPredicates));
    }

    public static BlockPredicate anyOf(BlockPredicate blockPredicate, BlockPredicate blockPredicate2) {
        return BlockPredicate.anyOf(List.of(blockPredicate, blockPredicate2));
    }

    public static BlockPredicate matchesBlocks(List<Block> list, BlockPos blockPos) {
        return new MatchingBlocksPredicate(list, blockPos);
    }

    public static BlockPredicate matchesBlock(Block block, BlockPos blockPos) {
        return BlockPredicate.matchesBlocks(List.of(block), blockPos);
    }

    public static BlockPredicate matchesFluids(List<Fluid> list, BlockPos blockPos) {
        return new MatchingFluidsPredicate(list, blockPos);
    }

    public static BlockPredicate matchesFluid(Fluid fluid, BlockPos blockPos) {
        return BlockPredicate.matchesFluids(List.of(fluid), blockPos);
    }

    public static BlockPredicate not(BlockPredicate blockPredicate) {
        return new NotPredicate(blockPredicate);
    }

    public static BlockPredicate replaceable() {
        return ReplaceablePredicate.INSTANCE;
    }
}

