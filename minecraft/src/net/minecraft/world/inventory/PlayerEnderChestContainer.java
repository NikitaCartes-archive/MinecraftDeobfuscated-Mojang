package net.minecraft.world.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;

public class PlayerEnderChestContainer extends SimpleContainer {
	private EnderChestBlockEntity activeChest;

	public PlayerEnderChestContainer() {
		super(27);
	}

	public void setActiveChest(EnderChestBlockEntity enderChestBlockEntity) {
		this.activeChest = enderChestBlockEntity;
	}

	@Override
	public void fromTag(ListTag listTag) {
		for (int i = 0; i < this.getContainerSize(); i++) {
			this.setItem(i, ItemStack.EMPTY);
		}

		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag = listTag.getCompound(i);
			int j = compoundTag.getByte("Slot") & 255;
			if (j >= 0 && j < this.getContainerSize()) {
				this.setItem(j, ItemStack.of(compoundTag));
			}
		}
	}

	@Override
	public ListTag createTag() {
		ListTag listTag = new ListTag();

		for (int i = 0; i < this.getContainerSize(); i++) {
			ItemStack itemStack = this.getItem(i);
			if (!itemStack.isEmpty()) {
				CompoundTag compoundTag = new CompoundTag();
				compoundTag.putByte("Slot", (byte)i);
				itemStack.save(compoundTag);
				listTag.add(compoundTag);
			}
		}

		return listTag;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.activeChest != null && !this.activeChest.stillValid(player) ? false : super.stillValid(player);
	}

	@Override
	public void startOpen(Player player) {
		if (this.activeChest != null) {
			this.activeChest.startOpen();
		}

		super.startOpen(player);
	}

	@Override
	public void stopOpen(Player player) {
		if (this.activeChest != null) {
			this.activeChest.stopOpen();
		}

		super.stopOpen(player);
		this.activeChest = null;
	}
}
