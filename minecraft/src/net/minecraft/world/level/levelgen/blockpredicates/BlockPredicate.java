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
import net.minecraft.world.level.material.Fluid;

public interface BlockPredicate extends BiPredicate<WorldGenLevel, BlockPos> {
	Codec<BlockPredicate> CODEC = Registry.BLOCK_PREDICATE_TYPES.byNameCodec().dispatch(BlockPredicate::type, BlockPredicateType::codec);
	BlockPredicate ONLY_IN_AIR_PREDICATE = matchesBlock(Blocks.AIR, BlockPos.ZERO);
	BlockPredicate ONLY_IN_AIR_OR_WATER_PREDICATE = matchesBlocks(List.of(Blocks.AIR, Blocks.WATER), BlockPos.ZERO);

	BlockPredicateType<?> type();

	static BlockPredicate allOf(List<BlockPredicate> list) {
		return new AllOfPredicate(list);
	}

	static BlockPredicate allOf(BlockPredicate... blockPredicates) {
		return allOf(List.of(blockPredicates));
	}

	static BlockPredicate allOf(BlockPredicate blockPredicate, BlockPredicate blockPredicate2) {
		return allOf(List.of(blockPredicate, blockPredicate2));
	}

	static BlockPredicate anyOf(List<BlockPredicate> list) {
		return new AnyOfPredicate(list);
	}

	static BlockPredicate anyOf(BlockPredicate... blockPredicates) {
		return anyOf(List.of(blockPredicates));
	}

	static BlockPredicate anyOf(BlockPredicate blockPredicate, BlockPredicate blockPredicate2) {
		return anyOf(List.of(blockPredicate, blockPredicate2));
	}

	static BlockPredicate matchesBlocks(List<Block> list, Vec3i vec3i) {
		return new MatchingBlocksPredicate(vec3i, HolderSet.direct(Block::builtInRegistryHolder, list));
	}

	static BlockPredicate matchesBlocks(List<Block> list) {
		return matchesBlocks(list, Vec3i.ZERO);
	}

	static BlockPredicate matchesBlock(Block block, Vec3i vec3i) {
		return matchesBlocks(List.of(block), vec3i);
	}

	static BlockPredicate matchesTag(TagKey<Block> tagKey, Vec3i vec3i) {
		return new MatchingBlockTagPredicate(vec3i, tagKey);
	}

	static BlockPredicate matchesTag(TagKey<Block> tagKey) {
		return matchesTag(tagKey, Vec3i.ZERO);
	}

	static BlockPredicate matchesFluids(List<Fluid> list, Vec3i vec3i) {
		return new MatchingFluidsPredicate(vec3i, HolderSet.direct(Fluid::builtInRegistryHolder, list));
	}

	static BlockPredicate matchesFluid(Fluid fluid, Vec3i vec3i) {
		return matchesFluids(List.of(fluid), vec3i);
	}

	static BlockPredicate not(BlockPredicate blockPredicate) {
		return new NotPredicate(blockPredicate);
	}

	static BlockPredicate replaceable(Vec3i vec3i) {
		return new ReplaceablePredicate(vec3i);
	}

	static BlockPredicate replaceable() {
		return replaceable(Vec3i.ZERO);
	}

	static BlockPredicate wouldSurvive(BlockState blockState, Vec3i vec3i) {
		return new WouldSurvivePredicate(vec3i, blockState);
	}

	static BlockPredicate hasSturdyFace(Vec3i vec3i, Direction direction) {
		return new HasSturdyFacePredicate(vec3i, direction);
	}

	static BlockPredicate hasSturdyFace(Direction direction) {
		return hasSturdyFace(Vec3i.ZERO, direction);
	}

	static BlockPredicate solid(Vec3i vec3i) {
		return new SolidPredicate(vec3i);
	}

	static BlockPredicate solid() {
		return solid(Vec3i.ZERO);
	}

	static BlockPredicate insideWorld(Vec3i vec3i) {
		return new InsideWorldBoundsPredicate(vec3i);
	}

	static BlockPredicate alwaysTrue() {
		return TrueBlockPredicate.INSTANCE;
	}
}
