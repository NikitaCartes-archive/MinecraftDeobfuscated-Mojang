package net.minecraft.world.level.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;

public class ComparatorBlock extends DiodeBlock implements EntityBlock {
	public static final EnumProperty<ComparatorMode> MODE = BlockStateProperties.MODE_COMPARATOR;

	public ComparatorBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(MODE, ComparatorMode.COMPARE)
		);
	}

	@Override
	protected int getDelay(BlockState blockState) {
		return 2;
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return direction == Direction.DOWN && !this.canSurviveOn(levelAccessor, blockPos2, blockState2)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected int getOutputSignal(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
		return blockEntity instanceof ComparatorBlockEntity ? ((ComparatorBlockEntity)blockEntity).getOutputSignal() : 0;
	}

	private int calculateOutputSignal(Level level, BlockPos blockPos, BlockState blockState) {
		int i = this.getInputSignal(level, blockPos, blockState);
		if (i == 0) {
			return 0;
		} else {
			int j = this.getAlternateSignal(level, blockPos, blockState);
			if (j > i) {
				return 0;
			} else {
				return blockState.getValue(MODE) == ComparatorMode.SUBTRACT ? i - j : i;
			}
		}
	}

	@Override
	protected boolean shouldTurnOn(Level level, BlockPos blockPos, BlockState blockState) {
		int i = this.getInputSignal(level, blockPos, blockState);
		if (i == 0) {
			return false;
		} else {
			int j = this.getAlternateSignal(level, blockPos, blockState);
			return i > j ? true : i == j && blockState.getValue(MODE) == ComparatorMode.COMPARE;
		}
	}

	@Override
	protected int getInputSignal(Level level, BlockPos blockPos, BlockState blockState) {
		int i = super.getInputSignal(level, blockPos, blockState);
		Direction direction = blockState.getValue(FACING);
		BlockPos blockPos2 = blockPos.relative(direction);
		BlockState blockState2 = level.getBlockState(blockPos2);
		if (blockState2.hasAnalogOutputSignal()) {
			i = blockState2.getAnalogOutputSignal(level, blockPos2);
		} else if (i < 15 && blockState2.isRedstoneConductor(level, blockPos2)) {
			blockPos2 = blockPos2.relative(direction);
			blockState2 = level.getBlockState(blockPos2);
			ItemFrame itemFrame = this.getItemFrame(level, direction, blockPos2);
			int j = Math.max(
				itemFrame == null ? Integer.MIN_VALUE : itemFrame.getAnalogOutput(),
				blockState2.hasAnalogOutputSignal() ? blockState2.getAnalogOutputSignal(level, blockPos2) : Integer.MIN_VALUE
			);
			if (j != Integer.MIN_VALUE) {
				i = j;
			}
		}

		return i;
	}

	@Nullable
	private ItemFrame getItemFrame(Level level, Direction direction, BlockPos blockPos) {
		List<ItemFrame> list = level.getEntitiesOfClass(
			ItemFrame.class,
			new AABB(
				(double)blockPos.getX(),
				(double)blockPos.getY(),
				(double)blockPos.getZ(),
				(double)(blockPos.getX() + 1),
				(double)(blockPos.getY() + 1),
				(double)(blockPos.getZ() + 1)
			),
			itemFrame -> itemFrame != null && itemFrame.getDirection() == direction
		);
		return list.size() == 1 ? (ItemFrame)list.get(0) : null;
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (!player.getAbilities().mayBuild) {
			return InteractionResult.PASS;
		} else {
			blockState = blockState.cycle(MODE);
			float f = blockState.getValue(MODE) == ComparatorMode.SUBTRACT ? 0.55F : 0.5F;
			level.playSound(player, blockPos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, f);
			level.setBlock(blockPos, blockState, 2);
			this.refreshOutputState(level, blockPos, blockState);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	}

	@Override
	protected void checkTickOnNeighbor(Level level, BlockPos blockPos, BlockState blockState) {
		if (!level.getBlockTicks().willTickThisTick(blockPos, this)) {
			int i = this.calculateOutputSignal(level, blockPos, blockState);
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			int j = blockEntity instanceof ComparatorBlockEntity ? ((ComparatorBlockEntity)blockEntity).getOutputSignal() : 0;
			if (i != j || (Boolean)blockState.getValue(POWERED) != this.shouldTurnOn(level, blockPos, blockState)) {
				TickPriority tickPriority = this.shouldPrioritize(level, blockPos, blockState) ? TickPriority.HIGH : TickPriority.NORMAL;
				level.scheduleTick(blockPos, this, 2, tickPriority);
			}
		}
	}

	private void refreshOutputState(Level level, BlockPos blockPos, BlockState blockState) {
		int i = this.calculateOutputSignal(level, blockPos, blockState);
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		int j = 0;
		if (blockEntity instanceof ComparatorBlockEntity comparatorBlockEntity) {
			j = comparatorBlockEntity.getOutputSignal();
			comparatorBlockEntity.setOutputSignal(i);
		}

		if (j != i || blockState.getValue(MODE) == ComparatorMode.COMPARE) {
			boolean bl = this.shouldTurnOn(level, blockPos, blockState);
			boolean bl2 = (Boolean)blockState.getValue(POWERED);
			if (bl2 && !bl) {
				level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(false)), 2);
			} else if (!bl2 && bl) {
				level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(true)), 2);
			}

			this.updateNeighborsInFront(level, blockPos, blockState);
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		this.refreshOutputState(serverLevel, blockPos, blockState);
	}

	@Override
	public boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int j) {
		super.triggerEvent(blockState, level, blockPos, i, j);
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		return blockEntity != null && blockEntity.triggerEvent(i, j);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new ComparatorBlockEntity(blockPos, blockState);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, MODE, POWERED);
	}
}
