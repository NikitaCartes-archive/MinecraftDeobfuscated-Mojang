package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public interface BlockPredicate extends BiPredicate<WorldGenLevel, BlockPos> {
	Codec<BlockPredicate> CODEC = Registry.BLOCK_PREDICATE_TYPES.dispatch(BlockPredicate::type, BlockPredicateType::codec);

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

	static BlockPredicate matchesBlocks(List<Block> list, BlockPos blockPos) {
		return new MatchingBlocksPredicate(blockPos, list);
	}

	static BlockPredicate matchesBlock(Block block, BlockPos blockPos) {
		return matchesBlocks(List.of(block), blockPos);
	}

	static BlockPredicate matchesFluids(List<Fluid> list, BlockPos blockPos) {
		return new MatchingFluidsPredicate(blockPos, list);
	}

	static BlockPredicate matchesFluid(Fluid fluid, BlockPos blockPos) {
		return matchesFluids(List.of(fluid), blockPos);
	}

	static BlockPredicate not(BlockPredicate blockPredicate) {
		return new NotPredicate(blockPredicate);
	}

	static BlockPredicate replaceable(BlockPos blockPos) {
		return new ReplaceablePredicate(blockPos);
	}

	static BlockPredicate replaceable() {
		return replaceable(BlockPos.ZERO);
	}

	static BlockPredicate wouldSurvive(BlockState blockState, BlockPos blockPos) {
		return new WouldSurvivePredicate(blockPos, blockState);
	}

	static BlockPredicate alwaysTrue() {
		return TrueBlockPredicate.INSTANCE;
	}
}
