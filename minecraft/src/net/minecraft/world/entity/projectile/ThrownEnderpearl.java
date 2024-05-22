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
import org.jetbrains.annotations.Nullable;

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
			if (entity instanceof ServerPlayer serverPlayer) {
				if (serverPlayer.connection.isAcceptingMessages() && serverPlayer.canChangeDimensions()) {
					if (this.random.nextFloat() < 0.05F && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
						Endermite endermite = EntityType.ENDERMITE.create(serverLevel);
						if (endermite != null) {
							endermite.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
							serverLevel.addFreshEntity(endermite);
						}
					}

					if (entity.isPassenger()) {
						this.unRide();
					}

					entity.changeDimension(new DimensionTransition(serverLevel, this.position(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot()));
					entity.resetFallDistance();
					entity.hurt(this.damageSources().fall(), 5.0F);
					this.playSound(serverLevel, this.position());
				}
			} else if (entity != null) {
				entity.changeDimension(new DimensionTransition(serverLevel, this.position(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot()));
				entity.resetFallDistance();
			}

			this.discard();
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

	@Nullable
	@Override
	public Entity changeDimension(DimensionTransition dimensionTransition) {
		if (this.level().dimension() != dimensionTransition.newLevel().dimension()) {
			this.disown();
		}

		return super.changeDimension(dimensionTransition);
	}

	private void playSound(Level level, Vec3 vec3) {
		level.playSound(null, vec3.x, vec3.y, vec3.z, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
	}
}
