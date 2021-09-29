package net.minecraft.world;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CompoundContainer implements Container {
	private final Container container1;
	private final Container container2;

	public CompoundContainer(Container container, Container container2) {
		this.container1 = container;
		this.container2 = container2;
	}

	@Override
	public int getContainerSize() {
		return this.container1.getContainerSize() + this.container2.getContainerSize();
	}

	@Override
	public boolean isEmpty() {
		return this.container1.isEmpty() && this.container2.isEmpty();
	}

	public boolean contains(Container container) {
		return this.container1 == container || this.container2 == container;
	}

	@Override
	public ItemStack getItem(int i) {
		return i >= this.container1.getContainerSize() ? this.container2.getItem(i - this.container1.getContainerSize()) : this.container1.getItem(i);
	}

	@Override
	public ItemStack removeItem(int i, int j) {
		return i >= this.container1.getContainerSize() ? this.container2.removeItem(i - this.container1.getContainerSize(), j) : this.container1.removeItem(i, j);
	}

	@Override
	public ItemStack removeItemNoUpdate(int i) {
		return i >= this.container1.getContainerSize()
			? this.container2.removeItemNoUpdate(i - this.container1.getContainerSize())
			: this.container1.removeItemNoUpdate(i);
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		if (i >= this.container1.getContainerSize()) {
			this.container2.setItem(i - this.container1.getContainerSize(), itemStack);
		} else {
			this.container1.setItem(i, itemStack);
		}
	}

	@Override
	public int getMaxStackSize() {
		return this.container1.getMaxStackSize();
	}

	@Override
	public void setChanged() {
		this.container1.setChanged();
		this.container2.setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return this.container1.stillValid(player) && this.container2.stillValid(player);
	}

	@Override
	public void startOpen(Player player) {
		this.container1.startOpen(player);
		this.container2.startOpen(player);
	}

	@Override
	public void stopOpen(Player player) {
		this.container1.stopOpen(player);
		this.container2.stopOpen(player);
	}

	@Override
	public boolean canPlaceItem(int i, ItemStack itemStack) {
		return i >= this.container1.getContainerSize()
			? this.container2.canPlaceItem(i - this.container1.getContainerSize(), itemStack)
			: this.container1.canPlaceItem(i, itemStack);
	}

	@Override
	public void clearContent() {
		this.container1.clearContent();
		this.container2.clearContent();
	}
}
