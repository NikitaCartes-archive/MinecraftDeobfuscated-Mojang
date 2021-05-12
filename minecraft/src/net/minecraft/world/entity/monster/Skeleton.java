package net.minecraft.world.entity.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class Skeleton extends AbstractSkeleton {
	private static final EntityDataAccessor<Boolean> DATA_STRAY_CONVERSION_ID = SynchedEntityData.defineId(Skeleton.class, EntityDataSerializers.BOOLEAN);
	public static final String CONVERSION_TAG = "StrayConversionTime";
	private int inPowderSnowTime;
	private int conversionTime;

	public Skeleton(EntityType<? extends Skeleton> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.getEntityData().define(DATA_STRAY_CONVERSION_ID, false);
	}

	public boolean isFreezeConverting() {
		return this.getEntityData().get(DATA_STRAY_CONVERSION_ID);
	}

	public void setFreezeConverting(boolean bl) {
		this.entityData.set(DATA_STRAY_CONVERSION_ID, bl);
	}

	@Override
	public boolean isShaking() {
		return this.isFreezeConverting();
	}

	@Override
	public void tick() {
		if (!this.level.isClientSide && this.isAlive() && !this.isNoAi()) {
			if (this.isFreezeConverting()) {
				this.conversionTime--;
				if (this.conversionTime < 0) {
					this.doFreezeConversion();
				}
			} else if (this.isInPowderSnow) {
				this.inPowderSnowTime++;
				if (this.inPowderSnowTime >= 140) {
					this.startFreezeConversion(300);
				}
			} else {
				this.inPowderSnowTime = -1;
			}
		}

		super.tick();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("StrayConversionTime", this.isFreezeConverting() ? this.conversionTime : -1);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("StrayConversionTime", 99) && compoundTag.getInt("StrayConversionTime") > -1) {
			this.startFreezeConversion(compoundTag.getInt("StrayConversionTime"));
		}
	}

	private void startFreezeConversion(int i) {
		this.conversionTime = i;
		this.entityData.set(DATA_STRAY_CONVERSION_ID, true);
	}

	protected void doFreezeConversion() {
		this.convertTo(EntityType.STRAY, true);
		if (!this.isSilent()) {
			this.level.levelEvent(null, 1048, this.blockPosition(), 0);
		}
	}

	@Override
	public boolean canFreeze() {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.SKELETON_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.SKELETON_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SKELETON_DEATH;
	}

	@Override
	SoundEvent getStepSound() {
		return SoundEvents.SKELETON_STEP;
	}

	@Override
	protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean bl) {
		super.dropCustomDeathLoot(damageSource, i, bl);
		if (damageSource.getEntity() instanceof Creeper creeper && creeper.canDropMobsSkull()) {
			creeper.increaseDroppedSkulls();
			this.spawnAtLocation(Items.SKELETON_SKULL);
		}
	}
}
