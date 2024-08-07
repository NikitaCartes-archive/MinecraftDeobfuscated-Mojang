package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShulkerBoxBlock extends BaseEntityBlock {
	public static final MapCodec<ShulkerBoxBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					DyeColor.CODEC.optionalFieldOf("color").forGetter(shulkerBoxBlock -> Optional.ofNullable(shulkerBoxBlock.color)), propertiesCodec()
				)
				.apply(instance, (optional, properties) -> new ShulkerBoxBlock((DyeColor)optional.orElse(null), properties))
	);
	private static final Component UNKNOWN_CONTENTS = Component.translatable("container.shulkerBox.unknownContents");
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
	public static final ResourceLocation CONTENTS = ResourceLocation.withDefaultNamespace("contents");
	@Nullable
	private final DyeColor color;

	@Override
	public MapCodec<ShulkerBoxBlock> codec() {
		return CODEC;
	}

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
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
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
			AABB aABB = Shulker.getProgressDeltaAabb(1.0F, blockState.getValue(FACING), 0.0F, 0.5F).move(blockPos).deflate(1.0E-6);
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
	public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
			if (!level.isClientSide && player.isCreative() && !shulkerBoxBlockEntity.isEmpty()) {
				ItemStack itemStack = getColoredItemStack(this.getColor());
				itemStack.applyComponents(blockEntity.collectComponents());
				ItemEntity itemEntity = new ItemEntity(level, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, itemStack);
				itemEntity.setDefaultPickUpDelay();
				level.addFreshEntity(itemEntity);
			} else {
				shulkerBoxBlockEntity.unpackLootTable(player);
			}
		}

		return super.playerWillDestroy(level, blockPos, blockState, player);
	}

	@Override
	protected List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
		BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (blockEntity instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
			builder = builder.withDynamicDrop(CONTENTS, consumer -> {
				for (int i = 0; i < shulkerBoxBlockEntity.getContainerSize(); i++) {
					consumer.accept(shulkerBoxBlockEntity.getItem(i));
				}
			});
		}

		return super.getDrops(blockState, builder);
	}

	@Override
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			super.onRemove(blockState, level, blockPos, blockState2, bl);
			if (blockEntity instanceof ShulkerBoxBlockEntity) {
				level.updateNeighbourForOutputSignal(blockPos, blockState.getBlock());
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
		if (itemStack.has(DataComponents.CONTAINER_LOOT)) {
			list.add(UNKNOWN_CONTENTS);
		}

		int i = 0;
		int j = 0;

		for (ItemStack itemStack2 : itemStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItems()) {
			j++;
			if (i <= 4) {
				i++;
				list.add(Component.translatable("container.shulkerBox.itemCount", itemStack2.getHoverName(), itemStack2.getCount()));
			}
		}

		if (j - i > 0) {
			list.add(Component.translatable("container.shulkerBox.more", j - i).withStyle(ChatFormatting.ITALIC));
		}
	}

	@Override
	protected VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		if (blockGetter.getBlockEntity(blockPos) instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity && !shulkerBoxBlockEntity.isClosed()) {
			return (VoxelShape)OPEN_SHAPE_BY_DIRECTION.get(((Direction)blockState.getValue(FACING)).getOpposite());
		}

		return Shapes.block();
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
		return blockEntity instanceof ShulkerBoxBlockEntity ? Shapes.create(((ShulkerBoxBlockEntity)blockEntity).getBoundingBox(blockState)) : Shapes.block();
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return false;
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(blockPos));
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		ItemStack itemStack = super.getCloneItemStack(levelReader, blockPos, blockState);
		levelReader.getBlockEntity(blockPos, BlockEntityType.SHULKER_BOX)
			.ifPresent(shulkerBoxBlockEntity -> shulkerBoxBlockEntity.saveToItem(itemStack, levelReader.registryAccess()));
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
			return switch (dyeColor) {
				case WHITE -> Blocks.WHITE_SHULKER_BOX;
				case ORANGE -> Blocks.ORANGE_SHULKER_BOX;
				case MAGENTA -> Blocks.MAGENTA_SHULKER_BOX;
				case LIGHT_BLUE -> Blocks.LIGHT_BLUE_SHULKER_BOX;
				case YELLOW -> Blocks.YELLOW_SHULKER_BOX;
				case LIME -> Blocks.LIME_SHULKER_BOX;
				case PINK -> Blocks.PINK_SHULKER_BOX;
				case GRAY -> Blocks.GRAY_SHULKER_BOX;
				case LIGHT_GRAY -> Blocks.LIGHT_GRAY_SHULKER_BOX;
				case CYAN -> Blocks.CYAN_SHULKER_BOX;
				case BLUE -> Blocks.BLUE_SHULKER_BOX;
				case BROWN -> Blocks.BROWN_SHULKER_BOX;
				case GREEN -> Blocks.GREEN_SHULKER_BOX;
				case RED -> Blocks.RED_SHULKER_BOX;
				case BLACK -> Blocks.BLACK_SHULKER_BOX;
				case PURPLE -> Blocks.PURPLE_SHULKER_BOX;
			};
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
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}
}
