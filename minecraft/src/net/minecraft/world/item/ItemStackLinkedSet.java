package net.minecraft.world.item;

import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Set;
import javax.annotation.Nullable;

public class ItemStackLinkedSet {
	private static final Strategy<? super ItemStack> TYPE_AND_TAG = new Strategy<ItemStack>() {
		public int hashCode(@Nullable ItemStack itemStack) {
			return ItemStack.hashItemAndComponents(itemStack);
		}

		public boolean equals(@Nullable ItemStack itemStack, @Nullable ItemStack itemStack2) {
			return itemStack == itemStack2
				|| itemStack != null && itemStack2 != null && itemStack.isEmpty() == itemStack2.isEmpty() && ItemStack.isSameItemSameTags(itemStack, itemStack2);
		}
	};

	public static Set<ItemStack> createTypeAndComponentsSet() {
		return new ObjectLinkedOpenCustomHashSet<>(TYPE_AND_TAG);
	}
}
