package net.minecraft.world.entity.projectile;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownEnderpearl extends ThrowableItemProjectile {
	private long ticketTimer = 0L;

	public ThrownEnderpearl(EntityType<? extends ThrownEnderpearl> entityType, Level level) {
		super(entityType, level);
	}

	public ThrownEnderpearl(Level level, LivingEntity livingEntity, ItemStack itemStack) {
		super(EntityType.ENDER_PEARL, livingEntity, level, itemStack);
	}

	@Override
	protected Item getDefaultItem() {
		return Items.ENDER_PEARL;
	}

	@Override
	protected void setOwnerThroughUUID(UUID uUID) {
		this.deregisterFromCurrentOwner();
		super.setOwnerThroughUUID(uUID);
		this.registerToCurrentOwner();
	}

	@Override
	public void setOwner(@Nullable Entity entity) {
		this.deregisterFromCurrentOwner();
		super.setOwner(entity);
		this.registerToCurrentOwner();
	}

	private void deregisterFromCurrentOwner() {
		if (this.getOwner() instanceof ServerPlayer serverPlayer) {
			serverPlayer.deregisterEnderPearl(this);
		}
	}

	private void registerToCurrentOwner() {
		if (this.getOwner() instanceof ServerPlayer serverPlayer) {
			serverPlayer.registerEnderPearl(this);
		}
	}

	@Nullable
	@Override
	protected Entity findOwner(UUID uUID) {
		if (this.level() instanceof ServerLevel serverLevel) {
			Entity entity = super.findOwner(uUID);
			if (entity != null) {
				return entity;
			} else {
				for (ServerLevel serverLevel2 : serverLevel.getServer().getAllLevels()) {
					if (serverLevel2 != serverLevel) {
						entity = serverLevel2.getEntity(uUID);
						if (entity != null) {
							return entity;
						}
					}
				}

				return null;
			}
		} else {
			return null;
		}
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
					entity.unRide();
				}

				Vec3 vec33;
				if (this.getDeltaMovement().lengthSqr() > 0.0) {
					AABB aABB = entity.getBoundingBox();
					Vec3 vec3 = new Vec3(aABB.getXsize(), aABB.getYsize(), aABB.getZsize()).scale(0.5000099999997474);
					Vec3 vec32 = new Vec3(Math.signum(this.getDeltaMovement().x), Math.signum(this.getDeltaMovement().y), Math.signum(this.getDeltaMovement().z));
					vec33 = vec32.multiply(vec3).add(0.0, aABB.getYsize() * 0.5, 0.0);
				} else {
					vec33 = Vec3.ZERO;
				}

				Vec3 vec34 = this.position().subtract(vec33);
				if (entity instanceof ServerPlayer serverPlayer) {
					if (serverPlayer.connection.isAcceptingMessages()) {
						if (this.random.nextFloat() < 0.05F && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
							Endermite endermite = EntityType.ENDERMITE.create(serverLevel, EntitySpawnReason.TRIGGERED);
							if (endermite != null) {
								endermite.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
								serverLevel.addFreshEntity(endermite);
							}
						}

						Player player = serverPlayer.changeDimension(
							new DimensionTransition(serverLevel, vec34, Vec3.ZERO, 0.0F, 0.0F, Relative.union(Relative.ROTATION, Relative.DELTA), DimensionTransition.DO_NOTHING)
						);
						if (player != null) {
							player.resetFallDistance();
							player.resetCurrentImpulseContext();
							player.hurt(this.damageSources().enderPearl(), 5.0F);
						}

						this.playSound(serverLevel, vec34);
					}
				} else {
					Entity entity2 = entity.changeDimension(
						new DimensionTransition(serverLevel, vec34, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING)
					);
					if (entity2 != null) {
						entity2.resetFallDistance();
					}

					this.playSound(serverLevel, vec34);
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
			return entity.canUsePortal(true);
		}
	}

	@Override
	public void tick() {
		int i = SectionPos.blockToSectionCoord(this.position().x());
		int j = SectionPos.blockToSectionCoord(this.position().z());
		Entity entity = this.getOwner();
		if (entity instanceof ServerPlayer && !entity.isAlive() && this.level().getGameRules().getBoolean(GameRules.RULE_ENDER_PEARLS_VANISH_ON_DEATH)) {
			this.discard();
		} else {
			super.tick();
		}

		BlockPos blockPos = BlockPos.containing(this.position());
		if ((--this.ticketTimer <= 0L || i != SectionPos.blockToSectionCoord(blockPos.getX()) || j != SectionPos.blockToSectionCoord(blockPos.getZ()))
			&& entity instanceof ServerPlayer serverPlayer) {
			this.ticketTimer = serverPlayer.registerAndUpdateEnderPearlTicket(this);
		}
	}

	private void playSound(Level level, Vec3 vec3) {
		level.playSound(null, vec3.x, vec3.y, vec3.z, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
	}

	@Nullable
	@Override
	public Entity changeDimension(DimensionTransition dimensionTransition) {
		Entity entity = super.changeDimension(dimensionTransition);
		if (entity != null) {
			entity.placePortalTicket(BlockPos.containing(entity.position()));
		}

		return entity;
	}

	@Override
	public boolean canChangeDimensions(Level level, Level level2) {
		return level.dimension() == Level.END && level2.dimension() == Level.OVERWORLD && this.getOwner() instanceof ServerPlayer serverPlayer
			? super.canChangeDimensions(level, level2) && serverPlayer.seenCredits
			: super.canChangeDimensions(level, level2);
	}

	@Override
	protected void onInsideBlock(BlockState blockState) {
		super.onInsideBlock(blockState);
		if (blockState.is(Blocks.END_GATEWAY) && this.getOwner() instanceof ServerPlayer serverPlayer) {
			serverPlayer.onInsideBlock(blockState);
		}
	}

	@Override
	public void remove(Entity.RemovalReason removalReason) {
		if (removalReason != Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
			this.deregisterFromCurrentOwner();
		}

		super.remove(removalReason);
	}
}
