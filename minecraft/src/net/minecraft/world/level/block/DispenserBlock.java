package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class DispenserBlock extends BaseEntityBlock {
	public static final DirectionProperty FACING = DirectionalBlock.FACING;
	public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
	private static final Map<Item, DispenseItemBehavior> DISPENSER_REGISTRY = Util.make(
		new Object2ObjectOpenHashMap<>(), object2ObjectOpenHashMap -> object2ObjectOpenHashMap.defaultReturnValue(new DefaultDispenseItemBehavior())
	);

	public static void registerBehavior(ItemLike itemLike, DispenseItemBehavior dispenseItemBehavior) {
		DISPENSER_REGISTRY.put(itemLike.asItem(), dispenseItemBehavior);
	}

	protected DispenserBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, Boolean.valueOf(false)));
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof DispenserBlockEntity) {
				player.openMenu((DispenserBlockEntity)blockEntity);
				if (blockEntity instanceof DropperBlockEntity) {
					player.awardStat(Stats.INSPECT_DROPPER);
				} else {
					player.awardStat(Stats.INSPECT_DISPENSER);
				}
			}

			return InteractionResult.CONSUME;
		}
	}

	protected void dispenseFrom(ServerLevel serverLevel, BlockPos blockPos) {
		BlockSourceImpl blockSourceImpl = new BlockSourceImpl(serverLevel, blockPos);
		DispenserBlockEntity dispenserBlockEntity = blockSourceImpl.getEntity();
		int i = dispenserBlockEntity.getRandomSlot();
		if (i < 0) {
			serverLevel.levelEvent(1001, blockPos, 0);
		} else {
			ItemStack itemStack = dispenserBlockEntity.getItem(i);
			DispenseItemBehavior dispenseItemBehavior = this.getDispenseMethod(itemStack);
			if (dispenseItemBehavior != DispenseItemBehavior.NOOP) {
				dispenserBlockEntity.setItem(i, dispenseItemBehavior.dispense(blockSourceImpl, itemStack));
			}
		}
	}

	protected DispenseItemBehavior getDispenseMethod(ItemStack itemStack) {
		return (DispenseItemBehavior)DISPENSER_REGISTRY.get(itemStack.getItem());
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		boolean bl2 = level.hasNeighborSignal(blockPos) || level.hasNeighborSignal(blockPos.above());
		boolean bl3 = (Boolean)blockState.getValue(TRIGGERED);
		if (bl2 && !bl3) {
			level.getBlockTicks().scheduleTick(blockPos, this, 4);
			level.setBlock(blockPos, blockState.setValue(TRIGGERED, Boolean.valueOf(true)), 4);
		} else if (!bl2 && bl3) {
			level.setBlock(blockPos, blockState.setValue(TRIGGERED, Boolean.valueOf(false)), 4);
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		this.dispenseFrom(serverLevel, blockPos);
	}

	@Override
	public BlockEntity newBlockEntity(BlockGetter blockGetter) {
		return new DispenserBlockEntity();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getNearestLookingDirection().getOpposite());
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
		if (itemStack.hasCustomHoverName()) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof DispenserBlockEntity) {
				((DispenserBlockEntity)blockEntity).setCustomName(itemStack.getHoverName());
			}
		}
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof DispenserBlockEntity) {
				Containers.dropContents(level, blockPos, (DispenserBlockEntity)blockEntity);
				level.updateNeighbourForOutputSignal(blockPos, this);
			}

			super.onRemove(blockState, level, blockPos, blockState2, bl);
		}
	}

	public static Position getDispensePosition(BlockSource blockSource) {
		Direction direction = blockSource.getBlockState().getValue(FACING);
		double d = blockSource.x() + 0.7 * (double)direction.getStepX();
		double e = blockSource.y() + 0.7 * (double)direction.getStepY();
		double f = blockSource.z() + 0.7 * (double)direction.getStepZ();
		return new PositionImpl(d, e, f);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(blockPos));
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, TRIGGERED);
	}
}
