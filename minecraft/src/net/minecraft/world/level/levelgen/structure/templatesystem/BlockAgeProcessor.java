package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

public class BlockAgeProcessor extends StructureProcessor {
	public static final Codec<BlockAgeProcessor> CODEC = Codec.FLOAT
		.fieldOf("mossiness")
		.<BlockAgeProcessor>xmap(BlockAgeProcessor::new, blockAgeProcessor -> blockAgeProcessor.mossiness)
		.codec();
	private static final float PROBABILITY_OF_REPLACING_FULL_BLOCK = 0.5F;
	private static final float PROBABILITY_OF_REPLACING_STAIRS = 0.5F;
	private static final float PROBABILITY_OF_REPLACING_OBSIDIAN = 0.15F;
	private static final BlockState[] NON_MOSSY_REPLACEMENTS = new BlockState[]{
		Blocks.STONE_SLAB.defaultBlockState(), Blocks.STONE_BRICK_SLAB.defaultBlockState()
	};
	private final float mossiness;

	public BlockAgeProcessor(float f) {
		this.mossiness = f;
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
		RandomSource randomSource = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
		BlockState blockState = structureBlockInfo2.state;
		BlockPos blockPos3 = structureBlockInfo2.pos;
		BlockState blockState2 = null;
		if (blockState.is(Blocks.STONE_BRICKS) || blockState.is(Blocks.STONE) || blockState.is(Blocks.CHISELED_STONE_BRICKS)) {
			blockState2 = this.maybeReplaceFullStoneBlock(randomSource);
		} else if (blockState.is(BlockTags.STAIRS)) {
			blockState2 = this.maybeReplaceStairs(randomSource, structureBlockInfo2.state);
		} else if (blockState.is(BlockTags.SLABS)) {
			blockState2 = this.maybeReplaceSlab(randomSource);
		} else if (blockState.is(BlockTags.WALLS)) {
			blockState2 = this.maybeReplaceWall(randomSource);
		} else if (blockState.is(Blocks.OBSIDIAN)) {
			blockState2 = this.maybeReplaceObsidian(randomSource);
		}

		return blockState2 != null ? new StructureTemplate.StructureBlockInfo(blockPos3, blockState2, structureBlockInfo2.nbt) : structureBlockInfo2;
	}

	@Nullable
	private BlockState maybeReplaceFullStoneBlock(RandomSource randomSource) {
		if (randomSource.nextFloat() >= 0.5F) {
			return null;
		} else {
			BlockState[] blockStates = new BlockState[]{Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs(randomSource, Blocks.STONE_BRICK_STAIRS)};
			BlockState[] blockStates2 = new BlockState[]{
				Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs(randomSource, Blocks.MOSSY_STONE_BRICK_STAIRS)
			};
			return this.getRandomBlock(randomSource, blockStates, blockStates2);
		}
	}

	@Nullable
	private BlockState maybeReplaceStairs(RandomSource randomSource, BlockState blockState) {
		Direction direction = blockState.getValue(StairBlock.FACING);
		Half half = blockState.getValue(StairBlock.HALF);
		if (randomSource.nextFloat() >= 0.5F) {
			return null;
		} else {
			BlockState[] blockStates = new BlockState[]{
				Blocks.MOSSY_STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, direction).setValue(StairBlock.HALF, half),
				Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState()
			};
			return this.getRandomBlock(randomSource, NON_MOSSY_REPLACEMENTS, blockStates);
		}
	}

	@Nullable
	private BlockState maybeReplaceSlab(RandomSource randomSource) {
		return randomSource.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState() : null;
	}

	@Nullable
	private BlockState maybeReplaceWall(RandomSource randomSource) {
		return randomSource.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState() : null;
	}

	@Nullable
	private BlockState maybeReplaceObsidian(RandomSource randomSource) {
		return randomSource.nextFloat() < 0.15F ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : null;
	}

	private static BlockState getRandomFacingStairs(RandomSource randomSource, Block block) {
		return block.defaultBlockState()
			.setValue(StairBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(randomSource))
			.setValue(StairBlock.HALF, Half.values()[randomSource.nextInt(Half.values().length)]);
	}

	private BlockState getRandomBlock(RandomSource randomSource, BlockState[] blockStates, BlockState[] blockStates2) {
		return randomSource.nextFloat() < this.mossiness ? getRandomBlock(randomSource, blockStates2) : getRandomBlock(randomSource, blockStates);
	}

	private static BlockState getRandomBlock(RandomSource randomSource, BlockState[] blockStates) {
		return blockStates[randomSource.nextInt(blockStates.length)];
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return StructureProcessorType.BLOCK_AGE;
	}
}
