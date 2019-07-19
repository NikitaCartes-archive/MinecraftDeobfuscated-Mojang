package net.minecraft.world.entity.projectile;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class DragonFireball extends AbstractHurtingProjectile {
	public DragonFireball(EntityType<? extends DragonFireball> entityType, Level level) {
		super(entityType, level);
	}

	@Environment(EnvType.CLIENT)
	public DragonFireball(Level level, double d, double e, double f, double g, double h, double i) {
		super(EntityType.DRAGON_FIREBALL, d, e, f, g, h, i, level);
	}

	public DragonFireball(Level level, LivingEntity livingEntity, double d, double e, double f) {
		super(EntityType.DRAGON_FIREBALL, livingEntity, d, e, f, level);
	}

	@Override
	protected void onHit(HitResult hitResult) {
		if (hitResult.getType() != HitResult.Type.ENTITY || !((EntityHitResult)hitResult).getEntity().is(this.owner)) {
			if (!this.level.isClientSide) {
				List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0, 2.0, 4.0));
				AreaEffectCloud areaEffectCloud = new AreaEffectCloud(this.level, this.x, this.y, this.z);
				areaEffectCloud.setOwner(this.owner);
				areaEffectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
				areaEffectCloud.setRadius(3.0F);
				areaEffectCloud.setDuration(600);
				areaEffectCloud.setRadiusPerTick((7.0F - areaEffectCloud.getRadius()) / (float)areaEffectCloud.getDuration());
				areaEffectCloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
				if (!list.isEmpty()) {
					for (LivingEntity livingEntity : list) {
						double d = this.distanceToSqr(livingEntity);
						if (d < 16.0) {
							areaEffectCloud.setPos(livingEntity.x, livingEntity.y, livingEntity.z);
							break;
						}
					}
				}

				this.level.levelEvent(2006, new BlockPos(this.x, this.y, this.z), 0);
				this.level.addFreshEntity(areaEffectCloud);
				this.remove();
			}
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
	protected ParticleOptions getTrailParticle() {
		return ParticleTypes.DRAGON_BREATH;
	}

	@Override
	protected boolean shouldBurn() {
		return false;
	}
}
