package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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
		entityHitResult.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);

		for (int i = 0; i < 32; i++) {
			this.level()
				.addParticle(
					ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0, this.getZ(), this.random.nextGaussian(), 0.0, this.random.nextGaussian()
				);
		}

		if (this.level() instanceof ServerLevel serverLevel && !this.isRemoved()) {
			Entity entity = this.getOwner();
			if (entity != null && isAllowedToTeleportOwner(entity, serverLevel)) {
				if (entity.isPassenger()) {
					this.unRide();
				}

				if (entity instanceof ServerPlayer serverPlayer) {
					if (serverPlayer.connection.isAcceptingMessages()) {
						if (this.random.nextFloat() < 0.05F && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
							Endermite endermite = EntityType.ENDERMITE.create(serverLevel);
							if (endermite != null) {
								endermite.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
								serverLevel.addFreshEntity(endermite);
							}
						}

						entity.changeDimension(
							new DimensionTransition(serverLevel, this.position(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING)
						);
						entity.resetFallDistance();
						entity.hurt(this.damageSources().fall(), 5.0F);
						this.playSound(serverLevel, this.position());
					}
				} else {
					entity.changeDimension(
						new DimensionTransition(serverLevel, this.position(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING)
					);
					entity.resetFallDistance();
					this.playSound(serverLevel, this.position());
				}

				this.discard();
				return;
			}

			this.discard();
			return;
		}
	}

	private static boolean isAllowedToTeleportOwner(Entity entity, Level level) {
		if (entity.level().dimension() == level.dimension()) {
			return !(entity instanceof LivingEntity livingEntity) ? entity.isAlive() : livingEntity.isAlive() && !livingEntity.isSleeping();
		} else {
			return entity.canChangeDimensions();
		}
	}

	@Override
	public void tick() {
		Entity entity = this.getOwner();
		if (entity instanceof ServerPlayer && !entity.isAlive() && this.level().getGameRules().getBoolean(GameRules.RULE_ENDER_PEARLS_VANISH_ON_DEATH)) {
			this.discard();
		} else {
			super.tick();
		}
	}

	private void playSound(Level level, Vec3 vec3) {
		level.playSound(null, vec3.x, vec3.y, vec3.z, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
	}
}
