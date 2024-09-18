package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class CoralFanBlock extends BaseCoralFanBlock {
	public static final MapCodec<CoralFanBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(CoralBlock.DEAD_CORAL_FIELD.forGetter(coralFanBlock -> coralFanBlock.deadBlock), propertiesCodec())
				.apply(instance, CoralFanBlock::new)
	);
	private final Block deadBlock;

	@Override
	public MapCodec<CoralFanBlock> codec() {
		return CODEC;
	}

	protected CoralFanBlock(Block block, BlockBehaviour.Properties properties) {
		super(properties);
		this.deadBlock = block;
	}

	@Override
	protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		this.tryScheduleDieTick(blockState, level, level, level.random, blockPos);
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (!scanForWater(blockState, serverLevel, blockPos)) {
			serverLevel.setBlock(blockPos, this.deadBlock.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)), 2);
		}
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
		if (direction == Direction.DOWN && !blockState.canSurvive(levelReader, blockPos)) {
			return Blocks.AIR.defaultBlockState();
		} else {
			this.tryScheduleDieTick(blockState, levelReader, scheduledTickAccess, randomSource, blockPos);
			if ((Boolean)blockState.getValue(WATERLOGGED)) {
				scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
			}

			return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
		}
	}
}
