package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeCache;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CrafterBlock extends BaseEntityBlock {
	public static final MapCodec<CrafterBlock> CODEC = simpleCodec(CrafterBlock::new);
	public static final BooleanProperty CRAFTING = BlockStateProperties.CRAFTING;
	public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
	private static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;
	private static final int MAX_CRAFTING_TICKS = 6;
	private static final RecipeCache RECIPE_CACHE = new RecipeCache(10);

	public CrafterBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(ORIENTATION, FrontAndTop.NORTH_UP)
				.setValue(TRIGGERED, Boolean.valueOf(false))
				.setValue(CRAFTING, Boolean.valueOf(false))
		);
	}

	@Override
	protected MapCodec<CrafterBlock> codec() {
		return CODEC;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return level.getBlockEntity(blockPos) instanceof CrafterBlockEntity crafterBlockEntity ? crafterBlockEntity.getRedstoneSignal() : 0;
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		boolean bl2 = level.hasNeighborSignal(blockPos);
		boolean bl3 = (Boolean)blockState.getValue(TRIGGERED);
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (bl2 && !bl3) {
			level.scheduleTick(blockPos, this, 1);
			level.setBlock(blockPos, blockState.setValue(TRIGGERED, Boolean.valueOf(true)), 2);
			this.setBlockEntityTriggered(blockEntity, true);
		} else if (!bl2 && bl3) {
			level.setBlock(blockPos, blockState.setValue(TRIGGERED, Boolean.valueOf(false)).setValue(CRAFTING, Boolean.valueOf(false)), 2);
			this.setBlockEntityTriggered(blockEntity, false);
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		this.dispenseFrom(blockState, serverLevel, blockPos);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return level.isClientSide ? null : createTickerHelper(blockEntityType, BlockEntityType.CRAFTER, CrafterBlockEntity::serverTick);
	}

	private void setBlockEntityTriggered(@Nullable BlockEntity blockEntity, boolean bl) {
		if (blockEntity instanceof CrafterBlockEntity crafterBlockEntity) {
			crafterBlockEntity.setTriggered(bl);
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		CrafterBlockEntity crafterBlockEntity = new CrafterBlockEntity(blockPos, blockState);
		crafterBlockEntity.setTriggered(blockState.hasProperty(TRIGGERED) && (Boolean)blockState.getValue(TRIGGERED));
		return crafterBlockEntity;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		Direction direction = blockPlaceContext.getNearestLookingDirection().getOpposite();

		Direction direction2 = switch (direction) {
			case DOWN -> blockPlaceContext.getHorizontalDirection().getOpposite();
			case UP -> blockPlaceContext.getHorizontalDirection();
			case NORTH, SOUTH, WEST, EAST -> Direction.UP;
		};
		return this.defaultBlockState()
			.setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(direction, direction2))
			.setValue(TRIGGERED, Boolean.valueOf(blockPlaceContext.getLevel().hasNeighborSignal(blockPlaceContext.getClickedPos())));
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
		if (itemStack.hasCustomHoverName() && level.getBlockEntity(blockPos) instanceof CrafterBlockEntity crafterBlockEntity) {
			crafterBlockEntity.setCustomName(itemStack.getHoverName());
		}

		if ((Boolean)blockState.getValue(TRIGGERED)) {
			level.scheduleTick(blockPos, this, 1);
		}
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		Containers.dropContentsOnDestroy(blockState, blockState2, level, blockPos);
		super.onRemove(blockState, level, blockPos, blockState2, bl);
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof CrafterBlockEntity) {
				player.openMenu((CrafterBlockEntity)blockEntity);
			}

			return InteractionResult.CONSUME;
		}
	}

	protected void dispenseFrom(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos) {
		if (serverLevel.getBlockEntity(blockPos) instanceof CrafterBlockEntity crafterBlockEntity) {
			Optional<CraftingRecipe> optional = getPotentialResults(serverLevel, crafterBlockEntity);
			if (optional.isEmpty()) {
				serverLevel.levelEvent(1050, blockPos, 0);
			} else {
				crafterBlockEntity.setCraftingTicksRemaining(6);
				serverLevel.setBlock(blockPos, blockState.setValue(CRAFTING, Boolean.valueOf(true)), 2);
				CraftingRecipe craftingRecipe = (CraftingRecipe)optional.get();
				ItemStack itemStack = craftingRecipe.assemble(crafterBlockEntity, serverLevel.registryAccess());
				itemStack.onCraftedBySystem(serverLevel);
				this.dispenseItem(serverLevel, blockPos, crafterBlockEntity, itemStack, blockState);
				craftingRecipe.getRemainingItems(crafterBlockEntity)
					.forEach(itemStackx -> this.dispenseItem(serverLevel, blockPos, crafterBlockEntity, itemStackx, blockState));
				crafterBlockEntity.getItems().forEach(itemStackx -> {
					if (!itemStackx.isEmpty()) {
						itemStackx.shrink(1);
					}
				});
				crafterBlockEntity.setChanged();
			}
		}
	}

	public static Optional<CraftingRecipe> getPotentialResults(Level level, CraftingContainer craftingContainer) {
		return RECIPE_CACHE.get(level, craftingContainer);
	}

	private void dispenseItem(Level level, BlockPos blockPos, CrafterBlockEntity crafterBlockEntity, ItemStack itemStack, BlockState blockState) {
		Direction direction = ((FrontAndTop)blockState.getValue(ORIENTATION)).front();
		Container container = HopperBlockEntity.getContainerAt(level, blockPos.relative(direction));
		ItemStack itemStack2 = itemStack.copy();
		if (container instanceof CrafterBlockEntity) {
			while (!itemStack2.isEmpty()) {
				ItemStack itemStack3 = itemStack2.copyWithCount(1);
				ItemStack itemStack4 = HopperBlockEntity.addItem(crafterBlockEntity, container, itemStack3, direction.getOpposite());
				if (!itemStack4.isEmpty()) {
					break;
				}

				itemStack2.shrink(1);
			}
		} else if (container != null) {
			while (!itemStack2.isEmpty()) {
				int i = itemStack2.getCount();
				itemStack2 = HopperBlockEntity.addItem(crafterBlockEntity, container, itemStack2, direction.getOpposite());
				if (i == itemStack2.getCount()) {
					break;
				}
			}
		}

		if (!itemStack2.isEmpty()) {
			Vec3 vec3 = Vec3.atCenterOf(blockPos).relative(direction, 0.7);
			DefaultDispenseItemBehavior.spawnItem(level, itemStack2, 6, direction, vec3);
			level.levelEvent(1049, blockPos, 0);
			level.levelEvent(2010, blockPos, direction.get3DDataValue());
		}
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(ORIENTATION, rotation.rotation().rotate(blockState.getValue(ORIENTATION)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.setValue(ORIENTATION, mirror.rotation().rotate(blockState.getValue(ORIENTATION)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ORIENTATION, TRIGGERED, CRAFTING);
	}
}
