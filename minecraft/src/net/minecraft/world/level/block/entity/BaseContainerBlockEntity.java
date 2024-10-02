package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseContainerBlockEntity extends BlockEntity implements Container, MenuProvider, Nameable {
	private LockCode lockKey = LockCode.NO_LOCK;
	@Nullable
	private Component name;

	protected BaseContainerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
	}

	@Override
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		this.lockKey = LockCode.fromTag(compoundTag, provider);
		if (compoundTag.contains("CustomName", 8)) {
			this.name = parseCustomNameSafe(compoundTag.getString("CustomName"), provider);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		this.lockKey.addToTag(compoundTag, provider);
		if (this.name != null) {
			compoundTag.putString("CustomName", Component.Serializer.toJson(this.name, provider));
		}
	}

	@Override
	public Component getName() {
		return this.name != null ? this.name : this.getDefaultName();
	}

	@Override
	public Component getDisplayName() {
		return this.getName();
	}

	@Nullable
	@Override
	public Component getCustomName() {
		return this.name;
	}

	protected abstract Component getDefaultName();

	public boolean canOpen(Player player) {
		return canUnlock(player, this.lockKey, this.getDisplayName());
	}

	public static boolean canUnlock(Player player, LockCode lockCode, Component component) {
		if (!player.isSpectator() && !lockCode.unlocksWith(player.getMainHandItem())) {
			player.displayClientMessage(Component.translatable("container.isLocked", component), true);
			player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
			return false;
		} else {
			return true;
		}
	}

	protected abstract NonNullList<ItemStack> getItems();

	protected abstract void setItems(NonNullList<ItemStack> nonNullList);

	@Override
	public boolean isEmpty() {
		for (ItemStack itemStack : this.getItems()) {
			if (!itemStack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public ItemStack getItem(int i) {
		return this.getItems().get(i);
	}

	@Override
	public ItemStack removeItem(int i, int j) {
		ItemStack itemStack = ContainerHelper.removeItem(this.getItems(), i, j);
		if (!itemStack.isEmpty()) {
			this.setChanged();
		}

		return itemStack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int i) {
		return ContainerHelper.takeItem(this.getItems(), i);
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		this.getItems().set(i, itemStack);
		itemStack.limitSize(this.getMaxStackSize(itemStack));
		this.setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.getItems().clear();
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
		return this.canOpen(player) ? this.createMenu(i, inventory) : null;
	}

	protected abstract AbstractContainerMenu createMenu(int i, Inventory inventory);

	@Override
	protected void applyImplicitComponents(BlockEntity.DataComponentInput dataComponentInput) {
		super.applyImplicitComponents(dataComponentInput);
		this.name = dataComponentInput.get(DataComponents.CUSTOM_NAME);
		this.lockKey = dataComponentInput.getOrDefault(DataComponents.LOCK, LockCode.NO_LOCK);
		dataComponentInput.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.getItems());
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder builder) {
		super.collectImplicitComponents(builder);
		builder.set(DataComponents.CUSTOM_NAME, this.name);
		if (!this.lockKey.equals(LockCode.NO_LOCK)) {
			builder.set(DataComponents.LOCK, this.lockKey);
		}

		builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
	}

	@Override
	public void removeComponentsFromTag(CompoundTag compoundTag) {
		compoundTag.remove("CustomName");
		compoundTag.remove("lock");
		compoundTag.remove("Items");
	}
}
