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
	public int x;
	public int y;

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
		return this.getMaxStackSize();
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
}
