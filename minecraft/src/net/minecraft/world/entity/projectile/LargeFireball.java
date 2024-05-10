package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LargeFireball extends Fireball {
	private int explosionPower = 1;

	public LargeFireball(EntityType<? extends LargeFireball> entityType, Level level) {
		super(entityType, level);
	}

	public LargeFireball(Level level, LivingEntity livingEntity, Vec3 vec3, int i) {
		super(EntityType.FIREBALL, livingEntity, vec3, level);
		this.explosionPower = i;
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (!this.level().isClientSide) {
			boolean bl = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
			this.level().explode(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, bl, Level.ExplosionInteraction.MOB);
			this.discard();
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		if (this.level() instanceof ServerLevel serverLevel) {
			Entity var6 = entityHitResult.getEntity();
			Entity entity2 = this.getOwner();
			DamageSource damageSource = this.damageSources().fireball(this, entity2);
			var6.hurt(damageSource, 6.0F);
			EnchantmentHelper.doPostAttackEffects(serverLevel, var6, damageSource);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putByte("ExplosionPower", (byte)this.explosionPower);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("ExplosionPower", 99)) {
			this.explosionPower = compoundTag.getByte("ExplosionPower");
		}
	}
}
