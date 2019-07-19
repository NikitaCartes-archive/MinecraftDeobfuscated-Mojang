package net.minecraft.client.player.inventory;

import com.google.common.collect.ForwardingList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class Hotbar extends ForwardingList<ItemStack> {
	private final NonNullList<ItemStack> items = NonNullList.withSize(Inventory.getSelectionSize(), ItemStack.EMPTY);

	@Override
	protected List<ItemStack> delegate() {
		return this.items;
	}

	public ListTag createTag() {
		ListTag listTag = new ListTag();

		for (ItemStack itemStack : this.delegate()) {
			listTag.add(itemStack.save(new CompoundTag()));
		}

		return listTag;
	}

	public void fromTag(ListTag listTag) {
		List<ItemStack> list = this.delegate();

		for (int i = 0; i < list.size(); i++) {
			list.set(i, ItemStack.of(listTag.getCompound(i)));
		}
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemStack : this.delegate()) {
			if (!itemStack.isEmpty()) {
				return false;
			}
		}

		return true;
	}
}
