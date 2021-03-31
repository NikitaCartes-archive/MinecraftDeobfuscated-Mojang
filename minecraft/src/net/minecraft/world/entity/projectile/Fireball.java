package net.minecraft.world.entity.projectile;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public abstract class Fireball extends AbstractHurtingProjectile implements ItemSupplier {
	private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(Fireball.class, EntityDataSerializers.ITEM_STACK);

	public Fireball(EntityType<? extends Fireball> entityType, Level level) {
		super(entityType, level);
	}

	public Fireball(EntityType<? extends Fireball> entityType, double d, double e, double f, double g, double h, double i, Level level) {
		super(entityType, d, e, f, g, h, i, level);
	}

	public Fireball(EntityType<? extends Fireball> entityType, LivingEntity livingEntity, double d, double e, double f, Level level) {
		super(entityType, livingEntity, d, e, f, level);
	}

	public void setItem(ItemStack itemStack) {
		if (!itemStack.is(Items.FIRE_CHARGE) || itemStack.hasTag()) {
			this.getEntityData().set(DATA_ITEM_STACK, Util.make(itemStack.copy(), itemStackx -> itemStackx.setCount(1)));
		}
	}

	protected ItemStack getItemRaw() {
		return this.getEntityData().get(DATA_ITEM_STACK);
	}

	@Override
	public ItemStack getItem() {
		ItemStack itemStack = this.getItemRaw();
		return itemStack.isEmpty() ? new ItemStack(Items.FIRE_CHARGE) : itemStack;
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
