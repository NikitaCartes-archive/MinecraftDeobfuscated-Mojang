package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ShulkerBoxBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
	public static final int COLUMNS = 9;
	public static final int ROWS = 3;
	public static final int CONTAINER_SIZE = 27;
	public static final int EVENT_SET_OPEN_COUNT = 1;
	public static final int OPENING_TICK_LENGTH = 10;
	public static final float MAX_LID_HEIGHT = 0.5F;
	public static final float MAX_LID_ROTATION = 270.0F;
	public static final String ITEMS_TAG = "Items";
	private static final int[] SLOTS = IntStream.range(0, 27).toArray();
	private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
	private int openCount;
	private ShulkerBoxBlockEntity.AnimationStatus animationStatus = ShulkerBoxBlockEntity.AnimationStatus.CLOSED;
	private float progress;
	private float progressOld;
	@Nullable
	private final DyeColor color;

	public ShulkerBoxBlockEntity(@Nullable DyeColor dyeColor, BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.SHULKER_BOX, blockPos, blockState);
		this.color = dyeColor;
	}

	public ShulkerBoxBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.SHULKER_BOX, blockPos, blockState);
		this.color = ShulkerBoxBlock.getColorFromBlock(blockState.getBlock());
	}

	public static void tick(Level level, BlockPos blockPos, BlockState blockState, ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
		shulkerBoxBlockEntity.updateAnimation(level, blockPos, blockState);
	}

	private void updateAnimation(Level level, BlockPos blockPos, BlockState blockState) {
		this.progressOld = this.progress;
		switch (this.animationStatus) {
			case CLOSED:
				this.progress = 0.0F;
				break;
			case OPENING:
				this.progress += 0.1F;
				if (this.progressOld == 0.0F) {
					doNeighborUpdates(level, blockPos, blockState);
				}

				if (this.progress >= 1.0F) {
					this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.OPENED;
					this.progress = 1.0F;
					doNeighborUpdates(level, blockPos, blockState);
				}

				this.moveCollidedEntities(level, blockPos, blockState);
				break;
			case CLOSING:
				this.progress -= 0.1F;
				if (this.progressOld == 1.0F) {
					doNeighborUpdates(level, blockPos, blockState);
				}

				if (this.progress <= 0.0F) {
					this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.CLOSED;
					this.progress = 0.0F;
					doNeighborUpdates(level, blockPos, blockState);
				}
				break;
			case OPENED:
				this.progress = 1.0F;
		}
	}

	public ShulkerBoxBlockEntity.AnimationStatus getAnimationStatus() {
		return this.animationStatus;
	}

	public AABB getBoundingBox(BlockState blockState) {
		return Shulker.getProgressAabb(blockState.getValue(ShulkerBoxBlock.FACING), 0.5F * this.getProgress(1.0F));
	}

	private void moveCollidedEntities(Level level, BlockPos blockPos, BlockState blockState) {
		if (blockState.getBlock() instanceof ShulkerBoxBlock) {
			Direction direction = blockState.getValue(ShulkerBoxBlock.FACING);
			AABB aABB = Shulker.getProgressDeltaAabb(direction, this.progressOld, this.progress).move(blockPos);
			List<Entity> list = level.getEntities(null, aABB);
			if (!list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					Entity entity = (Entity)list.get(i);
					if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
						entity.move(
							MoverType.SHULKER_BOX,
							new Vec3(
								(aABB.getXsize() + 0.01) * (double)direction.getStepX(),
								(aABB.getYsize() + 0.01) * (double)direction.getStepY(),
								(aABB.getZsize() + 0.01) * (double)direction.getStepZ()
							)
						);
					}
				}
			}
		}
	}

	@Override
	public int getContainerSize() {
		return this.itemStacks.size();
	}

	@Override
	public boolean triggerEvent(int i, int j) {
		if (i == 1) {
			this.openCount = j;
			if (j == 0) {
				this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.CLOSING;
			}

			if (j == 1) {
				this.animationStatus = ShulkerBoxBlockEntity.AnimationStatus.OPENING;
			}

			return true;
		} else {
			return super.triggerEvent(i, j);
		}
	}

	private static void doNeighborUpdates(Level level, BlockPos blockPos, BlockState blockState) {
		blockState.updateNeighbourShapes(level, blockPos, 3);
		level.updateNeighborsAt(blockPos, blockState.getBlock());
	}

	@Override
	public void startOpen(Player player) {
		if (!this.remove && !player.isSpectator()) {
			if (this.openCount < 0) {
				this.openCount = 0;
			}

			this.openCount++;
			this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
			if (this.openCount == 1) {
				this.level.gameEvent(player, GameEvent.CONTAINER_OPEN, this.worldPosition);
				this.level.playSound(null, this.worldPosition, SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
			}
		}
	}

	@Override
	public void stopOpen(Player player) {
		if (!this.remove && !player.isSpectator()) {
			this.openCount--;
			this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
			if (this.openCount <= 0) {
				this.level.gameEvent(player, GameEvent.CONTAINER_CLOSE, this.worldPosition);
				this.level.playSound(null, this.worldPosition, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
			}
		}
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.shulkerBox");
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.loadFromTag(compoundTag);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		super.saveAdditional(compoundTag);
		if (!this.trySaveLootTable(compoundTag)) {
			ContainerHelper.saveAllItems(compoundTag, this.itemStacks, false);
		}
	}

	public void loadFromTag(CompoundTag compoundTag) {
		this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(compoundTag) && compoundTag.contains("Items", 9)) {
			ContainerHelper.loadAllItems(compoundTag, this.itemStacks);
		}
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.itemStacks;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> nonNullList) {
		this.itemStacks = nonNullList;
	}

	@Override
	public int[] getSlotsForFace(Direction direction) {
		return SLOTS;
	}

	@Override
	public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
		return !(Block.byItem(itemStack.getItem()) instanceof ShulkerBoxBlock);
	}

	@Override
	public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
		return true;
	}

	public float getProgress(float f) {
		return Mth.lerp(f, this.progressOld, this.progress);
	}

	@Nullable
	public DyeColor getColor() {
		return this.color;
	}

	@Override
	protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
		return new ShulkerBoxMenu(i, inventory, this);
	}

	public boolean isClosed() {
		return this.animationStatus == ShulkerBoxBlockEntity.AnimationStatus.CLOSED;
	}

	public static enum AnimationStatus {
		CLOSED,
		OPENING,
		OPENED,
		CLOSING;
	}
}
