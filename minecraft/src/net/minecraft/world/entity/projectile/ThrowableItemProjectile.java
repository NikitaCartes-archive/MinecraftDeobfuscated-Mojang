package net.minecraft.world.entity.projectile;

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
		this.getEntityData().set(DATA_ITEM_STACK, itemStack.copyWithCount(1));
	}

	protected abstract Item getDefaultItem();

	@Override
	public ItemStack getItem() {
		return this.getEntityData().get(DATA_ITEM_STACK);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_ITEM_STACK, new ItemStack(this.getDefaultItem()));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.put("Item", this.getItem().save(this.registryAccess()));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("Item", 10)) {
			this.setItem((ItemStack)ItemStack.parse(this.registryAccess(), compoundTag.getCompound("Item")).orElseGet(() -> new ItemStack(this.getDefaultItem())));
		} else {
			this.setItem(new ItemStack(this.getDefaultItem()));
		}
	}
}
