package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeCache;
import net.minecraft.world.item.crafting.RecipeHolder;
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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CrafterBlock extends BaseEntityBlock {
	public static final MapCodec<CrafterBlock> CODEC = simpleCodec(CrafterBlock::new);
	public static final BooleanProperty CRAFTING = BlockStateProperties.CRAFTING;
	public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
	private static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;
	private static final int MAX_CRAFTING_TICKS = 6;
	private static final int CRAFTING_TICK_DELAY = 4;
	private static final RecipeCache RECIPE_CACHE = new RecipeCache(10);
	private static final int CRAFTER_ADVANCEMENT_DIAMETER = 17;

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
	protected boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return level.getBlockEntity(blockPos) instanceof CrafterBlockEntity crafterBlockEntity ? crafterBlockEntity.getRedstoneSignal() : 0;
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
		boolean bl2 = level.hasNeighborSignal(blockPos);
		boolean bl3 = (Boolean)blockState.getValue(TRIGGERED);
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (bl2 && !bl3) {
			level.scheduleTick(blockPos, this, 4);
			level.setBlock(blockPos, blockState.setValue(TRIGGERED, Boolean.valueOf(true)), 2);
			this.setBlockEntityTriggered(blockEntity, true);
		} else if (!bl2 && bl3) {
			level.setBlock(blockPos, blockState.setValue(TRIGGERED, Boolean.valueOf(false)).setValue(CRAFTING, Boolean.valueOf(false)), 2);
			this.setBlockEntityTriggered(blockEntity, false);
		}
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
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
		if ((Boolean)blockState.getValue(TRIGGERED)) {
			level.scheduleTick(blockPos, this, 4);
		}
	}

	@Override
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		Containers.dropContentsOnDestroy(blockState, blockState2, level, blockPos);
		super.onRemove(blockState, level, blockPos, blockState2, bl);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (!level.isClientSide && level.getBlockEntity(blockPos) instanceof CrafterBlockEntity crafterBlockEntity) {
			player.openMenu(crafterBlockEntity);
		}

		return InteractionResult.SUCCESS;
	}

	protected void dispenseFrom(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos) {
		if (serverLevel.getBlockEntity(blockPos) instanceof CrafterBlockEntity crafterBlockEntity) {
			CraftingInput var11 = crafterBlockEntity.asCraftInput();
			Optional<RecipeHolder<CraftingRecipe>> optional = getPotentialResults(serverLevel, var11);
			if (optional.isEmpty()) {
				serverLevel.levelEvent(1050, blockPos, 0);
			} else {
				RecipeHolder<CraftingRecipe> recipeHolder = (RecipeHolder<CraftingRecipe>)optional.get();
				ItemStack itemStack = recipeHolder.value().assemble(var11, serverLevel.registryAccess());
				if (itemStack.isEmpty()) {
					serverLevel.levelEvent(1050, blockPos, 0);
				} else {
					crafterBlockEntity.setCraftingTicksRemaining(6);
					serverLevel.setBlock(blockPos, blockState.setValue(CRAFTING, Boolean.valueOf(true)), 2);
					itemStack.onCraftedBySystem(serverLevel);
					this.dispenseItem(serverLevel, blockPos, crafterBlockEntity, itemStack, blockState, recipeHolder);

					for (ItemStack itemStack2 : recipeHolder.value().getRemainingItems(var11)) {
						if (!itemStack2.isEmpty()) {
							this.dispenseItem(serverLevel, blockPos, crafterBlockEntity, itemStack2, blockState, recipeHolder);
						}
					}

					crafterBlockEntity.getItems().forEach(itemStackx -> {
						if (!itemStackx.isEmpty()) {
							itemStackx.shrink(1);
						}
					});
					crafterBlockEntity.setChanged();
				}
			}
		}
	}

	public static Optional<RecipeHolder<CraftingRecipe>> getPotentialResults(ServerLevel serverLevel, CraftingInput craftingInput) {
		return RECIPE_CACHE.get(serverLevel, craftingInput);
	}

	private void dispenseItem(
		ServerLevel serverLevel, BlockPos blockPos, CrafterBlockEntity crafterBlockEntity, ItemStack itemStack, BlockState blockState, RecipeHolder<?> recipeHolder
	) {
		Direction direction = ((FrontAndTop)blockState.getValue(ORIENTATION)).front();
		Container container = HopperBlockEntity.getContainerAt(serverLevel, blockPos.relative(direction));
		ItemStack itemStack2 = itemStack.copy();
		if (container != null && (container instanceof CrafterBlockEntity || itemStack.getCount() > container.getMaxStackSize(itemStack))) {
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
			Vec3 vec3 = Vec3.atCenterOf(blockPos);
			Vec3 vec32 = vec3.relative(direction, 0.7);
			DefaultDispenseItemBehavior.spawnItem(serverLevel, itemStack2, 6, direction, vec32);

			for (ServerPlayer serverPlayer : serverLevel.getEntitiesOfClass(ServerPlayer.class, AABB.ofSize(vec3, 17.0, 17.0, 17.0))) {
				CriteriaTriggers.CRAFTER_RECIPE_CRAFTED.trigger(serverPlayer, recipeHolder.id(), crafterBlockEntity.getItems());
			}

			serverLevel.levelEvent(1049, blockPos, 0);
			serverLevel.levelEvent(2010, blockPos, direction.get3DDataValue());
		}
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(ORIENTATION, rotation.rotation().rotate(blockState.getValue(ORIENTATION)));
	}

	@Override
	protected BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.setValue(ORIENTATION, mirror.rotation().rotate(blockState.getValue(ORIENTATION)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ORIENTATION, TRIGGERED, CRAFTING);
	}
}
