package net.minecraft.world;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public class ContainerHelper {
	public static ItemStack removeItem(List<ItemStack> list, int i, int j) {
		return i >= 0 && i < list.size() && !((ItemStack)list.get(i)).isEmpty() && j > 0 ? ((ItemStack)list.get(i)).split(j) : ItemStack.EMPTY;
	}

	public static ItemStack takeItem(List<ItemStack> list, int i) {
		return i >= 0 && i < list.size() ? (ItemStack)list.set(i, ItemStack.EMPTY) : ItemStack.EMPTY;
	}

	public static CompoundTag saveAllItems(CompoundTag compoundTag, NonNullList<ItemStack> nonNullList) {
		return saveAllItems(compoundTag, nonNullList, true);
	}

	public static CompoundTag saveAllItems(CompoundTag compoundTag, NonNullList<ItemStack> nonNullList, boolean bl) {
		ListTag listTag = new ListTag();

		for (int i = 0; i < nonNullList.size(); i++) {
			ItemStack itemStack = nonNullList.get(i);
			if (!itemStack.isEmpty()) {
				CompoundTag compoundTag2 = new CompoundTag();
				compoundTag2.putByte("Slot", (byte)i);
				itemStack.save(compoundTag2);
				listTag.add(compoundTag2);
			}
		}

		if (!listTag.isEmpty() || bl) {
			compoundTag.put("Items", listTag);
		}

		return compoundTag;
	}

	public static void loadAllItems(CompoundTag compoundTag, NonNullList<ItemStack> nonNullList) {
		ListTag listTag = compoundTag.getList("Items", 10);

		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag2 = listTag.getCompound(i);
			int j = compoundTag2.getByte("Slot") & 255;
			if (j >= 0 && j < nonNullList.size()) {
				nonNullList.set(j, ItemStack.of(compoundTag2));
			}
		}
	}

	public static int clearOrCountMatchingItems(Container container, Predicate<ItemStack> predicate, int i, boolean bl) {
		int j = 0;

		for (int k = 0; k < container.getContainerSize(); k++) {
			ItemStack itemStack = container.getItem(k);
			int l = clearOrCountMatchingItems(itemStack, predicate, i - j, bl);
			if (l > 0 && !bl && itemStack.isEmpty()) {
				container.setItem(k, ItemStack.EMPTY);
			}

			j += l;
		}

		return j;
	}

	public static int clearOrCountMatchingItems(ItemStack itemStack, Predicate<ItemStack> predicate, int i, boolean bl) {
		if (itemStack.isEmpty() || !predicate.test(itemStack)) {
			return 0;
		} else if (bl) {
			return itemStack.getCount();
		} else {
			int j = i < 0 ? itemStack.getCount() : Math.min(i, itemStack.getCount());
			itemStack.shrink(j);
			return j;
		}
	}
}
