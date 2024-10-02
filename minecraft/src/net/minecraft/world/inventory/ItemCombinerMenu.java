package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ItemCombinerMenu extends AbstractContainerMenu {
	private static final int INVENTORY_SLOTS_PER_ROW = 9;
	private static final int INVENTORY_ROWS = 3;
	private static final int INPUT_SLOT_START = 0;
	protected final ContainerLevelAccess access;
	protected final Player player;
	protected final Container inputSlots;
	protected final ResultContainer resultSlots = new ResultContainer();
	private final int resultSlotIndex;

	protected boolean mayPickup(Player player, boolean bl) {
		return true;
	}

	protected abstract void onTake(Player player, ItemStack itemStack);

	protected abstract boolean isValidBlock(BlockState blockState);

	public ItemCombinerMenu(
		@Nullable MenuType<?> menuType,
		int i,
		Inventory inventory,
		ContainerLevelAccess containerLevelAccess,
		ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition
	) {
		super(menuType, i);
		this.access = containerLevelAccess;
		this.player = inventory.player;
		this.inputSlots = this.createContainer(itemCombinerMenuSlotDefinition.getNumOfInputSlots());
		this.resultSlotIndex = itemCombinerMenuSlotDefinition.getResultSlotIndex();
		this.createInputSlots(itemCombinerMenuSlotDefinition);
		this.createResultSlot(itemCombinerMenuSlotDefinition);
		this.addStandardInventorySlots(inventory, 8, 84);
	}

	private void createInputSlots(ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition) {
		for (final ItemCombinerMenuSlotDefinition.SlotDefinition slotDefinition : itemCombinerMenuSlotDefinition.getSlots()) {
			this.addSlot(new Slot(this.inputSlots, slotDefinition.slotIndex(), slotDefinition.x(), slotDefinition.y()) {
				@Override
				public boolean mayPlace(ItemStack itemStack) {
					return slotDefinition.mayPlace().test(itemStack);
				}
			});
		}
	}

	private void createResultSlot(ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition) {
		this.addSlot(
			new Slot(
				this.resultSlots,
				itemCombinerMenuSlotDefinition.getResultSlot().slotIndex(),
				itemCombinerMenuSlotDefinition.getResultSlot().x(),
				itemCombinerMenuSlotDefinition.getResultSlot().y()
			) {
				@Override
				public boolean mayPlace(ItemStack itemStack) {
					return false;
				}

				@Override
				public boolean mayPickup(Player player) {
					return ItemCombinerMenu.this.mayPickup(player, this.hasItem());
				}

				@Override
				public void onTake(Player player, ItemStack itemStack) {
					ItemCombinerMenu.this.onTake(player, itemStack);
				}
			}
		);
	}

	public abstract void createResult();

	private SimpleContainer createContainer(int i) {
		return new SimpleContainer(i) {
			@Override
			public void setChanged() {
				super.setChanged();
				ItemCombinerMenu.this.slotsChanged(this);
			}
		};
	}

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
			.evaluate((level, blockPos) -> !this.isValidBlock(level.getBlockState(blockPos)) ? false : player.canInteractWithBlock(blockPos, 4.0), true);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			int j = this.getInventorySlotStart();
			int k = this.getUseRowEnd();
			if (i == this.getResultSlot()) {
				if (!this.moveItemStackTo(itemStack2, j, k, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemStack2, itemStack);
			} else if (i >= 0 && i < this.getResultSlot()) {
				if (!this.moveItemStackTo(itemStack2, j, k, false)) {
					return ItemStack.EMPTY;
				}
			} else if (this.canMoveIntoInputSlots(itemStack2) && i >= this.getInventorySlotStart() && i < this.getUseRowEnd()) {
				if (!this.moveItemStackTo(itemStack2, 0, this.getResultSlot(), false)) {
					return ItemStack.EMPTY;
				}
			} else if (i >= this.getInventorySlotStart() && i < this.getInventorySlotEnd()) {
				if (!this.moveItemStackTo(itemStack2, this.getUseRowStart(), this.getUseRowEnd(), false)) {
					return ItemStack.EMPTY;
				}
			} else if (i >= this.getUseRowStart()
				&& i < this.getUseRowEnd()
				&& !this.moveItemStackTo(itemStack2, this.getInventorySlotStart(), this.getInventorySlotEnd(), false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
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

	protected boolean canMoveIntoInputSlots(ItemStack itemStack) {
		return true;
	}

	public int getResultSlot() {
		return this.resultSlotIndex;
	}

	private int getInventorySlotStart() {
		return this.getResultSlot() + 1;
	}

	private int getInventorySlotEnd() {
		return this.getInventorySlotStart() + 27;
	}

	private int getUseRowStart() {
		return this.getInventorySlotEnd();
	}

	private int getUseRowEnd() {
		return this.getUseRowStart() + 9;
	}
}
