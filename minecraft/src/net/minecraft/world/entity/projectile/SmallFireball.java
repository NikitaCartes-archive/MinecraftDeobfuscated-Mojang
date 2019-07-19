package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SmallFireball extends Fireball {
	public SmallFireball(EntityType<? extends SmallFireball> entityType, Level level) {
		super(entityType, level);
	}

	public SmallFireball(Level level, LivingEntity livingEntity, double d, double e, double f) {
		super(EntityType.SMALL_FIREBALL, livingEntity, d, e, f, level);
	}

	public SmallFireball(Level level, double d, double e, double f, double g, double h, double i) {
		super(EntityType.SMALL_FIREBALL, d, e, f, g, h, i, level);
	}

	@Override
	protected void onHit(HitResult hitResult) {
		if (!this.level.isClientSide) {
			if (hitResult.getType() == HitResult.Type.ENTITY) {
				Entity entity = ((EntityHitResult)hitResult).getEntity();
				if (!entity.fireImmune()) {
					int i = entity.getRemainingFireTicks();
					entity.setSecondsOnFire(5);
					boolean bl = entity.hurt(DamageSource.fireball(this, this.owner), 5.0F);
					if (bl) {
						this.doEnchantDamageEffects(this.owner, entity);
					} else {
						entity.setRemainingFireTicks(i);
					}
				}
			} else if (this.owner == null || !(this.owner instanceof Mob) || this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
				BlockHitResult blockHitResult = (BlockHitResult)hitResult;
				BlockPos blockPos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
				if (this.level.isEmptyBlock(blockPos)) {
					this.level.setBlockAndUpdate(blockPos, Blocks.FIRE.defaultBlockState());
				}
			}

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
}
