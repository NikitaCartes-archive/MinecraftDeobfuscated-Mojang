package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class FallingBlock extends Block implements Fallable {
	public FallingBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected abstract MapCodec<? extends FallingBlock> codec();

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		level.scheduleTick(blockPos, this, this.getDelayAfterPlace());
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		levelAccessor.scheduleTick(blockPos, this, this.getDelayAfterPlace());
		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (isFree(serverLevel.getBlockState(blockPos.below())) && blockPos.getY() >= serverLevel.getMinBuildHeight()) {
			FallingBlockEntity fallingBlockEntity = FallingBlockEntity.fall(serverLevel, blockPos, blockState);
			this.falling(fallingBlockEntity);
		}
	}

	protected void falling(FallingBlockEntity fallingBlockEntity) {
	}

	protected int getDelayAfterPlace() {
		return 2;
	}

	public static boolean isFree(BlockState blockState) {
		return blockState.isAir() || blockState.is(BlockTags.FIRE) || blockState.liquid() || blockState.canBeReplaced();
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if (randomSource.nextInt(16) == 0) {
			BlockPos blockPos2 = blockPos.below();
			if (isFree(level.getBlockState(blockPos2))) {
				ParticleUtils.spawnParticleBelow(level, blockPos, randomSource, new BlockParticleOption(ParticleTypes.FALLING_DUST, blockState));
			}
		}
	}

	public int getDustColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return -16777216;
	}
}
