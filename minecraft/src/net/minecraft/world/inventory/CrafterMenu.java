package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CrafterBlock;

public class CrafterMenu extends AbstractContainerMenu implements ContainerListener {
	protected static final int SLOT_COUNT = 9;
	private static final int INV_SLOT_START = 9;
	private static final int INV_SLOT_END = 36;
	private static final int USE_ROW_SLOT_START = 36;
	private static final int USE_ROW_SLOT_END = 45;
	private final ResultContainer resultContainer = new ResultContainer();
	private final ContainerData containerData;
	private final Player player;
	private final CraftingContainer container;

	public CrafterMenu(int i, Inventory inventory) {
		super(MenuType.CRAFTER_3x3, i);
		this.player = inventory.player;
		this.containerData = new SimpleContainerData(10);
		this.container = new TransientCraftingContainer(this, 3, 3);
		this.addSlots(inventory);
	}

	public CrafterMenu(int i, Inventory inventory, CraftingContainer craftingContainer, ContainerData containerData) {
		super(MenuType.CRAFTER_3x3, i);
		this.player = inventory.player;
		this.containerData = containerData;
		this.container = craftingContainer;
		checkContainerSize(craftingContainer, 9);
		craftingContainer.startOpen(inventory.player);
		this.addSlots(inventory);
		this.addSlotListener(this);
	}

	private void addSlots(Inventory inventory) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int k = j + i * 3;
				this.addSlot(new CrafterSlot(this.container, k, 26 + j * 18, 17 + i * 18, this));
			}
		}

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
		}

		this.addSlot(new NonInteractiveResultSlot(this.resultContainer, 0, 134, 35));
		this.addDataSlots(this.containerData);
		this.refreshRecipeResult();
	}

	public void setSlotState(int i, boolean bl) {
		CrafterSlot crafterSlot = (CrafterSlot)this.getSlot(i);
		this.containerData.set(crafterSlot.index, bl ? 0 : 1);
		this.broadcastChanges();
	}

	public boolean isSlotDisabled(int i) {
		return i > -1 && i < 9 ? this.containerData.get(i) == 1 : false;
	}

	public boolean isPowered() {
		return this.containerData.get(9) == 1;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if (i < 9) {
				if (!this.moveItemStackTo(itemStack2, 9, 45, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 0, 9, false)) {
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

	@Override
	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}

	private void refreshRecipeResult() {
		if (this.player instanceof ServerPlayer serverPlayer) {
			Level level = serverPlayer.level();
			ItemStack itemStack = (ItemStack)CrafterBlock.getPotentialResults(level, this.container)
				.map(craftingRecipe -> craftingRecipe.assemble(this.container, level.registryAccess()))
				.orElse(ItemStack.EMPTY);
			this.resultContainer.setItem(0, itemStack);
		}
	}

	public Container getContainer() {
		return this.container;
	}

	@Override
	public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
		this.refreshRecipeResult();
	}

	@Override
	public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {
	}
}
