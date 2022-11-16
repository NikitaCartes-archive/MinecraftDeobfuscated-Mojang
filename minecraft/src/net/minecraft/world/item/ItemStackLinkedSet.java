package net.minecraft.world.item;

import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;

public class ItemStackLinkedSet {
	private static final Strategy<? super ItemStack> TYPE_AND_TAG = new Strategy<ItemStack>() {
		public int hashCode(@Nullable ItemStack itemStack) {
			return ItemStackLinkedSet.hashStackAndTag(itemStack);
		}

		public boolean equals(@Nullable ItemStack itemStack, @Nullable ItemStack itemStack2) {
			return itemStack == itemStack2
				|| itemStack != null && itemStack2 != null && itemStack.isEmpty() == itemStack2.isEmpty() && ItemStack.isSameItemSameTags(itemStack, itemStack2);
		}
	};

	static int hashStackAndTag(@Nullable ItemStack itemStack) {
		if (itemStack != null) {
			CompoundTag compoundTag = itemStack.getTag();
			int i = 31 + itemStack.getItem().hashCode();
			return 31 * i + (compoundTag == null ? 0 : compoundTag.hashCode());
		} else {
			return 0;
		}
	}

	public static Set<ItemStack> createTypeAndTagSet() {
		return new ObjectLinkedOpenCustomHashSet<>(TYPE_AND_TAG);
	}
}
