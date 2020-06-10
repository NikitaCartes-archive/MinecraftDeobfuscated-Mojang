package net.minecraft.world.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class AbstractContainerMenu {
	private final NonNullList<ItemStack> lastSlots = NonNullList.create();
	public final List<Slot> slots = Lists.<Slot>newArrayList();
	private final List<DataSlot> dataSlots = Lists.<DataSlot>newArrayList();
	@Nullable
	private final MenuType<?> menuType;
	public final int containerId;
	@Environment(EnvType.CLIENT)
	private short changeUid;
	private int quickcraftType = -1;
	private int quickcraftStatus;
	private final Set<Slot> quickcraftSlots = Sets.<Slot>newHashSet();
	private final List<ContainerListener> containerListeners = Lists.<ContainerListener>newArrayList();
	private final Set<Player> unSynchedPlayers = Sets.<Player>newHashSet();

	protected AbstractContainerMenu(@Nullable MenuType<?> menuType, int i) {
		this.menuType = menuType;
		this.containerId = i;
	}

	protected static boolean stillValid(ContainerLevelAccess containerLevelAccess, Player player, Block block) {
		return containerLevelAccess.evaluate(
			(level, blockPos) -> !level.getBlockState(blockPos).is(block)
					? false
					: player.distanceToSqr((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5) <= 64.0,
			true
		);
	}

	public MenuType<?> getType() {
		if (this.menuType == null) {
			throw new UnsupportedOperationException("Unable to construct this menu by type");
		} else {
			return this.menuType;
		}
	}

	protected static void checkContainerSize(Container container, int i) {
		int j = container.getContainerSize();
		if (j < i) {
			throw new IllegalArgumentException("Container size " + j + " is smaller than expected " + i);
		}
	}

	protected static void checkContainerDataCount(ContainerData containerData, int i) {
		int j = containerData.getCount();
		if (j < i) {
			throw new IllegalArgumentException("Container data count " + j + " is smaller than expected " + i);
		}
	}

	protected Slot addSlot(Slot slot) {
		slot.index = this.slots.size();
		this.slots.add(slot);
		this.lastSlots.add(ItemStack.EMPTY);
		return slot;
	}

	protected DataSlot addDataSlot(DataSlot dataSlot) {
		this.dataSlots.add(dataSlot);
		return dataSlot;
	}

	protected void addDataSlots(ContainerData containerData) {
		for (int i = 0; i < containerData.getCount(); i++) {
			this.addDataSlot(DataSlot.forContainer(containerData, i));
		}
	}

	public void addSlotListener(ContainerListener containerListener) {
		if (!this.containerListeners.contains(containerListener)) {
			this.containerListeners.add(containerListener);
			containerListener.refreshContainer(this, this.getItems());
			this.broadcastChanges();
		}
	}

	@Environment(EnvType.CLIENT)
	public void removeSlotListener(ContainerListener containerListener) {
		this.containerListeners.remove(containerListener);
	}

	public NonNullList<ItemStack> getItems() {
		NonNullList<ItemStack> nonNullList = NonNullList.create();

		for (int i = 0; i < this.slots.size(); i++) {
			nonNullList.add(((Slot)this.slots.get(i)).getItem());
		}

		return nonNullList;
	}

	public void broadcastChanges() {
		for (int i = 0; i < this.slots.size(); i++) {
			ItemStack itemStack = ((Slot)this.slots.get(i)).getItem();
			ItemStack itemStack2 = this.lastSlots.get(i);
			if (!ItemStack.matches(itemStack2, itemStack)) {
				ItemStack itemStack3 = itemStack.copy();
				this.lastSlots.set(i, itemStack3);

				for (ContainerListener containerListener : this.containerListeners) {
					containerListener.slotChanged(this, i, itemStack3);
				}
			}
		}

		for (int ix = 0; ix < this.dataSlots.size(); ix++) {
			DataSlot dataSlot = (DataSlot)this.dataSlots.get(ix);
			if (dataSlot.checkAndClearUpdateFlag()) {
				for (ContainerListener containerListener2 : this.containerListeners) {
					containerListener2.setContainerData(this, ix, dataSlot.get());
				}
			}
		}
	}

	public boolean clickMenuButton(Player player, int i) {
		return false;
	}

	public Slot getSlot(int i) {
		return (Slot)this.slots.get(i);
	}

	public ItemStack quickMoveStack(Player player, int i) {
		Slot slot = (Slot)this.slots.get(i);
		return slot != null ? slot.getItem() : ItemStack.EMPTY;
	}

	public ItemStack clicked(int i, int j, ClickType clickType, Player player) {
		ItemStack itemStack = ItemStack.EMPTY;
		Inventory inventory = player.inventory;
		if (clickType == ClickType.QUICK_CRAFT) {
			int k = this.quickcraftStatus;
			this.quickcraftStatus = getQuickcraftHeader(j);
			if ((k != 1 || this.quickcraftStatus != 2) && k != this.quickcraftStatus) {
				this.resetQuickCraft();
			} else if (inventory.getCarried().isEmpty()) {
				this.resetQuickCraft();
			} else if (this.quickcraftStatus == 0) {
				this.quickcraftType = getQuickcraftType(j);
				if (isValidQuickcraftType(this.quickcraftType, player)) {
					this.quickcraftStatus = 1;
					this.quickcraftSlots.clear();
				} else {
					this.resetQuickCraft();
				}
			} else if (this.quickcraftStatus == 1) {
				Slot slot = (Slot)this.slots.get(i);
				ItemStack itemStack2 = inventory.getCarried();
				if (slot != null
					&& canItemQuickReplace(slot, itemStack2, true)
					&& slot.mayPlace(itemStack2)
					&& (this.quickcraftType == 2 || itemStack2.getCount() > this.quickcraftSlots.size())
					&& this.canDragTo(slot)) {
					this.quickcraftSlots.add(slot);
				}
			} else if (this.quickcraftStatus == 2) {
				if (!this.quickcraftSlots.isEmpty()) {
					ItemStack itemStack3 = inventory.getCarried().copy();
					int l = inventory.getCarried().getCount();

					for (Slot slot2 : this.quickcraftSlots) {
						ItemStack itemStack4 = inventory.getCarried();
						if (slot2 != null
							&& canItemQuickReplace(slot2, itemStack4, true)
							&& slot2.mayPlace(itemStack4)
							&& (this.quickcraftType == 2 || itemStack4.getCount() >= this.quickcraftSlots.size())
							&& this.canDragTo(slot2)) {
							ItemStack itemStack5 = itemStack3.copy();
							int m = slot2.hasItem() ? slot2.getItem().getCount() : 0;
							getQuickCraftSlotCount(this.quickcraftSlots, this.quickcraftType, itemStack5, m);
							int n = Math.min(itemStack5.getMaxStackSize(), slot2.getMaxStackSize(itemStack5));
							if (itemStack5.getCount() > n) {
								itemStack5.setCount(n);
							}

							l -= itemStack5.getCount() - m;
							slot2.set(itemStack5);
						}
					}

					itemStack3.setCount(l);
					inventory.setCarried(itemStack3);
				}

				this.resetQuickCraft();
			} else {
				this.resetQuickCraft();
			}
		} else if (this.quickcraftStatus != 0) {
			this.resetQuickCraft();
		} else if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (j == 0 || j == 1)) {
			if (i == -999) {
				if (!inventory.getCarried().isEmpty()) {
					if (j == 0) {
						player.drop(inventory.getCarried(), true);
						inventory.setCarried(ItemStack.EMPTY);
					}

					if (j == 1) {
						player.drop(inventory.getCarried().split(1), true);
					}
				}
			} else if (clickType == ClickType.QUICK_MOVE) {
				if (i < 0) {
					return ItemStack.EMPTY;
				}

				Slot slot3 = (Slot)this.slots.get(i);
				if (slot3 == null || !slot3.mayPickup(player)) {
					return ItemStack.EMPTY;
				}

				for (ItemStack itemStack3 = this.quickMoveStack(player, i);
					!itemStack3.isEmpty() && ItemStack.isSame(slot3.getItem(), itemStack3);
					itemStack3 = this.quickMoveStack(player, i)
				) {
					itemStack = itemStack3.copy();
				}
			} else {
				if (i < 0) {
					return ItemStack.EMPTY;
				}

				Slot slot3 = (Slot)this.slots.get(i);
				if (slot3 != null) {
					ItemStack itemStack3 = slot3.getItem();
					ItemStack itemStack2 = inventory.getCarried();
					if (!itemStack3.isEmpty()) {
						itemStack = itemStack3.copy();
					}

					if (itemStack3.isEmpty()) {
						if (!itemStack2.isEmpty() && slot3.mayPlace(itemStack2)) {
							int o = j == 0 ? itemStack2.getCount() : 1;
							if (o > slot3.getMaxStackSize(itemStack2)) {
								o = slot3.getMaxStackSize(itemStack2);
							}

							slot3.set(itemStack2.split(o));
						}
					} else if (slot3.mayPickup(player)) {
						if (itemStack2.isEmpty()) {
							if (itemStack3.isEmpty()) {
								slot3.set(ItemStack.EMPTY);
								inventory.setCarried(ItemStack.EMPTY);
							} else {
								int o = j == 0 ? itemStack3.getCount() : (itemStack3.getCount() + 1) / 2;
								inventory.setCarried(slot3.remove(o));
								if (itemStack3.isEmpty()) {
									slot3.set(ItemStack.EMPTY);
								}

								slot3.onTake(player, inventory.getCarried());
							}
						} else if (slot3.mayPlace(itemStack2)) {
							if (consideredTheSameItem(itemStack3, itemStack2)) {
								int o = j == 0 ? itemStack2.getCount() : 1;
								if (o > slot3.getMaxStackSize(itemStack2) - itemStack3.getCount()) {
									o = slot3.getMaxStackSize(itemStack2) - itemStack3.getCount();
								}

								if (o > itemStack2.getMaxStackSize() - itemStack3.getCount()) {
									o = itemStack2.getMaxStackSize() - itemStack3.getCount();
								}

								itemStack2.shrink(o);
								itemStack3.grow(o);
							} else if (itemStack2.getCount() <= slot3.getMaxStackSize(itemStack2)) {
								slot3.set(itemStack2);
								inventory.setCarried(itemStack3);
							}
						} else if (itemStack2.getMaxStackSize() > 1 && consideredTheSameItem(itemStack3, itemStack2) && !itemStack3.isEmpty()) {
							int ox = itemStack3.getCount();
							if (ox + itemStack2.getCount() <= itemStack2.getMaxStackSize()) {
								itemStack2.grow(ox);
								itemStack3 = slot3.remove(ox);
								if (itemStack3.isEmpty()) {
									slot3.set(ItemStack.EMPTY);
								}

								slot3.onTake(player, inventory.getCarried());
							}
						}
					}

					slot3.setChanged();
				}
			}
		} else if (clickType == ClickType.SWAP) {
			Slot slot3 = (Slot)this.slots.get(i);
			ItemStack itemStack3x = inventory.getItem(j);
			ItemStack itemStack2x = slot3.getItem();
			if (!itemStack3x.isEmpty() || !itemStack2x.isEmpty()) {
				if (itemStack3x.isEmpty()) {
					if (slot3.mayPickup(player)) {
						inventory.setItem(j, itemStack2x);
						slot3.onSwapCraft(itemStack2x.getCount());
						slot3.set(ItemStack.EMPTY);
						slot3.onTake(player, itemStack2x);
					}
				} else if (itemStack2x.isEmpty()) {
					if (slot3.mayPlace(itemStack3x)) {
						int ox = slot3.getMaxStackSize(itemStack3x);
						if (itemStack3x.getCount() > ox) {
							slot3.set(itemStack3x.split(ox));
						} else {
							slot3.set(itemStack3x);
							inventory.setItem(j, ItemStack.EMPTY);
						}
					}
				} else if (slot3.mayPickup(player) && slot3.mayPlace(itemStack3x)) {
					int ox = slot3.getMaxStackSize(itemStack3x);
					if (itemStack3x.getCount() > ox) {
						slot3.set(itemStack3x.split(ox));
						slot3.onTake(player, itemStack2x);
						if (!inventory.add(itemStack2x)) {
							player.drop(itemStack2x, true);
						}
					} else {
						slot3.set(itemStack3x);
						inventory.setItem(j, itemStack2x);
						slot3.onTake(player, itemStack2x);
					}
				}
			}
		} else if (clickType == ClickType.CLONE && player.abilities.instabuild && inventory.getCarried().isEmpty() && i >= 0) {
			Slot slot3 = (Slot)this.slots.get(i);
			if (slot3 != null && slot3.hasItem()) {
				ItemStack itemStack3x = slot3.getItem().copy();
				itemStack3x.setCount(itemStack3x.getMaxStackSize());
				inventory.setCarried(itemStack3x);
			}
		} else if (clickType == ClickType.THROW && inventory.getCarried().isEmpty() && i >= 0) {
			Slot slot3 = (Slot)this.slots.get(i);
			if (slot3 != null && slot3.hasItem() && slot3.mayPickup(player)) {
				ItemStack itemStack3x = slot3.remove(j == 0 ? 1 : slot3.getItem().getCount());
				slot3.onTake(player, itemStack3x);
				player.drop(itemStack3x, true);
			}
		} else if (clickType == ClickType.PICKUP_ALL && i >= 0) {
			Slot slot3 = (Slot)this.slots.get(i);
			ItemStack itemStack3x = inventory.getCarried();
			if (!itemStack3x.isEmpty() && (slot3 == null || !slot3.hasItem() || !slot3.mayPickup(player))) {
				int l = j == 0 ? 0 : this.slots.size() - 1;
				int ox = j == 0 ? 1 : -1;

				for (int p = 0; p < 2; p++) {
					for (int q = l; q >= 0 && q < this.slots.size() && itemStack3x.getCount() < itemStack3x.getMaxStackSize(); q += ox) {
						Slot slot4 = (Slot)this.slots.get(q);
						if (slot4.hasItem() && canItemQuickReplace(slot4, itemStack3x, true) && slot4.mayPickup(player) && this.canTakeItemForPickAll(itemStack3x, slot4)) {
							ItemStack itemStack6 = slot4.getItem();
							if (p != 0 || itemStack6.getCount() != itemStack6.getMaxStackSize()) {
								int n = Math.min(itemStack3x.getMaxStackSize() - itemStack3x.getCount(), itemStack6.getCount());
								ItemStack itemStack7 = slot4.remove(n);
								itemStack3x.grow(n);
								if (itemStack7.isEmpty()) {
									slot4.set(ItemStack.EMPTY);
								}

								slot4.onTake(player, itemStack7);
							}
						}
					}
				}
			}

			this.broadcastChanges();
		}

		return itemStack;
	}

	public static boolean consideredTheSameItem(ItemStack itemStack, ItemStack itemStack2) {
		return itemStack.getItem() == itemStack2.getItem() && ItemStack.tagMatches(itemStack, itemStack2);
	}

	public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
		return true;
	}

	public void removed(Player player) {
		Inventory inventory = player.inventory;
		if (!inventory.getCarried().isEmpty()) {
			player.drop(inventory.getCarried(), false);
			inventory.setCarried(ItemStack.EMPTY);
		}
	}

	protected void clearContainer(Player player, Level level, Container container) {
		if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
			for (int i = 0; i < container.getContainerSize(); i++) {
				player.drop(container.removeItemNoUpdate(i), false);
			}
		} else {
			for (int i = 0; i < container.getContainerSize(); i++) {
				player.inventory.placeItemBackInInventory(level, container.removeItemNoUpdate(i));
			}
		}
	}

	public void slotsChanged(Container container) {
		this.broadcastChanges();
	}

	public void setItem(int i, ItemStack itemStack) {
		this.getSlot(i).set(itemStack);
	}

	@Environment(EnvType.CLIENT)
	public void setAll(List<ItemStack> list) {
		for (int i = 0; i < list.size(); i++) {
			this.getSlot(i).set((ItemStack)list.get(i));
		}
	}

	public void setData(int i, int j) {
		((DataSlot)this.dataSlots.get(i)).set(j);
	}

	@Environment(EnvType.CLIENT)
	public short backup(Inventory inventory) {
		this.changeUid++;
		return this.changeUid;
	}

	public boolean isSynched(Player player) {
		return !this.unSynchedPlayers.contains(player);
	}

	public void setSynched(Player player, boolean bl) {
		if (bl) {
			this.unSynchedPlayers.remove(player);
		} else {
			this.unSynchedPlayers.add(player);
		}
	}

	public abstract boolean stillValid(Player player);

	protected boolean moveItemStackTo(ItemStack itemStack, int i, int j, boolean bl) {
		boolean bl2 = false;
		int k = i;
		if (bl) {
			k = j - 1;
		}

		if (itemStack.isStackable()) {
			while (!itemStack.isEmpty() && (bl ? k >= i : k < j)) {
				Slot slot = (Slot)this.slots.get(k);
				ItemStack itemStack2 = slot.getItem();
				if (!itemStack2.isEmpty() && consideredTheSameItem(itemStack, itemStack2)) {
					int l = itemStack2.getCount() + itemStack.getCount();
					if (l <= itemStack.getMaxStackSize()) {
						itemStack.setCount(0);
						itemStack2.setCount(l);
						slot.setChanged();
						bl2 = true;
					} else if (itemStack2.getCount() < itemStack.getMaxStackSize()) {
						itemStack.shrink(itemStack.getMaxStackSize() - itemStack2.getCount());
						itemStack2.setCount(itemStack.getMaxStackSize());
						slot.setChanged();
						bl2 = true;
					}
				}

				if (bl) {
					k--;
				} else {
					k++;
				}
			}
		}

		if (!itemStack.isEmpty()) {
			if (bl) {
				k = j - 1;
			} else {
				k = i;
			}

			while (bl ? k >= i : k < j) {
				Slot slotx = (Slot)this.slots.get(k);
				ItemStack itemStack2x = slotx.getItem();
				if (itemStack2x.isEmpty() && slotx.mayPlace(itemStack)) {
					if (itemStack.getCount() > slotx.getMaxStackSize()) {
						slotx.set(itemStack.split(slotx.getMaxStackSize()));
					} else {
						slotx.set(itemStack.split(itemStack.getCount()));
					}

					slotx.setChanged();
					bl2 = true;
					break;
				}

				if (bl) {
					k--;
				} else {
					k++;
				}
			}
		}

		return bl2;
	}

	public static int getQuickcraftType(int i) {
		return i >> 2 & 3;
	}

	public static int getQuickcraftHeader(int i) {
		return i & 3;
	}

	@Environment(EnvType.CLIENT)
	public static int getQuickcraftMask(int i, int j) {
		return i & 3 | (j & 3) << 2;
	}

	public static boolean isValidQuickcraftType(int i, Player player) {
		if (i == 0) {
			return true;
		} else {
			return i == 1 ? true : i == 2 && player.abilities.instabuild;
		}
	}

	protected void resetQuickCraft() {
		this.quickcraftStatus = 0;
		this.quickcraftSlots.clear();
	}

	public static boolean canItemQuickReplace(@Nullable Slot slot, ItemStack itemStack, boolean bl) {
		boolean bl2 = slot == null || !slot.hasItem();
		return !bl2 && itemStack.sameItem(slot.getItem()) && ItemStack.tagMatches(slot.getItem(), itemStack)
			? slot.getItem().getCount() + (bl ? 0 : itemStack.getCount()) <= itemStack.getMaxStackSize()
			: bl2;
	}

	public static void getQuickCraftSlotCount(Set<Slot> set, int i, ItemStack itemStack, int j) {
		switch (i) {
			case 0:
				itemStack.setCount(Mth.floor((float)itemStack.getCount() / (float)set.size()));
				break;
			case 1:
				itemStack.setCount(1);
				break;
			case 2:
				itemStack.setCount(itemStack.getItem().getMaxStackSize());
		}

		itemStack.grow(j);
	}

	public boolean canDragTo(Slot slot) {
		return true;
	}

	public static int getRedstoneSignalFromBlockEntity(@Nullable BlockEntity blockEntity) {
		return blockEntity instanceof Container ? getRedstoneSignalFromContainer((Container)blockEntity) : 0;
	}

	public static int getRedstoneSignalFromContainer(@Nullable Container container) {
		if (container == null) {
			return 0;
		} else {
			int i = 0;
			float f = 0.0F;

			for (int j = 0; j < container.getContainerSize(); j++) {
				ItemStack itemStack = container.getItem(j);
				if (!itemStack.isEmpty()) {
					f += (float)itemStack.getCount() / (float)Math.min(container.getMaxStackSize(), itemStack.getMaxStackSize());
					i++;
				}
			}

			f /= (float)container.getContainerSize();
			return Mth.floor(f * 14.0F) + (i > 0 ? 1 : 0);
		}
	}
}
