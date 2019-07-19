package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownEnderpearl extends ThrowableItemProjectile {
	private LivingEntity originalOwner;

	public ThrownEnderpearl(EntityType<? extends ThrownEnderpearl> entityType, Level level) {
		super(entityType, level);
	}

	public ThrownEnderpearl(Level level, LivingEntity livingEntity) {
		super(EntityType.ENDER_PEARL, livingEntity, level);
		this.originalOwner = livingEntity;
	}

	@Environment(EnvType.CLIENT)
	public ThrownEnderpearl(Level level, double d, double e, double f) {
		super(EntityType.ENDER_PEARL, d, e, f, level);
	}

	@Override
	protected Item getDefaultItem() {
		return Items.ENDER_PEARL;
	}

	@Override
	protected void onHit(HitResult hitResult) {
		LivingEntity livingEntity = this.getOwner();
		if (hitResult.getType() == HitResult.Type.ENTITY) {
			Entity entity = ((EntityHitResult)hitResult).getEntity();
			if (entity == this.originalOwner) {
				return;
			}

			entity.hurt(DamageSource.thrown(this, livingEntity), 0.0F);
		}

		if (hitResult.getType() == HitResult.Type.BLOCK) {
			BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
			BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
			if (blockEntity instanceof TheEndGatewayBlockEntity) {
				TheEndGatewayBlockEntity theEndGatewayBlockEntity = (TheEndGatewayBlockEntity)blockEntity;
				if (livingEntity != null) {
					if (livingEntity instanceof ServerPlayer) {
						CriteriaTriggers.ENTER_BLOCK.trigger((ServerPlayer)livingEntity, this.level.getBlockState(blockPos));
					}

					theEndGatewayBlockEntity.teleportEntity(livingEntity);
					this.remove();
					return;
				}

				theEndGatewayBlockEntity.teleportEntity(this);
				return;
			}
		}

		for (int i = 0; i < 32; i++) {
			this.level
				.addParticle(ParticleTypes.PORTAL, this.x, this.y + this.random.nextDouble() * 2.0, this.z, this.random.nextGaussian(), 0.0, this.random.nextGaussian());
		}

		if (!this.level.isClientSide) {
			if (livingEntity instanceof ServerPlayer) {
				ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
				if (serverPlayer.connection.getConnection().isConnected() && serverPlayer.level == this.level && !serverPlayer.isSleeping()) {
					if (this.random.nextFloat() < 0.05F && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
						Endermite endermite = EntityType.ENDERMITE.create(this.level);
						endermite.setPlayerSpawned(true);
						endermite.moveTo(livingEntity.x, livingEntity.y, livingEntity.z, livingEntity.yRot, livingEntity.xRot);
						this.level.addFreshEntity(endermite);
					}

					if (livingEntity.isPassenger()) {
						livingEntity.stopRiding();
					}

					livingEntity.teleportTo(this.x, this.y, this.z);
					livingEntity.fallDistance = 0.0F;
					livingEntity.hurt(DamageSource.FALL, 5.0F);
				}
			} else if (livingEntity != null) {
				livingEntity.teleportTo(this.x, this.y, this.z);
				livingEntity.fallDistance = 0.0F;
			}

			this.remove();
		}
	}

	@Override
	public void tick() {
		LivingEntity livingEntity = this.getOwner();
		if (livingEntity != null && livingEntity instanceof Player && !livingEntity.isAlive()) {
			this.remove();
		} else {
			super.tick();
		}
	}

	@Nullable
	@Override
	public Entity changeDimension(DimensionType dimensionType) {
		if (this.owner.dimension != dimensionType) {
			this.owner = null;
		}

		return super.changeDimension(dimensionType);
	}
}
