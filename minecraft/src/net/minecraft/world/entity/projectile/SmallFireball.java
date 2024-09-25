package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SmallFireball extends Fireball {
	public SmallFireball(EntityType<? extends SmallFireball> entityType, Level level) {
		super(entityType, level);
	}

	public SmallFireball(Level level, LivingEntity livingEntity, Vec3 vec3) {
		super(EntityType.SMALL_FIREBALL, livingEntity, vec3, level);
	}

	public SmallFireball(Level level, double d, double e, double f, Vec3 vec3) {
		super(EntityType.SMALL_FIREBALL, d, e, f, vec3, level);
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		if (this.level() instanceof ServerLevel serverLevel) {
			Entity var7 = entityHitResult.getEntity();
			Entity entity2 = this.getOwner();
			int i = var7.getRemainingFireTicks();
			var7.igniteForSeconds(5.0F);
			DamageSource damageSource = this.damageSources().fireball(this, entity2);
			if (!var7.hurtServer(serverLevel, damageSource, 5.0F)) {
				var7.setRemainingFireTicks(i);
			} else {
				EnchantmentHelper.doPostAttackEffects(serverLevel, var7, damageSource);
			}
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult blockHitResult) {
		super.onHitBlock(blockHitResult);
		if (this.level() instanceof ServerLevel serverLevel) {
			Entity entity = this.getOwner();
			if (!(entity instanceof Mob) || serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
				BlockPos blockPos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
				if (this.level().isEmptyBlock(blockPos)) {
					this.level().setBlockAndUpdate(blockPos, BaseFireBlock.getState(this.level(), blockPos));
				}
			}
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (!this.level().isClientSide) {
			this.discard();
		}
	}
}
