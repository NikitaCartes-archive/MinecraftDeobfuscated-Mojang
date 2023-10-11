package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class WitherSkull extends AbstractHurtingProjectile {
	private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(WitherSkull.class, EntityDataSerializers.BOOLEAN);

	public WitherSkull(EntityType<? extends WitherSkull> entityType, Level level) {
		super(entityType, level);
	}

	public WitherSkull(Level level, LivingEntity livingEntity, double d, double e, double f) {
		super(EntityType.WITHER_SKULL, livingEntity, d, e, f, level);
	}

	@Override
	protected float getInertia() {
		return this.isDangerous() ? 0.73F : super.getInertia();
	}

	@Override
	public boolean isOnFire() {
		return false;
	}

	@Override
	public float getBlockExplosionResistance(
		Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState, float f
	) {
		return this.isDangerous() && WitherBoss.canDestroy(blockState) ? Math.min(0.8F, f) : f;
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		if (!this.level().isClientSide) {
			Entity entity = entityHitResult.getEntity();
			boolean bl;
			if (this.getOwner() instanceof LivingEntity livingEntity) {
				bl = entity.hurt(this.damageSources().witherSkull(this, livingEntity), 8.0F);
				if (bl) {
					if (entity.isAlive()) {
						this.doEnchantDamageEffects(livingEntity, entity);
					} else {
						livingEntity.heal(5.0F);
					}
				}
			} else {
				bl = entity.hurt(this.damageSources().magic(), 5.0F);
			}

			if (bl && entity instanceof LivingEntity livingEntityx) {
				int i = 0;
				if (this.level().getDifficulty() == Difficulty.NORMAL) {
					i = 10;
				} else if (this.level().getDifficulty() == Difficulty.HARD) {
					i = 40;
				}

				if (i > 0) {
					livingEntityx.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * i, 1), this.getEffectSource());
				}
			}
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (!this.level().isClientSide) {
			this.level().explode(this, this.getX(), this.getY(), this.getZ(), 1.0F, false, Level.ExplosionInteraction.MOB);
			this.discard();
		}
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		return false;
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_DANGEROUS, false);
	}

	public boolean isDangerous() {
		return this.entityData.get(DATA_DANGEROUS);
	}

	public void setDangerous(boolean bl) {
		this.entityData.set(DATA_DANGEROUS, bl);
	}

	@Override
	protected boolean shouldBurn() {
		return false;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("dangerous", this.isDangerous());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setDangerous(compoundTag.getBoolean("dangerous"));
	}
}
