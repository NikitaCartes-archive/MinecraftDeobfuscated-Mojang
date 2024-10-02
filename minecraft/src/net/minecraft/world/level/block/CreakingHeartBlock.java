package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class CreakingHeartBlock extends BaseEntityBlock {
	public static final MapCodec<CreakingHeartBlock> CODEC = simpleCodec(CreakingHeartBlock::new);
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
	public static final EnumProperty<CreakingHeartBlock.CreakingHeartState> CREAKING = BlockStateProperties.CREAKING;

	@Override
	public MapCodec<CreakingHeartBlock> codec() {
		return CODEC;
	}

	protected CreakingHeartBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y).setValue(CREAKING, CreakingHeartBlock.CreakingHeartState.DISABLED));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new CreakingHeartBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		if (level.isClientSide) {
			return null;
		} else {
			return blockState.getValue((Property<T>)CREAKING) != CreakingHeartBlock.CreakingHeartState.DISABLED
				? createTickerHelper(blockEntityType, BlockEntityType.CREAKING_HEART, CreakingHeartBlockEntity::serverTick)
				: null;
		}
	}

	public static boolean canSummonCreaking(Level level) {
		return level.dimensionType().natural() && level.isNight();
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if (canSummonCreaking(level)) {
			if (blockState.getValue(CREAKING) != CreakingHeartBlock.CreakingHeartState.DISABLED) {
				if (randomSource.nextInt(16) == 0 && isSurroundedByLogs(level, blockPos)) {
					level.playLocalSound(
						(double)(blockPos.getX() + level.random.nextIntBetweenInclusive(-16, 16)),
						(double)(blockPos.getY() + level.random.nextIntBetweenInclusive(-14, 2)),
						(double)(blockPos.getZ() + level.random.nextIntBetweenInclusive(-16, 16)),
						SoundEvents.CREAKING_HEART_IDLE,
						SoundSource.BLOCKS,
						1.0F,
						1.0F,
						false
					);
				}
			}
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
		BlockState blockState3 = super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
		return updateState(blockState3, levelReader, blockPos);
	}

	private static BlockState updateState(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		boolean bl = hasRequiredLogs(blockState, levelReader, blockPos);
		CreakingHeartBlock.CreakingHeartState creakingHeartState = blockState.getValue(CREAKING);
		return bl && creakingHeartState == CreakingHeartBlock.CreakingHeartState.DISABLED
			? blockState.setValue(CREAKING, CreakingHeartBlock.CreakingHeartState.DORMANT)
			: blockState;
	}

	public static boolean hasRequiredLogs(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		Direction.Axis axis = blockState.getValue(AXIS);

		for (Direction direction : axis.getDirections()) {
			BlockState blockState2 = levelReader.getBlockState(blockPos.relative(direction));
			if (!blockState2.is(BlockTags.PALE_OAK_LOGS) || blockState2.getValue(AXIS) != axis) {
				return false;
			}
		}

		return true;
	}

	private static boolean isSurroundedByLogs(LevelAccessor levelAccessor, BlockPos blockPos) {
		for (Direction direction : Direction.values()) {
			BlockPos blockPos2 = blockPos.relative(direction);
			BlockState blockState = levelAccessor.getBlockState(blockPos2);
			if (!blockState.is(BlockTags.LOGS)) {
				return false;
			}
		}

		return true;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return updateState(
			this.defaultBlockState().setValue(AXIS, blockPlaceContext.getClickedFace().getAxis()), blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos()
		);
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return RotatedPillarBlock.rotatePillar(blockState, rotation);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS, CREAKING);
	}

	@Override
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (level.getBlockEntity(blockPos) instanceof CreakingHeartBlockEntity creakingHeartBlockEntity) {
			creakingHeartBlockEntity.removeProtector(null);
		}

		super.onRemove(blockState, level, blockPos, blockState2, bl);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		if (level.getBlockEntity(blockPos) instanceof CreakingHeartBlockEntity creakingHeartBlockEntity) {
			creakingHeartBlockEntity.removeProtector(player.damageSources().playerAttack(player));
		}

		return super.playerWillDestroy(level, blockPos, blockState, player);
	}

	public static enum CreakingHeartState implements StringRepresentable {
		DISABLED("disabled"),
		DORMANT("dormant"),
		ACTIVE("active");

		private final String name;

		private CreakingHeartState(final String string2) {
			this.name = string2;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
