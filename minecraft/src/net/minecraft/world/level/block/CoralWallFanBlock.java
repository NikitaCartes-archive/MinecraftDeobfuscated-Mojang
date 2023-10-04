package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class CoralWallFanBlock extends BaseCoralWallFanBlock {
	public static final MapCodec<CoralWallFanBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(CoralBlock.DEAD_CORAL_FIELD.forGetter(coralWallFanBlock -> coralWallFanBlock.deadBlock), propertiesCodec())
				.apply(instance, CoralWallFanBlock::new)
	);
	private final Block deadBlock;

	@Override
	public MapCodec<CoralWallFanBlock> codec() {
		return CODEC;
	}

	protected CoralWallFanBlock(Block block, BlockBehaviour.Properties properties) {
		super(properties);
		this.deadBlock = block;
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		this.tryScheduleDieTick(blockState, level, blockPos);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (!scanForWater(blockState, serverLevel, blockPos)) {
			serverLevel.setBlock(
				blockPos, this.deadBlock.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, (Direction)blockState.getValue(FACING)), 2
			);
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (direction.getOpposite() == blockState.getValue(FACING) && !blockState.canSurvive(levelAccessor, blockPos)) {
			return Blocks.AIR.defaultBlockState();
		} else {
			if ((Boolean)blockState.getValue(WATERLOGGED)) {
				levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
			}

			this.tryScheduleDieTick(blockState, levelAccessor, blockPos);
			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		}
	}
}
