package net.minecraft.world.entity.projectile;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class ThrowableItemProjectile extends ThrowableProjectile implements ItemSupplier {
	private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(
		ThrowableItemProjectile.class, EntityDataSerializers.ITEM_STACK
	);

	public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
		super(entityType, level);
	}

	public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> entityType, double d, double e, double f, Level level) {
		super(entityType, d, e, f, level);
	}

	public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> entityType, LivingEntity livingEntity, Level level) {
		super(entityType, livingEntity, level);
	}

	public void setItem(ItemStack itemStack) {
		if (!itemStack.is(this.getDefaultItem()) || itemStack.hasTag()) {
			this.getEntityData().set(DATA_ITEM_STACK, Util.make(itemStack.copy(), itemStackx -> itemStackx.setCount(1)));
		}
	}

	protected abstract Item getDefaultItem();

	protected ItemStack getItemRaw() {
		return this.getEntityData().get(DATA_ITEM_STACK);
	}

	@Override
	public ItemStack getItem() {
		ItemStack itemStack = this.getItemRaw();
		return itemStack.isEmpty() ? new ItemStack(this.getDefaultItem()) : itemStack;
	}

	@Override
	protected void defineSynchedData() {
		this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		ItemStack itemStack = this.getItemRaw();
		if (!itemStack.isEmpty()) {
			compoundTag.put("Item", itemStack.save(new CompoundTag()));
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		ItemStack itemStack = ItemStack.of(compoundTag.getCompound("Item"));
		this.setItem(itemStack);
	}
}
