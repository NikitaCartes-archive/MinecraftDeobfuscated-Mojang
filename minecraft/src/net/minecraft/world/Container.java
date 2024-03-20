package net.minecraft.world;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface Container extends Clearable {
	float DEFAULT_DISTANCE_BUFFER = 4.0F;

	int getContainerSize();

	boolean isEmpty();

	ItemStack getItem(int i);

	ItemStack removeItem(int i, int j);

	ItemStack removeItemNoUpdate(int i);

	void setItem(int i, ItemStack itemStack);

	default int getMaxStackSize() {
		return 99;
	}

	default int getMaxStackSize(ItemStack itemStack) {
		return Math.min(this.getMaxStackSize(), itemStack.getMaxStackSize());
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

	default boolean canTakeItem(Container container, int i, ItemStack itemStack) {
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
		return this.hasAnyMatching(itemStack -> !itemStack.isEmpty() && set.contains(itemStack.getItem()));
	}

	default boolean hasAnyMatching(Predicate<ItemStack> predicate) {
		for (int i = 0; i < this.getContainerSize(); i++) {
			ItemStack itemStack = this.getItem(i);
			if (predicate.test(itemStack)) {
				return true;
			}
		}

		return false;
	}

	static boolean stillValidBlockEntity(BlockEntity blockEntity, Player player) {
		return stillValidBlockEntity(blockEntity, player, 4.0F);
	}

	static boolean stillValidBlockEntity(BlockEntity blockEntity, Player player, float f) {
		Level level = blockEntity.getLevel();
		BlockPos blockPos = blockEntity.getBlockPos();
		if (level == null) {
			return false;
		} else {
			return level.getBlockEntity(blockPos) != blockEntity ? false : player.canInteractWithBlock(blockPos, (double)f);
		}
	}
}
