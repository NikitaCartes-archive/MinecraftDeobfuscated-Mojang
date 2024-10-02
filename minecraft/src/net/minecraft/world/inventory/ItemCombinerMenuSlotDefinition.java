package net.minecraft.world.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

public class ItemCombinerMenuSlotDefinition {
	private final List<ItemCombinerMenuSlotDefinition.SlotDefinition> slots;
	private final ItemCombinerMenuSlotDefinition.SlotDefinition resultSlot;

	ItemCombinerMenuSlotDefinition(List<ItemCombinerMenuSlotDefinition.SlotDefinition> list, ItemCombinerMenuSlotDefinition.SlotDefinition slotDefinition) {
		if (!list.isEmpty() && !slotDefinition.equals(ItemCombinerMenuSlotDefinition.SlotDefinition.EMPTY)) {
			this.slots = list;
			this.resultSlot = slotDefinition;
		} else {
			throw new IllegalArgumentException("Need to define both inputSlots and resultSlot");
		}
	}

	public static ItemCombinerMenuSlotDefinition.Builder create() {
		return new ItemCombinerMenuSlotDefinition.Builder();
	}

	public ItemCombinerMenuSlotDefinition.SlotDefinition getSlot(int i) {
		return (ItemCombinerMenuSlotDefinition.SlotDefinition)this.slots.get(i);
	}

	public ItemCombinerMenuSlotDefinition.SlotDefinition getResultSlot() {
		return this.resultSlot;
	}

	public List<ItemCombinerMenuSlotDefinition.SlotDefinition> getSlots() {
		return this.slots;
	}

	public int getNumOfInputSlots() {
		return this.slots.size();
	}

	public int getResultSlotIndex() {
		return this.getNumOfInputSlots();
	}

	public static class Builder {
		private final List<ItemCombinerMenuSlotDefinition.SlotDefinition> inputSlots = new ArrayList();
		private ItemCombinerMenuSlotDefinition.SlotDefinition resultSlot = ItemCombinerMenuSlotDefinition.SlotDefinition.EMPTY;

		public ItemCombinerMenuSlotDefinition.Builder withSlot(int i, int j, int k, Predicate<ItemStack> predicate) {
			this.inputSlots.add(new ItemCombinerMenuSlotDefinition.SlotDefinition(i, j, k, predicate));
			return this;
		}

		public ItemCombinerMenuSlotDefinition.Builder withResultSlot(int i, int j, int k) {
			this.resultSlot = new ItemCombinerMenuSlotDefinition.SlotDefinition(i, j, k, itemStack -> false);
			return this;
		}

		public ItemCombinerMenuSlotDefinition build() {
			int i = this.inputSlots.size();

			for (int j = 0; j < i; j++) {
				ItemCombinerMenuSlotDefinition.SlotDefinition slotDefinition = (ItemCombinerMenuSlotDefinition.SlotDefinition)this.inputSlots.get(j);
				if (slotDefinition.slotIndex != j) {
					throw new IllegalArgumentException("Expected input slots to have continous indexes");
				}
			}

			if (this.resultSlot.slotIndex != i) {
				throw new IllegalArgumentException("Expected result slot index to follow last input slot");
			} else {
				return new ItemCombinerMenuSlotDefinition(this.inputSlots, this.resultSlot);
			}
		}
	}

	public static record SlotDefinition(int slotIndex, int x, int y, Predicate<ItemStack> mayPlace) {
		static final ItemCombinerMenuSlotDefinition.SlotDefinition EMPTY = new ItemCombinerMenuSlotDefinition.SlotDefinition(0, 0, 0, itemStack -> true);
	}
}
