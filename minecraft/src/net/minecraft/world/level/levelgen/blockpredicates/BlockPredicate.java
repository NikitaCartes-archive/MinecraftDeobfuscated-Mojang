package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public interface BlockPredicate extends BiPredicate<WorldGenLevel, BlockPos> {
	Codec<BlockPredicate> CODEC = BuiltInRegistries.BLOCK_PREDICATE_TYPE.byNameCodec().dispatch(BlockPredicate::type, BlockPredicateType::codec);
	BlockPredicate ONLY_IN_AIR_PREDICATE = matchesBlocks(Blocks.AIR);
	BlockPredicate ONLY_IN_AIR_OR_WATER_PREDICATE = matchesBlocks(Blocks.AIR, Blocks.WATER);

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

	static BlockPredicate matchesBlocks(Vec3i vec3i, List<Block> list) {
		return new MatchingBlocksPredicate(vec3i, HolderSet.direct(Block::builtInRegistryHolder, list));
	}

	static BlockPredicate matchesBlocks(List<Block> list) {
		return matchesBlocks(Vec3i.ZERO, list);
	}

	static BlockPredicate matchesBlocks(Vec3i vec3i, Block... blocks) {
		return matchesBlocks(vec3i, List.of(blocks));
	}

	static BlockPredicate matchesBlocks(Block... blocks) {
		return matchesBlocks(Vec3i.ZERO, blocks);
	}

	static BlockPredicate matchesTag(Vec3i vec3i, TagKey<Block> tagKey) {
		return new MatchingBlockTagPredicate(vec3i, tagKey);
	}

	static BlockPredicate matchesTag(TagKey<Block> tagKey) {
		return matchesTag(Vec3i.ZERO, tagKey);
	}

	static BlockPredicate matchesFluids(Vec3i vec3i, List<Fluid> list) {
		return new MatchingFluidsPredicate(vec3i, HolderSet.direct(Fluid::builtInRegistryHolder, list));
	}

	static BlockPredicate matchesFluids(Vec3i vec3i, Fluid... fluids) {
		return matchesFluids(vec3i, List.of(fluids));
	}

	static BlockPredicate matchesFluids(Fluid... fluids) {
		return matchesFluids(Vec3i.ZERO, fluids);
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

	static BlockPredicate noFluid() {
		return noFluid(Vec3i.ZERO);
	}

	static BlockPredicate noFluid(Vec3i vec3i) {
		return matchesFluids(vec3i, Fluids.EMPTY);
	}

	static BlockPredicate insideWorld(Vec3i vec3i) {
		return new InsideWorldBoundsPredicate(vec3i);
	}

	static BlockPredicate alwaysTrue() {
		return TrueBlockPredicate.INSTANCE;
	}

	static BlockPredicate unobstructed(Vec3i vec3i) {
		return new UnobstructedPredicate(vec3i);
	}

	static BlockPredicate unobstructed() {
		return unobstructed(Vec3i.ZERO);
	}
}
