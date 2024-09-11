package net.minecraft.world.item;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;

public class BlockItem extends Item {
	@Deprecated
	private final Block block;

	public BlockItem(Block block, Item.Properties properties) {
		super(properties);
		this.block = block;
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		InteractionResult interactionResult = this.place(new BlockPlaceContext(useOnContext));
		return !interactionResult.consumesAction() && useOnContext.getItemInHand().has(DataComponents.CONSUMABLE)
			? super.use(useOnContext.getLevel(), useOnContext.getPlayer(), useOnContext.getHand())
			: interactionResult;
	}

	public InteractionResult place(BlockPlaceContext blockPlaceContext) {
		if (!this.getBlock().isEnabled(blockPlaceContext.getLevel().enabledFeatures())) {
			return InteractionResult.FAIL;
		} else if (!blockPlaceContext.canPlace()) {
			return InteractionResult.FAIL;
		} else {
			BlockPlaceContext blockPlaceContext2 = this.updatePlacementContext(blockPlaceContext);
			if (blockPlaceContext2 == null) {
				return InteractionResult.FAIL;
			} else {
				BlockState blockState = this.getPlacementState(blockPlaceContext2);
				if (blockState == null) {
					return InteractionResult.FAIL;
				} else if (!this.placeBlock(blockPlaceContext2, blockState)) {
					return InteractionResult.FAIL;
				} else {
					BlockPos blockPos = blockPlaceContext2.getClickedPos();
					Level level = blockPlaceContext2.getLevel();
					Player player = blockPlaceContext2.getPlayer();
					ItemStack itemStack = blockPlaceContext2.getItemInHand();
					BlockState blockState2 = level.getBlockState(blockPos);
					if (blockState2.is(blockState.getBlock())) {
						blockState2 = this.updateBlockStateFromTag(blockPos, level, itemStack, blockState2);
						this.updateCustomBlockEntityTag(blockPos, level, player, itemStack, blockState2);
						updateBlockEntityComponents(level, blockPos, itemStack);
						blockState2.getBlock().setPlacedBy(level, blockPos, blockState2, player, itemStack);
						if (player instanceof ServerPlayer) {
							CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
						}
					}

					SoundType soundType = blockState2.getSoundType();
					level.playSound(player, blockPos, this.getPlaceSound(blockState2), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
					level.gameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Context.of(player, blockState2));
					itemStack.consume(1, player);
					return InteractionResult.SUCCESS;
				}
			}
		}
	}

	protected SoundEvent getPlaceSound(BlockState blockState) {
		return blockState.getSoundType().getPlaceSound();
	}

	@Nullable
	public BlockPlaceContext updatePlacementContext(BlockPlaceContext blockPlaceContext) {
		return blockPlaceContext;
	}

	private static void updateBlockEntityComponents(Level level, BlockPos blockPos, ItemStack itemStack) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity != null) {
			blockEntity.applyComponentsFromItemStack(itemStack);
			blockEntity.setChanged();
		}
	}

	protected boolean updateCustomBlockEntityTag(BlockPos blockPos, Level level, @Nullable Player player, ItemStack itemStack, BlockState blockState) {
		return updateCustomBlockEntityTag(level, player, blockPos, itemStack);
	}

	@Nullable
	protected BlockState getPlacementState(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = this.getBlock().getStateForPlacement(blockPlaceContext);
		return blockState != null && this.canPlace(blockPlaceContext, blockState) ? blockState : null;
	}

	private BlockState updateBlockStateFromTag(BlockPos blockPos, Level level, ItemStack itemStack, BlockState blockState) {
		BlockItemStateProperties blockItemStateProperties = itemStack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
		if (blockItemStateProperties.isEmpty()) {
			return blockState;
		} else {
			BlockState blockState2 = blockItemStateProperties.apply(blockState);
			if (blockState2 != blockState) {
				level.setBlock(blockPos, blockState2, 2);
			}

			return blockState2;
		}
	}

	protected boolean canPlace(BlockPlaceContext blockPlaceContext, BlockState blockState) {
		Player player = blockPlaceContext.getPlayer();
		CollisionContext collisionContext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
		return (!this.mustSurvive() || blockState.canSurvive(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos()))
			&& blockPlaceContext.getLevel().isUnobstructed(blockState, blockPlaceContext.getClickedPos(), collisionContext);
	}

	protected boolean mustSurvive() {
		return true;
	}

	protected boolean placeBlock(BlockPlaceContext blockPlaceContext, BlockState blockState) {
		return blockPlaceContext.getLevel().setBlock(blockPlaceContext.getClickedPos(), blockState, 11);
	}

	public static boolean updateCustomBlockEntityTag(Level level, @Nullable Player player, BlockPos blockPos, ItemStack itemStack) {
		MinecraftServer minecraftServer = level.getServer();
		if (minecraftServer == null) {
			return false;
		} else {
			CustomData customData = itemStack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
			if (!customData.isEmpty()) {
				BlockEntity blockEntity = level.getBlockEntity(blockPos);
				if (blockEntity != null) {
					if (level.isClientSide || !blockEntity.onlyOpCanSetNbt() || player != null && player.canUseGameMasterBlocks()) {
						return customData.loadInto(blockEntity, level.registryAccess());
					}

					return false;
				}
			}

			return false;
		}
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
		this.getBlock().appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
	}

	public Block getBlock() {
		return this.block;
	}

	public void registerBlocks(Map<Block, Item> map, Item item) {
		map.put(this.getBlock(), item);
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return !(this.getBlock() instanceof ShulkerBoxBlock);
	}

	@Override
	public void onDestroyed(ItemEntity itemEntity) {
		ItemContainerContents itemContainerContents = itemEntity.getItem().set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		if (itemContainerContents != null) {
			ItemUtils.onContainerDestroyed(itemEntity, itemContainerContents.nonEmptyItemsCopy());
		}
	}

	public static void setBlockEntityData(ItemStack itemStack, BlockEntityType<?> blockEntityType, CompoundTag compoundTag) {
		compoundTag.remove("id");
		if (compoundTag.isEmpty()) {
			itemStack.remove(DataComponents.BLOCK_ENTITY_DATA);
		} else {
			BlockEntity.addEntityType(compoundTag, blockEntityType);
			itemStack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(compoundTag));
		}
	}

	@Override
	public FeatureFlagSet requiredFeatures() {
		return this.getBlock().requiredFeatures();
	}
}
