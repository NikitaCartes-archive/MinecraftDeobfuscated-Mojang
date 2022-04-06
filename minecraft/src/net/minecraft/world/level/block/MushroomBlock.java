package net.minecraft.world.level.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MushroomBlock extends BushBlock implements BonemealableBlock {
	protected static final float AABB_OFFSET = 3.0F;
	protected static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0);
	private final Supplier<Holder<? extends ConfiguredFeature<?, ?>>> featureSupplier;

	public MushroomBlock(BlockBehaviour.Properties properties, Supplier<Holder<? extends ConfiguredFeature<?, ?>>> supplier) {
		super(properties);
		this.featureSupplier = supplier;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (randomSource.nextInt(25) == 0) {
			int i = 5;
			int j = 4;

			for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-4, -1, -4), blockPos.offset(4, 1, 4))) {
				if (serverLevel.getBlockState(blockPos2).is(this)) {
					if (--i <= 0) {
						return;
					}
				}
			}

			BlockPos blockPos3 = blockPos.offset(randomSource.nextInt(3) - 1, randomSource.nextInt(2) - randomSource.nextInt(2), randomSource.nextInt(3) - 1);

			for (int k = 0; k < 4; k++) {
				if (serverLevel.isEmptyBlock(blockPos3) && blockState.canSurvive(serverLevel, blockPos3)) {
					blockPos = blockPos3;
				}

				blockPos3 = blockPos.offset(randomSource.nextInt(3) - 1, randomSource.nextInt(2) - randomSource.nextInt(2), randomSource.nextInt(3) - 1);
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
		return blockState2.is(BlockTags.MUSHROOM_GROW_BLOCK)
			? true
			: levelReader.getRawBrightness(blockPos, 0) < 13 && this.mayPlaceOn(blockState2, levelReader, blockPos2);
	}

	public boolean growMushroom(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, RandomSource randomSource) {
		serverLevel.removeBlock(blockPos, false);
		if (((ConfiguredFeature)((Holder)this.featureSupplier.get()).value()).place(serverLevel, serverLevel.getChunkSource().getGenerator(), randomSource, blockPos)
			)
		 {
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
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return (double)randomSource.nextFloat() < 0.4;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		this.growMushroom(serverLevel, blockPos, blockState, randomSource);
	}
}
