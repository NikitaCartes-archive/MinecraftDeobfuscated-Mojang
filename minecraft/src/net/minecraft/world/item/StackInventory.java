package net.minecraft.world.item;

import java.util.Arrays;
import java.util.List;

public final class StackInventory {
	private int top = -1;
	private final boolean[] freeSlots;
	private final int[] slotMap;
	private final ItemStack[] items;
	private final int capacity;

	public StackInventory(int i) {
		this.capacity = i;
		this.freeSlots = new boolean[i];
		Arrays.fill(this.freeSlots, true);
		this.slotMap = new int[i];
		this.items = new ItemStack[i];
	}

	private int firstFreeSlot() {
		for (int i = 0; i < this.freeSlots.length; i++) {
			if (this.freeSlots[i]) {
				return i;
			}
		}

		return -1;
	}

	public boolean pushWithSlot(ItemStack itemStack, int i) {
		if (this.top != this.capacity - 1 && !itemStack.isEmpty()) {
			this.top++;
			this.slotMap[this.top] = i;
			this.freeSlots[i] = false;
			this.items[this.top] = itemStack;
			return true;
		} else {
			return false;
		}
	}

	public boolean push(ItemStack itemStack) {
		return this.pushWithSlot(itemStack, this.firstFreeSlot());
	}

	public ItemStack pop() {
		if (this.top == -1) {
			return ItemStack.EMPTY;
		} else {
			ItemStack itemStack = this.items[this.top];
			this.items[this.top] = ItemStack.EMPTY;
			this.freeSlots[this.slotMap[this.top]] = true;
			this.top--;
			return itemStack;
		}
	}

	public boolean canSet(int i) {
		return i >= 0 && i <= this.capacity - 1;
	}

	public boolean set(ItemStack itemStack, int i) {
		if (i >= 0 && i <= this.capacity - 1) {
			if (itemStack.isEmpty()) {
				this.remove(i);
				return true;
			} else {
				for (int j = 0; j < this.size(); j++) {
					if (this.slotMap[j] == i) {
						this.items[j] = itemStack;
						return true;
					}
				}

				return this.pushWithSlot(itemStack, i);
			}
		} else {
			return false;
		}
	}

	public ItemStack get(int i) {
		for (int j = 0; j < this.size(); j++) {
			if (this.slotMap[j] == i) {
				return this.items[j];
			}
		}

		return ItemStack.EMPTY;
	}

	public ItemStack remove(int i) {
		for (int j = 0; j < this.size(); j++) {
			if (this.slotMap[j] == i) {
				ItemStack itemStack = this.items[j];
				if (j != this.top) {
					System.arraycopy(this.slotMap, j + 1, this.slotMap, j, this.top - j);
					System.arraycopy(this.items, j + 1, this.items, j, this.top - j);
				}

				this.freeSlots[i] = true;
				this.top--;
				return itemStack;
			}
		}

		return ItemStack.EMPTY;
	}

	public int size() {
		return this.top + 1;
	}

	public boolean isFull() {
		return this.size() == this.capacity;
	}

	public boolean isEmpty() {
		return this.top == -1;
	}

	public List<ItemStack> view() {
		ItemStack[] itemStacks = new ItemStack[this.size()];
		System.arraycopy(this.items, 0, itemStacks, 0, this.size());
		return List.of(itemStacks);
	}

	public List<ItemStack> clear() {
		if (this.top == -1) {
			return List.of();
		} else {
			List<ItemStack> list = this.view();

			for (int i = 0; i < this.size(); i++) {
				this.items[i] = ItemStack.EMPTY;
				this.freeSlots[i] = true;
			}

			this.top = -1;
			return list;
		}
	}

	public StackInventory.FlattenResult flatten() {
		StackInventory.FlattenResult flattenResult = StackInventory.FlattenResult.NO_CHANGE;

		for (int i = 0; i < this.size(); i++) {
			for (int j = this.items[i].getCount() - 1; j > 0; j--) {
				flattenResult = StackInventory.FlattenResult.FULLY_FLATTENED;
				if (!this.push(this.items[i].split(1))) {
					this.items[i].grow(1);
					return StackInventory.FlattenResult.PARTIALLY_FLATTENED;
				}
			}
		}

		return flattenResult;
	}

	public static enum FlattenResult {
		PARTIALLY_FLATTENED,
		FULLY_FLATTENED,
		NO_CHANGE;
	}
}
