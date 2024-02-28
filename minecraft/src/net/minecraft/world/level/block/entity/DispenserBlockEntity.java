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

	public int addItem(ItemStack itemStack) {
		for (int i = 0; i < this.items.size(); i++) {
			if (this.items.get(i).isEmpty()) {
				this.setItem(i, itemStack);
				return i;
			}
		}

		return -1;
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.dispenser");
	}

	@Override
	public void load(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.load(compoundTag, provider);
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
