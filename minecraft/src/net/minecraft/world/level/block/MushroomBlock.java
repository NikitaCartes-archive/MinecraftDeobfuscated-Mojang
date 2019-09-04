package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.HugeMushroomFeatureConfig;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MushroomBlock extends BushBlock implements BonemealableBlock {
	protected static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0);

	public MushroomBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (random.nextInt(25) == 0) {
			int i = 5;
			int j = 4;

			for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-4, -1, -4), blockPos.offset(4, 1, 4))) {
				if (serverLevel.getBlockState(blockPos2).getBlock() == this) {
					if (--i <= 0) {
						return;
					}
				}
			}

			BlockPos blockPos3 = blockPos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);

			for (int k = 0; k < 4; k++) {
				if (serverLevel.isEmptyBlock(blockPos3) && blockState.canSurvive(serverLevel, blockPos3)) {
					blockPos = blockPos3;
				}

				blockPos3 = blockPos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
			}

			if (serverLevel.isEmptyBlock(blockPos3) && blockState.canSurvive(serverLevel, blockPos3)) {
				serverLevel.setBlock(blockPos3, blockState, 2);
			}
		}
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.isSolidRender(blockGetter, blockPos);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState2 = levelReader.getBlockState(blockPos2);
		Block block = blockState2.getBlock();
		return block != Blocks.MYCELIUM && block != Blocks.PODZOL
			? levelReader.getRawBrightness(blockPos, 0) < 13 && this.mayPlaceOn(blockState2, levelReader, blockPos2)
			: true;
	}

	public boolean growMushroom(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, Random random) {
		serverLevel.removeBlock(blockPos, false);
		Feature<HugeMushroomFeatureConfig> feature = null;
		if (this == Blocks.BROWN_MUSHROOM) {
			feature = Feature.HUGE_BROWN_MUSHROOM;
		} else if (this == Blocks.RED_MUSHROOM) {
			feature = Feature.HUGE_RED_MUSHROOM;
		}

		if (feature != null
			&& feature.place(
				serverLevel,
				(ChunkGenerator<? extends ChunkGeneratorSettings>)serverLevel.getChunkSource().getGenerator(),
				random,
				blockPos,
				new HugeMushroomFeatureConfig(true)
			)) {
			return true;
		} else {
			serverLevel.setBlock(blockPos, blockState, 3);
			return false;
		}
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
		return (double)random.nextFloat() < 0.4;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
		this.growMushroom(serverLevel, blockPos, blockState, random);
	}

	@Override
	public boolean hasPostProcess(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return true;
	}
}
