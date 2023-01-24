/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;

public class ItemCombinerMenuSlotDefinition {
    private final List<SlotDefinition> slots;
    private final SlotDefinition resultSlot;

    ItemCombinerMenuSlotDefinition(List<SlotDefinition> list, SlotDefinition slotDefinition) {
        if (list.isEmpty() || slotDefinition.equals(SlotDefinition.EMPTY)) {
            throw new IllegalArgumentException("Need to define both inputSlots and resultSlot");
        }
        this.slots = list;
        this.resultSlot = slotDefinition;
    }

    public static Builder create() {
        return new Builder();
    }

    public boolean hasSlot(int i) {
        return this.slots.size() >= i;
    }

    public SlotDefinition getSlot(int i) {
        return this.slots.get(i);
    }

    public SlotDefinition getResultSlot() {
        return this.resultSlot;
    }

    public List<SlotDefinition> getSlots() {
        return this.slots;
    }

    public int getNumOfInputSlots() {
        return this.slots.size();
    }

    public int getResultSlotIndex() {
        return this.getNumOfInputSlots();
    }

    public List<Integer> getInputSlotIndexes() {
        return this.slots.stream().map(SlotDefinition::slotIndex).collect(Collectors.toList());
    }

    public record SlotDefinition(int slotIndex, int x, int y, Predicate<ItemStack> mayPlace) {
        static final SlotDefinition EMPTY = new SlotDefinition(0, 0, 0, itemStack -> true);
    }

    public static class Builder {
        private final List<SlotDefinition> slots = new ArrayList<SlotDefinition>();
        private SlotDefinition resultSlot = SlotDefinition.EMPTY;

        public Builder withSlot(int i, int j, int k, Predicate<ItemStack> predicate) {
            this.slots.add(new SlotDefinition(i, j, k, predicate));
            return this;
        }

        public Builder withResultSlot(int i, int j, int k) {
            this.resultSlot = new SlotDefinition(i, j, k, itemStack -> false);
            return this;
        }

        public ItemCombinerMenuSlotDefinition build() {
            return new ItemCombinerMenuSlotDefinition(this.slots, this.resultSlot);
        }
    }
}

