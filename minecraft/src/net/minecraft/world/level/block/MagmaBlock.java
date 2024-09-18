package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MagmaBlock extends Block {
	public static final MapCodec<MagmaBlock> CODEC = simpleCodec(MagmaBlock::new);
	private static final int BUBBLE_COLUMN_CHECK_DELAY = 20;

	@Override
	public MapCodec<MagmaBlock> codec() {
		return CODEC;
	}

	public MagmaBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
		if (!entity.isSteppingCarefully() && entity instanceof LivingEntity) {
			entity.hurt(level.damageSources().hotFloor(), 1.0F);
		}

		super.stepOn(level, blockPos, blockState, entity);
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		BubbleColumnBlock.updateColumn(serverLevel, blockPos.above(), blockState);
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState,
		LevelReader levelReader,
		ScheduledTickAccess scheduledTickAccess,
		BlockPos blockPos,
		Direction direction,
		BlockPos blockPos2,
		BlockState blockState2,
		RandomSource randomSource
	) {
		if (direction == Direction.UP && blockState2.is(Blocks.WATER)) {
			scheduledTickAccess.scheduleTick(blockPos, this, 20);
		}

		return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
	}

	@Override
	protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		level.scheduleTick(blockPos, this, 20);
	}
}
