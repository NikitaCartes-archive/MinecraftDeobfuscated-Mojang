package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

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
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_DARK_TICKS_REMAINING, 0);
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		return EntityType.GLOW_SQUID.create(serverLevel, EntitySpawnReason.BREEDING);
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

		this.level().addParticle(ParticleTypes.GLOW, this.getRandomX(0.6), this.getRandomY(), this.getRandomZ(0.6), 0.0, 0.0, 0.0);
	}

	@Override
	public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
		boolean bl = super.hurtServer(serverLevel, damageSource, f);
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

	public static boolean checkGlowSquidSpawnRules(
		EntityType<? extends LivingEntity> entityType,
		ServerLevelAccessor serverLevelAccessor,
		EntitySpawnReason entitySpawnReason,
		BlockPos blockPos,
		RandomSource randomSource
	) {
		return blockPos.getY() <= serverLevelAccessor.getSeaLevel() - 33
			&& serverLevelAccessor.getRawBrightness(blockPos, 0) == 0
			&& serverLevelAccessor.getBlockState(blockPos).is(Blocks.WATER);
	}
}
