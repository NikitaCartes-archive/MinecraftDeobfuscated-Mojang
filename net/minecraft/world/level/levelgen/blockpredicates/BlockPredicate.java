/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.AllOfPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.AnyOfPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.blockpredicates.HasSturdyFacePredicate;
import net.minecraft.world.level.levelgen.blockpredicates.InsideWorldBoundsPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.MatchingBlockTagPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.MatchingBlocksPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.MatchingFluidsPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.NotPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.ReplaceablePredicate;
import net.minecraft.world.level.levelgen.blockpredicates.SolidPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.TrueBlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.WouldSurvivePredicate;
import net.minecraft.world.level.material.Fluid;

public interface BlockPredicate
extends BiPredicate<WorldGenLevel, BlockPos> {
    public static final Codec<BlockPredicate> CODEC = Registry.BLOCK_PREDICATE_TYPES.byNameCodec().dispatch(BlockPredicate::type, BlockPredicateType::codec);
    public static final BlockPredicate ONLY_IN_AIR_PREDICATE = BlockPredicate.matchesBlock(Blocks.AIR, BlockPos.ZERO);
    public static final BlockPredicate ONLY_IN_AIR_OR_WATER_PREDICATE = BlockPredicate.matchesBlocks(List.of(Blocks.AIR, Blocks.WATER), BlockPos.ZERO);

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

    public static BlockPredicate matchesBlocks(List<Block> list, Vec3i vec3i) {
        return new MatchingBlocksPredicate(vec3i, HolderSet.direct(Block::builtInRegistryHolder, list));
    }

    public static BlockPredicate matchesBlocks(List<Block> list) {
        return BlockPredicate.matchesBlocks(list, Vec3i.ZERO);
    }

    public static BlockPredicate matchesBlock(Block block, Vec3i vec3i) {
        return BlockPredicate.matchesBlocks(List.of(block), vec3i);
    }

    public static BlockPredicate matchesTag(TagKey<Block> tagKey, Vec3i vec3i) {
        return new MatchingBlockTagPredicate(vec3i, tagKey);
    }

    public static BlockPredicate matchesTag(TagKey<Block> tagKey) {
        return BlockPredicate.matchesTag(tagKey, Vec3i.ZERO);
    }

    public static BlockPredicate matchesFluids(List<Fluid> list, Vec3i vec3i) {
        return new MatchingFluidsPredicate(vec3i, HolderSet.direct(Fluid::builtInRegistryHolder, list));
    }

    public static BlockPredicate matchesFluid(Fluid fluid, Vec3i vec3i) {
        return BlockPredicate.matchesFluids(List.of(fluid), vec3i);
    }

    public static BlockPredicate not(BlockPredicate blockPredicate) {
        return new NotPredicate(blockPredicate);
    }

    public static BlockPredicate replaceable(Vec3i vec3i) {
        return new ReplaceablePredicate(vec3i);
    }

    public static BlockPredicate replaceable() {
        return BlockPredicate.replaceable(Vec3i.ZERO);
    }

    public static BlockPredicate wouldSurvive(BlockState blockState, Vec3i vec3i) {
        return new WouldSurvivePredicate(vec3i, blockState);
    }

    public static BlockPredicate hasSturdyFace(Vec3i vec3i, Direction direction) {
        return new HasSturdyFacePredicate(vec3i, direction);
    }

    public static BlockPredicate hasSturdyFace(Direction direction) {
        return BlockPredicate.hasSturdyFace(Vec3i.ZERO, direction);
    }

    public static BlockPredicate solid(Vec3i vec3i) {
        return new SolidPredicate(vec3i);
    }

    public static BlockPredicate solid() {
        return BlockPredicate.solid(Vec3i.ZERO);
    }

    public static BlockPredicate insideWorld(Vec3i vec3i) {
        return new InsideWorldBoundsPredicate(vec3i);
    }

    public static BlockPredicate alwaysTrue() {
        return TrueBlockPredicate.INSTANCE;
    }
}

