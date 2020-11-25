package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Slot {
	private final int slot;
	public final Container container;
	public int index;
	public final int x;
	public final int y;

	public Slot(Container container, int i, int j, int k) {
		this.container = container;
		this.slot = i;
		this.x = j;
		this.y = k;
	}

	public void onQuickCraft(ItemStack itemStack, ItemStack itemStack2) {
		int i = itemStack2.getCount() - itemStack.getCount();
		if (i > 0) {
			this.onQuickCraft(itemStack2, i);
		}
	}

	protected void onQuickCraft(ItemStack itemStack, int i) {
	}

	protected void onSwapCraft(int i) {
	}

	protected void checkTakeAchievements(ItemStack itemStack) {
	}

	public ItemStack onTake(Player player, ItemStack itemStack) {
		this.setChanged();
		return itemStack;
	}

	public boolean mayPlace(ItemStack itemStack) {
		return true;
	}

	public ItemStack getItem() {
		return this.container.getItem(this.slot);
	}

	public boolean hasItem() {
		return !this.getItem().isEmpty();
	}

	public void set(ItemStack itemStack) {
		this.container.setItem(this.slot, itemStack);
		this.setChanged();
	}

	public void setChanged() {
		this.container.setChanged();
	}

	public int getMaxStackSize() {
		return this.container.getMaxStackSize();
	}

	public int getMaxStackSize(ItemStack itemStack) {
		return Math.min(this.getMaxStackSize(), itemStack.getMaxStackSize());
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
		return null;
	}

	public ItemStack remove(int i) {
		return this.container.removeItem(this.slot, i);
	}

	public boolean mayPickup(Player player) {
		return true;
	}

	@Environment(EnvType.CLIENT)
	public boolean isActive() {
		return true;
	}

	public ItemStack safeTake(int i, int j, Player player) {
		if (!this.mayPickup(player)) {
			return ItemStack.EMPTY;
		} else if (!this.allowModification(player) && j < this.getItem().getCount()) {
			return ItemStack.EMPTY;
		} else {
			if (!this.allowModification(player)) {
				i = this.getItem().getCount();
			}

			i = Math.min(i, j);
			ItemStack itemStack = this.remove(i);
			if (this.getItem().isEmpty()) {
				this.set(ItemStack.EMPTY);
			}

			this.onTake(player, itemStack);
			return itemStack;
		}
	}

	public ItemStack safeInsert(ItemStack itemStack) {
		return this.safeInsert(itemStack, itemStack.getCount());
	}

	public ItemStack safeInsert(ItemStack itemStack, int i) {
		if (!itemStack.isEmpty() && this.mayPlace(itemStack)) {
			ItemStack itemStack2 = this.getItem();
			int j = Math.min(Math.min(i, itemStack.getCount()), this.getMaxStackSize(itemStack) - itemStack2.getCount());
			if (itemStack2.isEmpty()) {
				this.set(itemStack.split(j));
			} else if (ItemStack.isSameItemSameTags(itemStack2, itemStack)) {
				itemStack.shrink(j);
				itemStack2.grow(j);
				this.set(itemStack2);
			}

			return itemStack;
		} else {
			return itemStack;
		}
	}

	public boolean allowModification(Player player) {
		return this.mayPickup(player) && this.mayPlace(this.getItem());
	}
}
