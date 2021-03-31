package net.minecraft.world;

import java.util.Set;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface Container extends Clearable {
	int LARGE_MAX_STACK_SIZE = 64;

	int getContainerSize();

	boolean isEmpty();

	ItemStack getItem(int i);

	ItemStack removeItem(int i, int j);

	ItemStack removeItemNoUpdate(int i);

	void setItem(int i, ItemStack itemStack);

	default int getMaxStackSize() {
		return 64;
	}

	void setChanged();

	boolean stillValid(Player player);

	default void startOpen(Player player) {
	}

	default void stopOpen(Player player) {
	}

	default boolean canPlaceItem(int i, ItemStack itemStack) {
		return true;
	}

	default int countItem(Item item) {
		int i = 0;

		for (int j = 0; j < this.getContainerSize(); j++) {
			ItemStack itemStack = this.getItem(j);
			if (itemStack.getItem().equals(item)) {
				i += itemStack.getCount();
			}
		}

		return i;
	}

	default boolean hasAnyOf(Set<Item> set) {
		for (int i = 0; i < this.getContainerSize(); i++) {
			ItemStack itemStack = this.getItem(i);
			if (set.contains(itemStack.getItem()) && itemStack.getCount() > 0) {
				return true;
			}
		}

		return false;
	}
}
