package net.minecraft.world.ticks;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ContainerSingleItem extends Container {
	ItemStack getTheItem();

	default ItemStack splitTheItem(int i) {
		return this.getTheItem().split(i);
	}

	void setTheItem(ItemStack itemStack);

	default ItemStack removeTheItem() {
		return this.splitTheItem(this.getMaxStackSize());
	}

	@Override
	default int getContainerSize() {
		return 1;
	}

	@Override
	default boolean isEmpty() {
		return this.getTheItem().isEmpty();
	}

	@Override
	default void clearContent() {
		this.removeTheItem();
	}

	@Override
	default ItemStack removeItemNoUpdate(int i) {
		return this.removeItem(i, this.getMaxStackSize());
	}

	@Override
	default ItemStack getItem(int i) {
		return i == 0 ? this.getTheItem() : ItemStack.EMPTY;
	}

	@Override
	default ItemStack removeItem(int i, int j) {
		return i != 0 ? ItemStack.EMPTY : this.splitTheItem(j);
	}

	@Override
	default void setItem(int i, ItemStack itemStack) {
		if (i == 0) {
			this.setTheItem(itemStack);
		}
	}

	public interface BlockContainerSingleItem extends ContainerSingleItem {
		BlockEntity getContainerBlockEntity();

		@Override
		default boolean stillValid(Player player) {
			return Container.stillValidBlockEntity(this.getContainerBlockEntity(), player);
		}
	}
}
