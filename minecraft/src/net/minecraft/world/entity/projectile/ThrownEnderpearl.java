package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownEnderpearl extends ThrowableItemProjectile {
	public ThrownEnderpearl(EntityType<? extends ThrownEnderpearl> entityType, Level level) {
		super(entityType, level);
	}

	public ThrownEnderpearl(Level level, LivingEntity livingEntity) {
		super(EntityType.ENDER_PEARL, livingEntity, level);
	}

	@Override
	protected Item getDefaultItem() {
		return Items.ENDER_PEARL;
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		entityHitResult.getEntity().hurt(DamageSource.thrown(this, this.getOwner()), 0.0F);
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		Entity entity = this.getOwner();

		for (int i = 0; i < 32; i++) {
			this.level
				.addParticle(
					ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0, this.getZ(), this.random.nextGaussian(), 0.0, this.random.nextGaussian()
				);
		}

		if (!this.level.isClientSide && !this.isRemoved()) {
			if (entity instanceof ServerPlayer) {
				ServerPlayer serverPlayer = (ServerPlayer)entity;
				if (serverPlayer.connection.getConnection().isConnected() && serverPlayer.level == this.level && !serverPlayer.isSleeping()) {
					if (this.random.nextFloat() < 0.05F && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
						Endermite endermite = EntityType.ENDERMITE.create(this.level);
						endermite.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.yRot, entity.xRot);
						this.level.addFreshEntity(endermite);
					}

					if (entity.isPassenger()) {
						serverPlayer.dismountTo(this.getX(), this.getY(), this.getZ());
					} else {
						entity.teleportTo(this.getX(), this.getY(), this.getZ());
					}

					entity.fallDistance = 0.0F;
					entity.hurt(DamageSource.FALL, 5.0F);
				}
			} else if (entity != null) {
				entity.teleportTo(this.getX(), this.getY(), this.getZ());
				entity.fallDistance = 0.0F;
			}

			this.discard();
		}
	}

	@Override
	public void tick() {
		Entity entity = this.getOwner();
		if (entity instanceof Player && !entity.isAlive()) {
			this.discard();
		} else {
			super.tick();
		}
	}

	@Nullable
	@Override
	public Entity changeDimension(ServerLevel serverLevel) {
		Entity entity = this.getOwner();
		if (entity != null && entity.level.dimension() != serverLevel.dimension()) {
			this.setOwner(null);
		}

		return super.changeDimension(serverLevel);
	}
}
