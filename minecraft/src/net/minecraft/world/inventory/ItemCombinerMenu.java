package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ItemCombinerMenu extends AbstractContainerMenu {
	protected final ResultContainer resultSlots = new ResultContainer();
	protected final Container inputSlots = new SimpleContainer(2) {
		@Override
		public void setChanged() {
			super.setChanged();
			ItemCombinerMenu.this.slotsChanged(this);
		}
	};
	protected final ContainerLevelAccess access;
	protected final Player player;

	protected abstract boolean mayPickup(Player player, boolean bl);

	protected abstract ItemStack onTake(Player player, ItemStack itemStack);

	protected abstract boolean isValidBlock(BlockState blockState);

	public ItemCombinerMenu(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
		super(menuType, i);
		this.access = containerLevelAccess;
		this.player = inventory.player;
		this.addSlot(new Slot(this.inputSlots, 0, 27, 47));
		this.addSlot(new Slot(this.inputSlots, 1, 76, 47));
		this.addSlot(new Slot(this.resultSlots, 2, 134, 47) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				return false;
			}

			@Override
			public boolean mayPickup(Player player) {
				return ItemCombinerMenu.this.mayPickup(player, this.hasItem());
			}

			@Override
			public ItemStack onTake(Player player, ItemStack itemStack) {
				return ItemCombinerMenu.this.onTake(player, itemStack);
			}
		});

		for (int j = 0; j < 3; j++) {
			for (int k = 0; k < 9; k++) {
				this.addSlot(new Slot(inventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
			}
		}

		for (int j = 0; j < 9; j++) {
			this.addSlot(new Slot(inventory, j, 8 + j * 18, 142));
		}
	}

	public abstract void createResult();

	@Override
	public void slotsChanged(Container container) {
		super.slotsChanged(container);
		if (container == this.inputSlots) {
			this.createResult();
		}
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.access.execute((level, blockPos) -> this.clearContainer(player, this.inputSlots));
	}

	@Override
	public boolean stillValid(Player player) {
		return this.access
			.evaluate(
				(level, blockPos) -> !this.isValidBlock(level.getBlockState(blockPos))
						? false
						: player.distanceToSqr((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5) <= 64.0,
				true
			);
	}

	protected boolean shouldQuickMoveToAdditionalSlot(ItemStack itemStack) {
		return false;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if (i == 2) {
				if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemStack2, itemStack);
			} else if (i != 0 && i != 1) {
				if (i >= 3 && i < 39) {
					int j = this.shouldQuickMoveToAdditionalSlot(itemStack) ? 1 : 0;
					if (!this.moveItemStackTo(itemStack2, j, 2, false)) {
						return ItemStack.EMPTY;
					}
				}
			} else if (!this.moveItemStackTo(itemStack2, 3, 39, false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemStack2.getCount() == itemStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemStack2);
		}

		return itemStack;
	}
}
