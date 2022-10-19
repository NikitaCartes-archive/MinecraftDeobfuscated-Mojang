package net.minecraft.world.item;

import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Objects;

public final class ItemStackLinkedSet extends ObjectLinkedOpenCustomHashSet<ItemStack> {
	private static final Strategy<? super ItemStack> STRATEGY = new Strategy<ItemStack>() {
		public int hashCode(ItemStack itemStack) {
			return itemStack != null ? Objects.hash(new Object[]{itemStack.getItem(), itemStack.getTag()}) : 0;
		}

		public boolean equals(ItemStack itemStack, ItemStack itemStack2) {
			return itemStack == itemStack2 || itemStack != null && itemStack2 != null && ItemStack.matches(itemStack, itemStack2);
		}
	};

	public ItemStackLinkedSet() {
		super(STRATEGY);
	}
}
