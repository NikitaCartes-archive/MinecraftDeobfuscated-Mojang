package net.minecraft.world.entity;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

public class GlowSquid extends Squid {
	private static final EntityDataAccessor<Integer> DATA_DARK_TICKS_REMAINING = SynchedEntityData.defineId(GlowSquid.class, EntityDataSerializers.INT);

	public GlowSquid(EntityType<? extends GlowSquid> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected ParticleOptions getInkParticle() {
		return ParticleTypes.GLOW_SQUID_INK;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_DARK_TICKS_REMAINING, 0);
	}

	@Override
	protected SoundEvent getSquirtSound() {
		return SoundEvents.GLOW_SQUID_SQUIRT;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.GLOW_SQUID_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.GLOW_SQUID_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.GLOW_SQUID_DEATH;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("DarkTicksRemaining", this.getDarkTicksRemaining());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setDarkTicks(compoundTag.getInt("DarkTicksRemaining"));
	}

	@Override
	public void aiStep() {
		super.aiStep();
		int i = this.getDarkTicksRemaining();
		if (i > 0) {
			this.setDarkTicks(i - 1);
		}

		this.level.addParticle(ParticleTypes.GLOW, this.getRandomX(0.6), this.getRandomY(), this.getRandomZ(0.6), 0.0, 0.0, 0.0);
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		boolean bl = super.hurt(damageSource, f);
		if (bl) {
			this.setDarkTicks(100);
		}

		return bl;
	}

	private void setDarkTicks(int i) {
		this.entityData.set(DATA_DARK_TICKS_REMAINING, i);
	}

	public int getDarkTicksRemaining() {
		return this.entityData.get(DATA_DARK_TICKS_REMAINING);
	}

	public static boolean checkGlowSquideSpawnRules(
		EntityType<? extends LivingEntity> entityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		return blockPos.getY() <= serverLevelAccessor.getSeaLevel() - 33
			&& serverLevelAccessor.getRawBrightness(blockPos, 0) == 0
			&& serverLevelAccessor.getBlockState(blockPos).is(Blocks.WATER);
	}
}
