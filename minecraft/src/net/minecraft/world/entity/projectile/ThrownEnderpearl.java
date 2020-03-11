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
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		Entity entity = entityHitResult.getEntity();
		if (entity != this.originalOwner) {
			entity.hurt(DamageSource.thrown(this, this.getOwner()), 0.0F);
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult blockHitResult) {
		super.onHitBlock(blockHitResult);
		Entity entity = this.getOwner();
		BlockPos blockPos = blockHitResult.getBlockPos();
		BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
		if (blockEntity instanceof TheEndGatewayBlockEntity) {
			TheEndGatewayBlockEntity theEndGatewayBlockEntity = (TheEndGatewayBlockEntity)blockEntity;
			if (entity != null) {
				if (entity instanceof ServerPlayer) {
					CriteriaTriggers.ENTER_BLOCK.trigger((ServerPlayer)entity, this.level.getBlockState(blockPos));
				}

				theEndGatewayBlockEntity.teleportEntity(entity);
				this.remove();
			} else {
				theEndGatewayBlockEntity.teleportEntity(this);
			}
		}
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

		if (!this.level.isClientSide && !this.removed) {
			if (entity instanceof ServerPlayer) {
				ServerPlayer serverPlayer = (ServerPlayer)entity;
				if (serverPlayer.connection.getConnection().isConnected() && serverPlayer.level == this.level && !serverPlayer.isSleeping()) {
					if (this.random.nextFloat() < 0.05F && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
						Endermite endermite = EntityType.ENDERMITE.create(this.level);
						endermite.setPlayerSpawned(true);
						endermite.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.yRot, entity.xRot);
						this.level.addFreshEntity(endermite);
					}

					if (entity.isPassenger()) {
						entity.stopRiding();
					}

					entity.teleportTo(this.getX(), this.getY(), this.getZ());
					entity.fallDistance = 0.0F;
					entity.hurt(DamageSource.FALL, 5.0F);
				}
			} else if (entity != null) {
				entity.teleportTo(this.getX(), this.getY(), this.getZ());
				entity.fallDistance = 0.0F;
			}

			this.remove();
		}
	}

	@Override
	public void tick() {
		Entity entity = this.getOwner();
		if (entity != null && entity instanceof Player && !entity.isAlive()) {
			this.remove();
		} else {
			super.tick();
		}
	}

	@Nullable
	@Override
	public Entity changeDimension(DimensionType dimensionType) {
		Entity entity = this.getOwner();
		if (entity.dimension != dimensionType) {
			this.setOwner(null);
		}

		return super.changeDimension(dimensionType);
	}
}
