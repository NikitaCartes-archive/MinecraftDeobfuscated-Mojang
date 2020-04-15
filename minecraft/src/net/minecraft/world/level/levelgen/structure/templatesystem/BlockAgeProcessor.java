package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

public class BlockAgeProcessor extends StructureProcessor {
	private final float mossiness;

	public BlockAgeProcessor(float f) {
		this.mossiness = f;
	}

	public BlockAgeProcessor(Dynamic<?> dynamic) {
		this(dynamic.get("mossiness").asFloat(1.0F));
	}

	@Nullable
	@Override
	public StructureTemplate.StructureBlockInfo processBlock(
		LevelReader levelReader,
		BlockPos blockPos,
		BlockPos blockPos2,
		StructureTemplate.StructureBlockInfo structureBlockInfo,
		StructureTemplate.StructureBlockInfo structureBlockInfo2,
		StructurePlaceSettings structurePlaceSettings
	) {
		Random random = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
		Block block = structureBlockInfo2.state.getBlock();
		BlockPos blockPos3 = structureBlockInfo2.pos;
		BlockState blockState = null;
		if (block == Blocks.STONE_BRICKS || block == Blocks.STONE || block == Blocks.CHISELED_STONE_BRICKS) {
			blockState = this.maybeReplaceFullStoneBlock(random);
		} else if (block.is(BlockTags.STAIRS)) {
			blockState = this.maybeReplaceStairs(random, structureBlockInfo2.state);
		} else if (block.is(BlockTags.SLABS)) {
			blockState = this.maybeReplaceSlab(random);
		} else if (block.is(BlockTags.WALLS)) {
			blockState = this.maybeReplaceWall(random);
		} else if (block == Blocks.OBSIDIAN) {
			blockState = this.maybeReplaceObsidian(random);
		}

		return blockState != null ? new StructureTemplate.StructureBlockInfo(blockPos3, blockState, structureBlockInfo2.nbt) : structureBlockInfo2;
	}

	@Nullable
	private BlockState maybeReplaceFullStoneBlock(Random random) {
		if (random.nextFloat() < 0.5F) {
			return null;
		} else {
			BlockState[] blockStates = new BlockState[]{Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs(random, Blocks.STONE_BRICK_STAIRS)};
			BlockState[] blockStates2 = new BlockState[]{Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs(random, Blocks.MOSSY_STONE_BRICK_STAIRS)};
			return this.getRandomBlock(random, blockStates, blockStates2);
		}
	}

	@Nullable
	private BlockState maybeReplaceStairs(Random random, BlockState blockState) {
		Direction direction = blockState.getValue(StairBlock.FACING);
		Half half = blockState.getValue(StairBlock.HALF);
		if (random.nextFloat() < 0.5F) {
			return null;
		} else {
			BlockState[] blockStates = new BlockState[]{Blocks.STONE_SLAB.defaultBlockState(), Blocks.STONE_BRICK_SLAB.defaultBlockState()};
			BlockState[] blockStates2 = new BlockState[]{
				Blocks.MOSSY_STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, direction).setValue(StairBlock.HALF, half),
				Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState()
			};
			return this.getRandomBlock(random, blockStates, blockStates2);
		}
	}

	@Nullable
	private BlockState maybeReplaceSlab(Random random) {
		return random.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState() : null;
	}

	@Nullable
	private BlockState maybeReplaceWall(Random random) {
		return random.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState() : null;
	}

	@Nullable
	private BlockState maybeReplaceObsidian(Random random) {
		return (double)random.nextFloat() < 0.2 ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : null;
	}

	private static BlockState getRandomFacingStairs(Random random, Block block) {
		return block.defaultBlockState()
			.setValue(StairBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random))
			.setValue(StairBlock.HALF, Half.values()[random.nextInt(Half.values().length)]);
	}

	private BlockState getRandomBlock(Random random, BlockState[] blockStates, BlockState[] blockStates2) {
		return random.nextFloat() < this.mossiness ? getRandomBlock(random, blockStates2) : getRandomBlock(random, blockStates);
	}

	private static BlockState getRandomBlock(Random random, BlockState[] blockStates) {
		return blockStates[random.nextInt(blockStates.length)];
	}

	@Override
	protected StructureProcessorType getType() {
		return StructureProcessorType.BLOCK_AGE;
	}

	@Override
	protected <T> Dynamic<T> getDynamic(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("mossiness"), dynamicOps.createFloat(this.mossiness))));
	}
}
