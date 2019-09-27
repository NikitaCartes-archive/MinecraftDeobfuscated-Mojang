package net.minecraft.world.level.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChestBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final VoxelShape NORTH_AABB = Block.box(1.0, 0.0, 0.0, 15.0, 14.0, 15.0);
	protected static final VoxelShape SOUTH_AABB = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 16.0);
	protected static final VoxelShape WEST_AABB = Block.box(0.0, 0.0, 1.0, 15.0, 14.0, 15.0);
	protected static final VoxelShape EAST_AABB = Block.box(1.0, 0.0, 1.0, 16.0, 14.0, 15.0);
	protected static final VoxelShape AABB = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);
	private static final ChestBlock.ChestSearchCallback<Container> CHEST_COMBINER = new ChestBlock.ChestSearchCallback<Container>() {
		public Container acceptDouble(ChestBlockEntity chestBlockEntity, ChestBlockEntity chestBlockEntity2) {
			return new CompoundContainer(chestBlockEntity, chestBlockEntity2);
		}

		public Container acceptSingle(ChestBlockEntity chestBlockEntity) {
			return chestBlockEntity;
		}
	};
	private static final ChestBlock.ChestSearchCallback<MenuProvider> MENU_PROVIDER_COMBINER = new ChestBlock.ChestSearchCallback<MenuProvider>() {
		public MenuProvider acceptDouble(ChestBlockEntity chestBlockEntity, ChestBlockEntity chestBlockEntity2) {
			final Container container = new CompoundContainer(chestBlockEntity, chestBlockEntity2);
			return new MenuProvider() {
				@Nullable
				@Override
				public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
					if (chestBlockEntity.canOpen(player) && chestBlockEntity2.canOpen(player)) {
						chestBlockEntity.unpackLootTable(inventory.player);
						chestBlockEntity2.unpackLootTable(inventory.player);
						return ChestMenu.sixRows(i, inventory, container);
					} else {
						return null;
					}
				}

				@Override
				public Component getDisplayName() {
					if (chestBlockEntity.hasCustomName()) {
						return chestBlockEntity.getDisplayName();
					} else {
						return (Component)(chestBlockEntity2.hasCustomName() ? chestBlockEntity2.getDisplayName() : new TranslatableComponent("container.chestDouble"));
					}
				}
			};
		}

		public MenuProvider acceptSingle(ChestBlockEntity chestBlockEntity) {
			return chestBlockEntity;
		}
	};

	protected ChestBlock(Block.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, ChestType.SINGLE).setValue(WATERLOGGED, Boolean.valueOf(false))
		);
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		if (blockState2.getBlock() == this && direction.getAxis().isHorizontal()) {
			ChestType chestType = blockState2.getValue(TYPE);
			if (blockState.getValue(TYPE) == ChestType.SINGLE
				&& chestType != ChestType.SINGLE
				&& blockState.getValue(FACING) == blockState2.getValue(FACING)
				&& getConnectedDirection(blockState2) == direction.getOpposite()) {
				return blockState.setValue(TYPE, chestType.getOpposite());
			}
		} else if (getConnectedDirection(blockState) == direction) {
			return blockState.setValue(TYPE, ChestType.SINGLE);
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		if (blockState.getValue(TYPE) == ChestType.SINGLE) {
			return AABB;
		} else {
			switch (getConnectedDirection(blockState)) {
				case NORTH:
				default:
					return NORTH_AABB;
				case SOUTH:
					return SOUTH_AABB;
				case WEST:
					return WEST_AABB;
				case EAST:
					return EAST_AABB;
			}
		}
	}

	public static Direction getConnectedDirection(BlockState blockState) {
		Direction direction = blockState.getValue(FACING);
		return blockState.getValue(TYPE) == ChestType.LEFT ? direction.getClockWise() : direction.getCounterClockWise();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		ChestType chestType = ChestType.SINGLE;
		Direction direction = blockPlaceContext.getHorizontalDirection().getOpposite();
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
		boolean bl = blockPlaceContext.isSecondaryUseActive();
		Direction direction2 = blockPlaceContext.getClickedFace();
		if (direction2.getAxis().isHorizontal() && bl) {
			Direction direction3 = this.candidatePartnerFacing(blockPlaceContext, direction2.getOpposite());
			if (direction3 != null && direction3.getAxis() != direction2.getAxis()) {
				direction = direction3;
				chestType = direction3.getCounterClockWise() == direction2.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
			}
		}

		if (chestType == ChestType.SINGLE && !bl) {
			if (direction == this.candidatePartnerFacing(blockPlaceContext, direction.getClockWise())) {
				chestType = ChestType.LEFT;
			} else if (direction == this.candidatePartnerFacing(blockPlaceContext, direction.getCounterClockWise())) {
				chestType = ChestType.RIGHT;
			}
		}

		return this.defaultBlockState()
			.setValue(FACING, direction)
			.setValue(TYPE, chestType)
			.setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Nullable
	private Direction candidatePartnerFacing(BlockPlaceContext blockPlaceContext, Direction direction) {
		BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().relative(direction));
		return blockState.getBlock() == this && blockState.getValue(TYPE) == ChestType.SINGLE ? blockState.getValue(FACING) : null;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
		if (itemStack.hasCustomHoverName()) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof ChestBlockEntity) {
				((ChestBlockEntity)blockEntity).setCustomName(itemStack.getHoverName());
			}
		}
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (blockState.getBlock() != blockState2.getBlock()) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof Container) {
				Containers.dropContents(level, blockPos, (Container)blockEntity);
				level.updateNeighbourForOutputSignal(blockPos, this);
			}

			super.onRemove(blockState, level, blockPos, blockState2, bl);
		}
	}

	@Override
	public boolean use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		if (level.isClientSide) {
			return true;
		} else {
			MenuProvider menuProvider = this.getMenuProvider(blockState, level, blockPos);
			if (menuProvider != null) {
				player.openMenu(menuProvider);
				player.awardStat(this.getOpenChestStat());
			}

			return true;
		}
	}

	protected Stat<ResourceLocation> getOpenChestStat() {
		return Stats.CUSTOM.get(Stats.OPEN_CHEST);
	}

	@Nullable
	public static <T> T combineWithNeigbour(
		BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, boolean bl, ChestBlock.ChestSearchCallback<T> chestSearchCallback
	) {
		BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
		if (!(blockEntity instanceof ChestBlockEntity)) {
			return null;
		} else if (!bl && isChestBlockedAt(levelAccessor, blockPos)) {
			return null;
		} else {
			ChestBlockEntity chestBlockEntity = (ChestBlockEntity)blockEntity;
			ChestType chestType = blockState.getValue(TYPE);
			if (chestType == ChestType.SINGLE) {
				return chestSearchCallback.acceptSingle(chestBlockEntity);
			} else {
				BlockPos blockPos2 = blockPos.relative(getConnectedDirection(blockState));
				BlockState blockState2 = levelAccessor.getBlockState(blockPos2);
				if (blockState2.getBlock() == blockState.getBlock()) {
					ChestType chestType2 = blockState2.getValue(TYPE);
					if (chestType2 != ChestType.SINGLE && chestType != chestType2 && blockState2.getValue(FACING) == blockState.getValue(FACING)) {
						if (!bl && isChestBlockedAt(levelAccessor, blockPos2)) {
							return null;
						}

						BlockEntity blockEntity2 = levelAccessor.getBlockEntity(blockPos2);
						if (blockEntity2 instanceof ChestBlockEntity) {
							ChestBlockEntity chestBlockEntity2 = chestType == ChestType.RIGHT ? chestBlockEntity : (ChestBlockEntity)blockEntity2;
							ChestBlockEntity chestBlockEntity3 = chestType == ChestType.RIGHT ? (ChestBlockEntity)blockEntity2 : chestBlockEntity;
							return chestSearchCallback.acceptDouble(chestBlockEntity2, chestBlockEntity3);
						}
					}
				}

				return chestSearchCallback.acceptSingle(chestBlockEntity);
			}
		}
	}

	@Nullable
	public static Container getContainer(BlockState blockState, Level level, BlockPos blockPos, boolean bl) {
		return combineWithNeigbour(blockState, level, blockPos, bl, CHEST_COMBINER);
	}

	@Nullable
	@Override
	public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
		return combineWithNeigbour(blockState, level, blockPos, false, MENU_PROVIDER_COMBINER);
	}

	@Override
	public BlockEntity newBlockEntity(BlockGetter blockGetter) {
		return new ChestBlockEntity();
	}

	private static boolean isChestBlockedAt(LevelAccessor levelAccessor, BlockPos blockPos) {
		return isBlockedChestByBlock(levelAccessor, blockPos) || isCatSittingOnChest(levelAccessor, blockPos);
	}

	private static boolean isBlockedChestByBlock(BlockGetter blockGetter, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.above();
		return blockGetter.getBlockState(blockPos2).isRedstoneConductor(blockGetter, blockPos2);
	}

	private static boolean isCatSittingOnChest(LevelAccessor levelAccessor, BlockPos blockPos) {
		List<Cat> list = levelAccessor.getEntitiesOfClass(
			Cat.class,
			new AABB(
				(double)blockPos.getX(),
				(double)(blockPos.getY() + 1),
				(double)blockPos.getZ(),
				(double)(blockPos.getX() + 1),
				(double)(blockPos.getY() + 2),
				(double)(blockPos.getZ() + 1)
			)
		);
		if (!list.isEmpty()) {
			for (Cat cat : list) {
				if (cat.isSitting()) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return AbstractContainerMenu.getRedstoneSignalFromContainer(getContainer(blockState, level, blockPos, false));
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
		builder.add(FACING, TYPE, WATERLOGGED);
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}

	interface ChestSearchCallback<T> {
		T acceptDouble(ChestBlockEntity chestBlockEntity, ChestBlockEntity chestBlockEntity2);

		T acceptSingle(ChestBlockEntity chestBlockEntity);
	}
}
