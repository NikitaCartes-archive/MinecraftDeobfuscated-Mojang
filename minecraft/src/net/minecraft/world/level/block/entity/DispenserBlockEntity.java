package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class DispenserBlockEntity extends RandomizableContainerBlockEntity {
	public static final int CONTAINER_SIZE = 9;
	private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

	protected DispenserBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
	}

	public DispenserBlockEntity(BlockPos blockPos, BlockState blockState) {
		this(BlockEntityType.DISPENSER, blockPos, blockState);
	}

	@Override
	public int getContainerSize() {
		return 9;
	}

	public int getRandomSlot(RandomSource randomSource) {
		this.unpackLootTable(null);
		int i = -1;
		int j = 1;

		for (int k = 0; k < this.items.size(); k++) {
			if (!this.items.get(k).isEmpty() && randomSource.nextInt(j++) == 0) {
				i = k;
			}
		}

		return i;
	}

	public ItemStack insertItem(ItemStack itemStack) {
		int i = this.getMaxStackSize(itemStack);

		for (int j = 0; j < this.items.size(); j++) {
			ItemStack itemStack2 = this.items.get(j);
			if (itemStack2.isEmpty() || ItemStack.isSameItemSameComponents(itemStack, itemStack2)) {
				int k = Math.min(itemStack.getCount(), i - itemStack2.getCount());
				if (k > 0) {
					if (itemStack2.isEmpty()) {
						this.setItem(j, itemStack.split(k));
					} else {
						itemStack.shrink(k);
						itemStack2.grow(k);
					}
				}

				if (itemStack.isEmpty()) {
					break;
				}
			}
		}

		return itemStack;
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.dispenser");
	}

	@Override
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(compoundTag)) {
			ContainerHelper.loadAllItems(compoundTag, this.items, provider);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		if (!this.trySaveLootTable(compoundTag)) {
			ContainerHelper.saveAllItems(compoundTag, this.items, provider);
		}
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> nonNullList) {
		this.items = nonNullList;
	}

	@Override
	protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
		return new DispenserMenu(i, inventory, this);
	}
}
