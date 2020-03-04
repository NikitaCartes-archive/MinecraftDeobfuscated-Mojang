package net.minecraft.world.entity.projectile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class LargeFireball extends Fireball {
	public int explosionPower = 1;

	public LargeFireball(EntityType<? extends LargeFireball> entityType, Level level) {
		super(entityType, level);
	}

	@Environment(EnvType.CLIENT)
	public LargeFireball(Level level, double d, double e, double f, double g, double h, double i) {
		super(EntityType.FIREBALL, d, e, f, g, h, i, level);
	}

	public LargeFireball(Level level, LivingEntity livingEntity, double d, double e, double f) {
		super(EntityType.FIREBALL, livingEntity, d, e, f, level);
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (!this.level.isClientSide) {
			boolean bl = this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
			this.level
				.explode(
					null, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, bl, bl ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE
				);
			this.remove();
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		if (!this.level.isClientSide) {
			Entity entity = entityHitResult.getEntity();
			Entity entity2 = this.getOwner();
			entity.hurt(DamageSource.fireball(this, entity2), 6.0F);
			if (entity2 instanceof LivingEntity) {
				this.doEnchantDamageEffects((LivingEntity)entity2, entity);
			}
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("ExplosionPower", this.explosionPower);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("ExplosionPower", 99)) {
			this.explosionPower = compoundTag.getInt("ExplosionPower");
		}
	}
}
