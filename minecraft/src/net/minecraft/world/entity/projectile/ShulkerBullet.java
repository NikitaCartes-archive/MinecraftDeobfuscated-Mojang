package net.minecraft.world.entity.projectile;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ShulkerBullet extends Projectile {
	private static final double SPEED = 0.15;
	@Nullable
	private Entity finalTarget;
	@Nullable
	private Direction currentMoveDirection;
	private int flightSteps;
	private double targetDeltaX;
	private double targetDeltaY;
	private double targetDeltaZ;
	@Nullable
	private UUID targetId;

	public ShulkerBullet(EntityType<? extends ShulkerBullet> entityType, Level level) {
		super(entityType, level);
		this.noPhysics = true;
	}

	public ShulkerBullet(Level level, LivingEntity livingEntity, Entity entity, Direction.Axis axis) {
		this(EntityType.SHULKER_BULLET, level);
		this.setOwner(livingEntity);
		Vec3 vec3 = livingEntity.getBoundingBox().getCenter();
		this.moveTo(vec3.x, vec3.y, vec3.z, this.getYRot(), this.getXRot());
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
		super.addAdditionalSaveData(compoundTag);
		if (this.finalTarget != null) {
			compoundTag.putUUID("Target", this.finalTarget.getUUID());
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
		super.readAdditionalSaveData(compoundTag);
		this.flightSteps = compoundTag.getInt("Steps");
		this.targetDeltaX = compoundTag.getDouble("TXD");
		this.targetDeltaY = compoundTag.getDouble("TYD");
		this.targetDeltaZ = compoundTag.getDouble("TZD");
		if (compoundTag.contains("Dir", 99)) {
			this.currentMoveDirection = Direction.from3DDataValue(compoundTag.getInt("Dir"));
		}

		if (compoundTag.hasUUID("Target")) {
			this.targetId = compoundTag.getUUID("Target");
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Nullable
	private Direction getMoveDirection() {
		return this.currentMoveDirection;
	}

	private void setMoveDirection(@Nullable Direction direction) {
		this.currentMoveDirection = direction;
	}

	private void selectNextMoveDirection(@Nullable Direction.Axis axis) {
		double d = 0.5;
		BlockPos blockPos;
		if (this.finalTarget == null) {
			blockPos = this.blockPosition().below();
		} else {
			d = (double)this.finalTarget.getBbHeight() * 0.5;
			blockPos = BlockPos.containing(this.finalTarget.getX(), this.finalTarget.getY() + d, this.finalTarget.getZ());
		}

		double e = (double)blockPos.getX() + 0.5;
		double f = (double)blockPos.getY() + d;
		double g = (double)blockPos.getZ() + 0.5;
		Direction direction = null;
		if (!blockPos.closerToCenterThan(this.position(), 2.0)) {
			BlockPos blockPos2 = this.blockPosition();
			List<Direction> list = Lists.<Direction>newArrayList();
			if (axis != Direction.Axis.X) {
				if (blockPos2.getX() < blockPos.getX() && this.level().isEmptyBlock(blockPos2.east())) {
					list.add(Direction.EAST);
				} else if (blockPos2.getX() > blockPos.getX() && this.level().isEmptyBlock(blockPos2.west())) {
					list.add(Direction.WEST);
				}
			}

			if (axis != Direction.Axis.Y) {
				if (blockPos2.getY() < blockPos.getY() && this.level().isEmptyBlock(blockPos2.above())) {
					list.add(Direction.UP);
				} else if (blockPos2.getY() > blockPos.getY() && this.level().isEmptyBlock(blockPos2.below())) {
					list.add(Direction.DOWN);
				}
			}

			if (axis != Direction.Axis.Z) {
				if (blockPos2.getZ() < blockPos.getZ() && this.level().isEmptyBlock(blockPos2.south())) {
					list.add(Direction.SOUTH);
				} else if (blockPos2.getZ() > blockPos.getZ() && this.level().isEmptyBlock(blockPos2.north())) {
					list.add(Direction.NORTH);
				}
			}

			direction = Direction.getRandom(this.random);
			if (list.isEmpty()) {
				for (int i = 5; !this.level().isEmptyBlock(blockPos2.relative(direction)) && i > 0; i--) {
					direction = Direction.getRandom(this.random);
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
		double l = Math.sqrt(h * h + j * j + k * k);
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
		if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
			this.discard();
		}
	}

	@Override
	protected double getDefaultGravity() {
		return 0.04;
	}

	@Override
	public void tick() {
		super.tick();
		HitResult hitResult = null;
		if (!this.level().isClientSide) {
			if (this.finalTarget == null && this.targetId != null) {
				this.finalTarget = ((ServerLevel)this.level()).getEntity(this.targetId);
				if (this.finalTarget == null) {
					this.targetId = null;
				}
			}

			if (this.finalTarget == null || !this.finalTarget.isAlive() || this.finalTarget instanceof Player && this.finalTarget.isSpectator()) {
				this.applyGravity();
			} else {
				this.targetDeltaX = Mth.clamp(this.targetDeltaX * 1.025, -1.0, 1.0);
				this.targetDeltaY = Mth.clamp(this.targetDeltaY * 1.025, -1.0, 1.0);
				this.targetDeltaZ = Mth.clamp(this.targetDeltaZ * 1.025, -1.0, 1.0);
				Vec3 vec3 = this.getDeltaMovement();
				this.setDeltaMovement(vec3.add((this.targetDeltaX - vec3.x) * 0.2, (this.targetDeltaY - vec3.y) * 0.2, (this.targetDeltaZ - vec3.z) * 0.2));
			}

			hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
		}

		Vec3 vec3 = this.getDeltaMovement();
		this.setPos(this.position().add(vec3));
		this.applyEffectsFromBlocks();
		if (this.portalProcess != null && this.portalProcess.isInsidePortalThisTick()) {
			this.handlePortal();
		}

		if (hitResult != null && this.isAlive() && hitResult.getType() != HitResult.Type.MISS) {
			this.hitTargetOrDeflectSelf(hitResult);
		}

		ProjectileUtil.rotateTowardsMovement(this, 0.5F);
		if (this.level().isClientSide) {
			this.level().addParticle(ParticleTypes.END_ROD, this.getX() - vec3.x, this.getY() - vec3.y + 0.15, this.getZ() - vec3.z, 0.0, 0.0, 0.0);
		} else if (this.finalTarget != null && !this.finalTarget.isRemoved()) {
			if (this.flightSteps > 0) {
				this.flightSteps--;
				if (this.flightSteps == 0) {
					this.selectNextMoveDirection(this.currentMoveDirection == null ? null : this.currentMoveDirection.getAxis());
				}
			}

			if (this.currentMoveDirection != null) {
				BlockPos blockPos = this.blockPosition();
				Direction.Axis axis = this.currentMoveDirection.getAxis();
				if (this.level().loadedAndEntityCanStandOn(blockPos.relative(this.currentMoveDirection), this)) {
					this.selectNextMoveDirection(axis);
				} else {
					BlockPos blockPos2 = this.finalTarget.blockPosition();
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
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) && !entity.noPhysics;
	}

	@Override
	public boolean isOnFire() {
		return false;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		return d < 16384.0;
	}

	@Override
	public float getLightLevelDependentMagicValue() {
		return 1.0F;
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		Entity entity = entityHitResult.getEntity();
		Entity entity2 = this.getOwner();
		LivingEntity livingEntity = entity2 instanceof LivingEntity ? (LivingEntity)entity2 : null;
		DamageSource damageSource = this.damageSources().mobProjectile(this, livingEntity);
		boolean bl = entity.hurtOrSimulate(damageSource, 4.0F);
		if (bl) {
			if (this.level() instanceof ServerLevel serverLevel) {
				EnchantmentHelper.doPostAttackEffects(serverLevel, entity, damageSource);
			}

			if (entity instanceof LivingEntity livingEntity2) {
				livingEntity2.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200), MoreObjects.firstNonNull(entity2, this));
			}
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult blockHitResult) {
		super.onHitBlock(blockHitResult);
		((ServerLevel)this.level()).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
		this.playSound(SoundEvents.SHULKER_BULLET_HIT, 1.0F, 1.0F);
	}

	private void destroy() {
		this.discard();
		this.level().gameEvent(GameEvent.ENTITY_DAMAGE, this.position(), GameEvent.Context.of(this));
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		this.destroy();
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public boolean hurtClient(DamageSource damageSource) {
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
		this.playSound(SoundEvents.SHULKER_BULLET_HURT, 1.0F, 1.0F);
		serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 15, 0.2, 0.2, 0.2, 0.0);
		this.destroy();
		return true;
	}

	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
		super.recreateFromPacket(clientboundAddEntityPacket);
		double d = clientboundAddEntityPacket.getXa();
		double e = clientboundAddEntityPacket.getYa();
		double f = clientboundAddEntityPacket.getZa();
		this.setDeltaMovement(d, e, f);
	}
}
