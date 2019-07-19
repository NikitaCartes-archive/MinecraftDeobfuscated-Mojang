package net.minecraft.world.entity.projectile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.GameRules;
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

	@Environment(EnvType.CLIENT)
	public WitherSkull(Level level, double d, double e, double f, double g, double h, double i) {
		super(EntityType.WITHER_SKULL, d, e, f, g, h, i, level);
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
	protected void onHit(HitResult hitResult) {
		if (!this.level.isClientSide) {
			if (hitResult.getType() == HitResult.Type.ENTITY) {
				Entity entity = ((EntityHitResult)hitResult).getEntity();
				if (this.owner != null) {
					if (entity.hurt(DamageSource.mobAttack(this.owner), 8.0F)) {
						if (entity.isAlive()) {
							this.doEnchantDamageEffects(this.owner, entity);
						} else {
							this.owner.heal(5.0F);
						}
					}
				} else {
					entity.hurt(DamageSource.MAGIC, 5.0F);
				}

				if (entity instanceof LivingEntity) {
					int i = 0;
					if (this.level.getDifficulty() == Difficulty.NORMAL) {
						i = 10;
					} else if (this.level.getDifficulty() == Difficulty.HARD) {
						i = 40;
					}

					if (i > 0) {
						((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * i, 1));
					}
				}
			}

			Explosion.BlockInteraction blockInteraction = this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
				? Explosion.BlockInteraction.DESTROY
				: Explosion.BlockInteraction.NONE;
			this.level.explode(this, this.x, this.y, this.z, 1.0F, false, blockInteraction);
			this.remove();
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
}
