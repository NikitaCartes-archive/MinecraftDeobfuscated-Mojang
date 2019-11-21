package net.minecraft.world.entity.projectile;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ShulkerBullet extends Entity {
	private LivingEntity owner;
	private Entity finalTarget;
	@Nullable
	private Direction currentMoveDirection;
	private int flightSteps;
	private double targetDeltaX;
	private double targetDeltaY;
	private double targetDeltaZ;
	@Nullable
	private UUID ownerId;
	private BlockPos lastKnownOwnerPos;
	@Nullable
	private UUID targetId;
	private BlockPos lastKnownTargetPos;

	public ShulkerBullet(EntityType<? extends ShulkerBullet> entityType, Level level) {
		super(entityType, level);
		this.noPhysics = true;
	}

	@Environment(EnvType.CLIENT)
	public ShulkerBullet(Level level, double d, double e, double f, double g, double h, double i) {
		this(EntityType.SHULKER_BULLET, level);
		this.moveTo(d, e, f, this.yRot, this.xRot);
		this.setDeltaMovement(g, h, i);
	}

	public ShulkerBullet(Level level, LivingEntity livingEntity, Entity entity, Direction.Axis axis) {
		this(EntityType.SHULKER_BULLET, level);
		this.owner = livingEntity;
		BlockPos blockPos = new BlockPos(livingEntity);
		double d = (double)blockPos.getX() + 0.5;
		double e = (double)blockPos.getY() + 0.5;
		double f = (double)blockPos.getZ() + 0.5;
		this.moveTo(d, e, f, this.yRot, this.xRot);
		this.finalTarget = entity;
		this.currentMoveDirection = Direction.UP;
		this.selectNextMoveDirection(axis);
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.HOSTILE;
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		if (this.owner != null) {
			BlockPos blockPos = new BlockPos(this.owner);
			CompoundTag compoundTag2 = NbtUtils.createUUIDTag(this.owner.getUUID());
			compoundTag2.putInt("X", blockPos.getX());
			compoundTag2.putInt("Y", blockPos.getY());
			compoundTag2.putInt("Z", blockPos.getZ());
			compoundTag.put("Owner", compoundTag2);
		}

		if (this.finalTarget != null) {
			BlockPos blockPos = new BlockPos(this.finalTarget);
			CompoundTag compoundTag2 = NbtUtils.createUUIDTag(this.finalTarget.getUUID());
			compoundTag2.putInt("X", blockPos.getX());
			compoundTag2.putInt("Y", blockPos.getY());
			compoundTag2.putInt("Z", blockPos.getZ());
			compoundTag.put("Target", compoundTag2);
		}

		if (this.currentMoveDirection != null) {
			compoundTag.putInt("Dir", this.currentMoveDirection.get3DDataValue());
		}

		compoundTag.putInt("Steps", this.flightSteps);
		compoundTag.putDouble("TXD", this.targetDeltaX);
		compoundTag.putDouble("TYD", this.targetDeltaY);
		compoundTag.putDouble("TZD", this.targetDeltaZ);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		this.flightSteps = compoundTag.getInt("Steps");
		this.targetDeltaX = compoundTag.getDouble("TXD");
		this.targetDeltaY = compoundTag.getDouble("TYD");
		this.targetDeltaZ = compoundTag.getDouble("TZD");
		if (compoundTag.contains("Dir", 99)) {
			this.currentMoveDirection = Direction.from3DDataValue(compoundTag.getInt("Dir"));
		}

		if (compoundTag.contains("Owner", 10)) {
			CompoundTag compoundTag2 = compoundTag.getCompound("Owner");
			this.ownerId = NbtUtils.loadUUIDTag(compoundTag2);
			this.lastKnownOwnerPos = new BlockPos(compoundTag2.getInt("X"), compoundTag2.getInt("Y"), compoundTag2.getInt("Z"));
		}

		if (compoundTag.contains("Target", 10)) {
			CompoundTag compoundTag2 = compoundTag.getCompound("Target");
			this.targetId = NbtUtils.loadUUIDTag(compoundTag2);
			this.lastKnownTargetPos = new BlockPos(compoundTag2.getInt("X"), compoundTag2.getInt("Y"), compoundTag2.getInt("Z"));
		}
	}

	@Override
	protected void defineSynchedData() {
	}

	private void setMoveDirection(@Nullable Direction direction) {
		this.currentMoveDirection = direction;
	}

	private void selectNextMoveDirection(@Nullable Direction.Axis axis) {
		double d = 0.5;
		BlockPos blockPos;
		if (this.finalTarget == null) {
			blockPos = new BlockPos(this).below();
		} else {
			d = (double)this.finalTarget.getBbHeight() * 0.5;
			blockPos = new BlockPos(this.finalTarget.getX(), this.finalTarget.getY() + d, this.finalTarget.getZ());
		}

		double e = (double)blockPos.getX() + 0.5;
		double f = (double)blockPos.getY() + d;
		double g = (double)blockPos.getZ() + 0.5;
		Direction direction = null;
		if (!blockPos.closerThan(this.position(), 2.0)) {
			BlockPos blockPos2 = new BlockPos(this);
			List<Direction> list = Lists.<Direction>newArrayList();
			if (axis != Direction.Axis.X) {
				if (blockPos2.getX() < blockPos.getX() && this.level.isEmptyBlock(blockPos2.east())) {
					list.add(Direction.EAST);
				} else if (blockPos2.getX() > blockPos.getX() && this.level.isEmptyBlock(blockPos2.west())) {
					list.add(Direction.WEST);
				}
			}

			if (axis != Direction.Axis.Y) {
				if (blockPos2.getY() < blockPos.getY() && this.level.isEmptyBlock(blockPos2.above())) {
					list.add(Direction.UP);
				} else if (blockPos2.getY() > blockPos.getY() && this.level.isEmptyBlock(blockPos2.below())) {
					list.add(Direction.DOWN);
				}
			}

			if (axis != Direction.Axis.Z) {
				if (blockPos2.getZ() < blockPos.getZ() && this.level.isEmptyBlock(blockPos2.south())) {
					list.add(Direction.SOUTH);
				} else if (blockPos2.getZ() > blockPos.getZ() && this.level.isEmptyBlock(blockPos2.north())) {
					list.add(Direction.NORTH);
				}
			}

			direction = Direction.getRandomFace(this.random);
			if (list.isEmpty()) {
				for (int i = 5; !this.level.isEmptyBlock(blockPos2.relative(direction)) && i > 0; i--) {
					direction = Direction.getRandomFace(this.random);
				}
			} else {
				direction = (Direction)list.get(this.random.nextInt(list.size()));
			}

			e = this.getX() + (double)direction.getStepX();
			f = this.getY() + (double)direction.getStepY();
			g = this.getZ() + (double)direction.getStepZ();
		}

		this.setMoveDirection(direction);
		double h = e - this.getX();
		double j = f - this.getY();
		double k = g - this.getZ();
		double l = (double)Mth.sqrt(h * h + j * j + k * k);
		if (l == 0.0) {
			this.targetDeltaX = 0.0;
			this.targetDeltaY = 0.0;
			this.targetDeltaZ = 0.0;
		} else {
			this.targetDeltaX = h / l * 0.15;
			this.targetDeltaY = j / l * 0.15;
			this.targetDeltaZ = k / l * 0.15;
		}

		this.hasImpulse = true;
		this.flightSteps = 10 + this.random.nextInt(5) * 10;
	}

	@Override
	public void checkDespawn() {
		if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
			this.remove();
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level.isClientSide) {
			if (this.finalTarget == null && this.targetId != null) {
				for (LivingEntity livingEntity : this.level
					.getEntitiesOfClass(LivingEntity.class, new AABB(this.lastKnownTargetPos.offset(-2, -2, -2), this.lastKnownTargetPos.offset(2, 2, 2)))) {
					if (livingEntity.getUUID().equals(this.targetId)) {
						this.finalTarget = livingEntity;
						break;
					}
				}

				this.targetId = null;
			}

			if (this.owner == null && this.ownerId != null) {
				for (LivingEntity livingEntityx : this.level
					.getEntitiesOfClass(LivingEntity.class, new AABB(this.lastKnownOwnerPos.offset(-2, -2, -2), this.lastKnownOwnerPos.offset(2, 2, 2)))) {
					if (livingEntityx.getUUID().equals(this.ownerId)) {
						this.owner = livingEntityx;
						break;
					}
				}

				this.ownerId = null;
			}

			if (this.finalTarget == null || !this.finalTarget.isAlive() || this.finalTarget instanceof Player && ((Player)this.finalTarget).isSpectator()) {
				if (!this.isNoGravity()) {
					this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
				}
			} else {
				this.targetDeltaX = Mth.clamp(this.targetDeltaX * 1.025, -1.0, 1.0);
				this.targetDeltaY = Mth.clamp(this.targetDeltaY * 1.025, -1.0, 1.0);
				this.targetDeltaZ = Mth.clamp(this.targetDeltaZ * 1.025, -1.0, 1.0);
				Vec3 vec3 = this.getDeltaMovement();
				this.setDeltaMovement(vec3.add((this.targetDeltaX - vec3.x) * 0.2, (this.targetDeltaY - vec3.y) * 0.2, (this.targetDeltaZ - vec3.z) * 0.2));
			}

			HitResult hitResult = ProjectileUtil.forwardsRaycast(this, true, false, this.owner, ClipContext.Block.COLLIDER);
			if (hitResult.getType() != HitResult.Type.MISS) {
				this.onHit(hitResult);
			}
		}

		Vec3 vec3 = this.getDeltaMovement();
		this.setPos(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
		ProjectileUtil.rotateTowardsMovement(this, 0.5F);
		if (this.level.isClientSide) {
			this.level.addParticle(ParticleTypes.END_ROD, this.getX() - vec3.x, this.getY() - vec3.y + 0.15, this.getZ() - vec3.z, 0.0, 0.0, 0.0);
		} else if (this.finalTarget != null && !this.finalTarget.removed) {
			if (this.flightSteps > 0) {
				this.flightSteps--;
				if (this.flightSteps == 0) {
					this.selectNextMoveDirection(this.currentMoveDirection == null ? null : this.currentMoveDirection.getAxis());
				}
			}

			if (this.currentMoveDirection != null) {
				BlockPos blockPos = new BlockPos(this);
				Direction.Axis axis = this.currentMoveDirection.getAxis();
				if (this.level.loadedAndEntityCanStandOn(blockPos.relative(this.currentMoveDirection), this)) {
					this.selectNextMoveDirection(axis);
				} else {
					BlockPos blockPos2 = new BlockPos(this.finalTarget);
					if (axis == Direction.Axis.X && blockPos.getX() == blockPos2.getX()
						|| axis == Direction.Axis.Z && blockPos.getZ() == blockPos2.getZ()
						|| axis == Direction.Axis.Y && blockPos.getY() == blockPos2.getY()) {
						this.selectNextMoveDirection(axis);
					}
				}
			}
		}
	}

	@Override
	public boolean isOnFire() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		return d < 16384.0;
	}

	@Override
	public float getBrightness() {
		return 1.0F;
	}

	protected void onHit(HitResult hitResult) {
		if (hitResult.getType() == HitResult.Type.ENTITY) {
			Entity entity = ((EntityHitResult)hitResult).getEntity();
			boolean bl = entity.hurt(DamageSource.indirectMobAttack(this, this.owner).setProjectile(), 4.0F);
			if (bl) {
				this.doEnchantDamageEffects(this.owner, entity);
				if (entity instanceof LivingEntity) {
					((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200));
				}
			}
		} else {
			((ServerLevel)this.level).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
			this.playSound(SoundEvents.SHULKER_BULLET_HIT, 1.0F, 1.0F);
		}

		this.remove();
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (!this.level.isClientSide) {
			this.playSound(SoundEvents.SHULKER_BULLET_HURT, 1.0F, 1.0F);
			((ServerLevel)this.level).sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 15, 0.2, 0.2, 0.2, 0.0);
			this.remove();
		}

		return true;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}
}
