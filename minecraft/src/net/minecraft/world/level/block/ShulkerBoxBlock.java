package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShulkerBoxBlock extends BaseEntityBlock {
	private static final float OPEN_AABB_SIZE = 1.0F;
	private static final VoxelShape UP_OPEN_AABB = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape DOWN_OPEN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
	private static final VoxelShape WES_OPEN_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
	private static final VoxelShape EAST_OPEN_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape NORTH_OPEN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
	private static final VoxelShape SOUTH_OPEN_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
	private static final Map<Direction, VoxelShape> OPEN_SHAPE_BY_DIRECTION = Util.make(Maps.newEnumMap(Direction.class), enumMap -> {
		enumMap.put(Direction.NORTH, NORTH_OPEN_AABB);
		enumMap.put(Direction.EAST, EAST_OPEN_AABB);
		enumMap.put(Direction.SOUTH, SOUTH_OPEN_AABB);
		enumMap.put(Direction.WEST, WES_OPEN_AABB);
		enumMap.put(Direction.UP, UP_OPEN_AABB);
		enumMap.put(Direction.DOWN, DOWN_OPEN_AABB);
	});
	public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;
	public static final ResourceLocation CONTENTS = new ResourceLocation("contents");
	@Nullable
	private final DyeColor color;

	public ShulkerBoxBlock(@Nullable DyeColor dyeColor, BlockBehaviour.Properties properties) {
		super(properties);
		this.color = dyeColor;
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new ShulkerBoxBlockEntity(this.color, blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return createTickerHelper(blockEntityType, BlockEntityType.SHULKER_BOX, ShulkerBoxBlockEntity::tick);
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else if (player.isSpectator()) {
			return InteractionResult.CONSUME;
		} else if (level.getBlockEntity(blockPos) instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
			if (canOpen(blockState, level, blockPos, shulkerBoxBlockEntity)) {
				player.openMenu(shulkerBoxBlockEntity);
				player.awardStat(Stats.OPEN_SHULKER_BOX);
				PiglinAi.angerNearbyPiglins(player, true);
			}

			return InteractionResult.CONSUME;
		} else {
			return InteractionResult.PASS;
		}
	}

	private static boolean canOpen(BlockState blockState, Level level, BlockPos blockPos, ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
		if (shulkerBoxBlockEntity.getAnimationStatus() != ShulkerBoxBlockEntity.AnimationStatus.CLOSED) {
			return true;
		} else {
			AABB aABB = Shulker.getProgressDeltaAabb(blockState.getValue(FACING), 0.0F, 0.5F).move(blockPos).deflate(1.0E-6);
			return level.noCollision(aABB);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getClickedFace());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
			if (!level.isClientSide && player.isCreative() && !shulkerBoxBlockEntity.isEmpty()) {
				ItemStack itemStack = getColoredItemStack(this.getColor());
				blockEntity.saveToItem(itemStack);
				if (shulkerBoxBlockEntity.hasCustomName()) {
					itemStack.setHoverName(shulkerBoxBlockEntity.getCustomName());
				}

				ItemEntity itemEntity = new ItemEntity(level, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, itemStack);
				itemEntity.setDefaultPickUpDelay();
				level.addFreshEntity(itemEntity);
			} else {
				shulkerBoxBlockEntity.unpackLootTable(player);
			}
		}

		super.playerWillDestroy(level, blockPos, blockState, player);
	}

	@Override
	public List<ItemStack> getDrops(BlockState blockState, LootContext.Builder builder) {
		BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (blockEntity instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
			builder = builder.withDynamicDrop(CONTENTS, (lootContext, consumer) -> {
				for (int i = 0; i < shulkerBoxBlockEntity.getContainerSize(); i++) {
					consumer.accept(shulkerBoxBlockEntity.getItem(i));
				}
			});
		}

		return super.getDrops(blockState, builder);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
		if (itemStack.hasCustomHoverName()) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof ShulkerBoxBlockEntity) {
				((ShulkerBoxBlockEntity)blockEntity).setCustomName(itemStack.getHoverName());
			}
		}
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof ShulkerBoxBlockEntity) {
				level.updateNeighbourForOutputSignal(blockPos, blockState.getBlock());
			}

			super.onRemove(blockState, level, blockPos, blockState2, bl);
		}
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, blockGetter, list, tooltipFlag);
		CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
		if (compoundTag != null) {
			if (compoundTag.contains("LootTable", 8)) {
				list.add(Component.literal("???????"));
			}

			if (compoundTag.contains("Items", 9)) {
				NonNullList<ItemStack> nonNullList = NonNullList.withSize(27, ItemStack.EMPTY);
				ContainerHelper.loadAllItems(compoundTag, nonNullList);
				int i = 0;
				int j = 0;

				for (ItemStack itemStack2 : nonNullList) {
					if (!itemStack2.isEmpty()) {
						j++;
						if (i <= 4) {
							i++;
							MutableComponent mutableComponent = itemStack2.getHoverName().copy();
							mutableComponent.append(" x").append(String.valueOf(itemStack2.getCount()));
							list.add(mutableComponent);
						}
					}
				}

				if (j - i > 0) {
					list.add(Component.translatable("container.shulkerBox.more", j - i).withStyle(ChatFormatting.ITALIC));
				}
			}
		}
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState blockState) {
		return PushReaction.DESTROY;
	}

	@Override
	public VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		if (blockGetter.getBlockEntity(blockPos) instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity && !shulkerBoxBlockEntity.isClosed()) {
			return (VoxelShape)OPEN_SHAPE_BY_DIRECTION.get(((Direction)blockState.getValue(FACING)).getOpposite());
		}

		return Shapes.block();
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
		return blockEntity instanceof ShulkerBoxBlockEntity ? Shapes.create(((ShulkerBoxBlockEntity)blockEntity).getBoundingBox(blockState)) : Shapes.block();
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)level.getBlockEntity(blockPos));
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		ItemStack itemStack = super.getCloneItemStack(blockGetter, blockPos, blockState);
		blockGetter.getBlockEntity(blockPos, BlockEntityType.SHULKER_BOX).ifPresent(shulkerBoxBlockEntity -> shulkerBoxBlockEntity.saveToItem(itemStack));
		return itemStack;
	}

	@Nullable
	public static DyeColor getColorFromItem(Item item) {
		return getColorFromBlock(Block.byItem(item));
	}

	@Nullable
	public static DyeColor getColorFromBlock(Block block) {
		return block instanceof ShulkerBoxBlock ? ((ShulkerBoxBlock)block).getColor() : null;
	}

	public static Block getBlockByColor(@Nullable DyeColor dyeColor) {
		if (dyeColor == null) {
			return Blocks.SHULKER_BOX;
		} else {
			switch (dyeColor) {
				case WHITE:
					return Blocks.WHITE_SHULKER_BOX;
				case ORANGE:
					return Blocks.ORANGE_SHULKER_BOX;
				case MAGENTA:
					return Blocks.MAGENTA_SHULKER_BOX;
				case LIGHT_BLUE:
					return Blocks.LIGHT_BLUE_SHULKER_BOX;
				case YELLOW:
					return Blocks.YELLOW_SHULKER_BOX;
				case LIME:
					return Blocks.LIME_SHULKER_BOX;
				case PINK:
					return Blocks.PINK_SHULKER_BOX;
				case GRAY:
					return Blocks.GRAY_SHULKER_BOX;
				case LIGHT_GRAY:
					return Blocks.LIGHT_GRAY_SHULKER_BOX;
				case CYAN:
					return Blocks.CYAN_SHULKER_BOX;
				case PURPLE:
				default:
					return Blocks.PURPLE_SHULKER_BOX;
				case BLUE:
					return Blocks.BLUE_SHULKER_BOX;
				case BROWN:
					return Blocks.BROWN_SHULKER_BOX;
				case GREEN:
					return Blocks.GREEN_SHULKER_BOX;
				case RED:
					return Blocks.RED_SHULKER_BOX;
				case BLACK:
					return Blocks.BLACK_SHULKER_BOX;
			}
		}
	}

	@Nullable
	public DyeColor getColor() {
		return this.color;
	}

	public static ItemStack getColoredItemStack(@Nullable DyeColor dyeColor) {
		return new ItemStack(getBlockByColor(dyeColor));
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}
}
