package net.minecraft.world;

import java.util.List;
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
}
