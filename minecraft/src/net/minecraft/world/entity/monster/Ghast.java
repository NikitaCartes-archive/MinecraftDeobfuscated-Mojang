package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Ghast extends FlyingMob implements Enemy {
	private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(Ghast.class, EntityDataSerializers.BOOLEAN);
	private int explosionPower = 1;

	public Ghast(EntityType<? extends Ghast> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 5;
		this.moveControl = new Ghast.GhastMoveControl(this);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(5, new Ghast.RandomFloatAroundGoal(this));
		this.goalSelector.addGoal(7, new Ghast.GhastLookGoal(this));
		this.goalSelector.addGoal(7, new Ghast.GhastShootFireballGoal(this));
		this.targetSelector
			.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, 10, true, false, livingEntity -> Math.abs(livingEntity.getY() - this.getY()) <= 4.0));
	}

	public boolean isCharging() {
		return this.entityData.get(DATA_IS_CHARGING);
	}

	public void setCharging(boolean bl) {
		this.entityData.set(DATA_IS_CHARGING, bl);
	}

	public int getExplosionPower() {
		return this.explosionPower;
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return true;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else if (damageSource.getDirectEntity() instanceof LargeFireball && damageSource.getEntity() instanceof Player) {
			super.hurt(damageSource, 1000.0F);
			return true;
		} else {
			return super.hurt(damageSource, f);
		}
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_IS_CHARGING, false);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.FOLLOW_RANGE, 100.0);
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.HOSTILE;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.GHAST_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.GHAST_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.GHAST_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 5.0F;
	}

	public static boolean checkGhastSpawnRules(
		EntityType<Ghast> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		return levelAccessor.getDifficulty() != Difficulty.PEACEFUL
			&& random.nextInt(20) == 0
			&& checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, random);
	}

	@Override
	public int getMaxSpawnClusterSize() {
		return 1;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putByte("ExplosionPower", (byte)this.explosionPower);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("ExplosionPower", 99)) {
			this.explosionPower = compoundTag.getByte("ExplosionPower");
		}
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return 2.6F;
	}

	static class GhastLookGoal extends Goal {
		private final Ghast ghast;

		public GhastLookGoal(Ghast ghast) {
			this.ghast = ghast;
			this.setFlags(EnumSet.of(Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return true;
		}

		@Override
		public void tick() {
			if (this.ghast.getTarget() == null) {
				Vec3 vec3 = this.ghast.getDeltaMovement();
				this.ghast.setYRot(-((float)Mth.atan2(vec3.x, vec3.z)) * (180.0F / (float)Math.PI));
				this.ghast.yBodyRot = this.ghast.getYRot();
			} else {
				LivingEntity livingEntity = this.ghast.getTarget();
				double d = 64.0;
				if (livingEntity.distanceToSqr(this.ghast) < 4096.0) {
					double e = livingEntity.getX() - this.ghast.getX();
					double f = livingEntity.getZ() - this.ghast.getZ();
					this.ghast.setYRot(-((float)Mth.atan2(e, f)) * (180.0F / (float)Math.PI));
					this.ghast.yBodyRot = this.ghast.getYRot();
				}
			}
		}
	}

	static class GhastMoveControl extends MoveControl {
		private final Ghast ghast;
		private int floatDuration;

		public GhastMoveControl(Ghast ghast) {
			super(ghast);
			this.ghast = ghast;
		}

		@Override
		public void tick() {
			if (this.operation == MoveControl.Operation.MOVE_TO) {
				if (this.floatDuration-- <= 0) {
					this.floatDuration = this.floatDuration + this.ghast.getRandom().nextInt(5) + 2;
					Vec3 vec3 = new Vec3(this.wantedX - this.ghast.getX(), this.wantedY - this.ghast.getY(), this.wantedZ - this.ghast.getZ());
					double d = vec3.length();
					vec3 = vec3.normalize();
					if (this.canReach(vec3, Mth.ceil(d))) {
						this.ghast.setDeltaMovement(this.ghast.getDeltaMovement().add(vec3.scale(0.1)));
					} else {
						this.operation = MoveControl.Operation.WAIT;
					}
				}
			}
		}

		private boolean canReach(Vec3 vec3, int i) {
			AABB aABB = this.ghast.getBoundingBox();

			for (int j = 1; j < i; j++) {
				aABB = aABB.move(vec3);
				if (!this.ghast.level.noCollision(this.ghast, aABB)) {
					return false;
				}
			}

			return true;
		}
	}

	static class GhastShootFireballGoal extends Goal {
		private final Ghast ghast;
		public int chargeTime;

		public GhastShootFireballGoal(Ghast ghast) {
			this.ghast = ghast;
		}

		@Override
		public boolean canUse() {
			return this.ghast.getTarget() != null;
		}

		@Override
		public void start() {
			this.chargeTime = 0;
		}

		@Override
		public void stop() {
			this.ghast.setCharging(false);
		}

		@Override
		public void tick() {
			LivingEntity livingEntity = this.ghast.getTarget();
			double d = 64.0;
			if (livingEntity.distanceToSqr(this.ghast) < 4096.0 && this.ghast.hasLineOfSight(livingEntity)) {
				Level level = this.ghast.level;
				this.chargeTime++;
				if (this.chargeTime == 10 && !this.ghast.isSilent()) {
					level.levelEvent(null, 1015, this.ghast.blockPosition(), 0);
				}

				if (this.chargeTime == 20) {
					double e = 4.0;
					Vec3 vec3 = this.ghast.getViewVector(1.0F);
					double f = livingEntity.getX() - (this.ghast.getX() + vec3.x * 4.0);
					double g = livingEntity.getY(0.5) - (0.5 + this.ghast.getY(0.5));
					double h = livingEntity.getZ() - (this.ghast.getZ() + vec3.z * 4.0);
					if (!this.ghast.isSilent()) {
						level.levelEvent(null, 1016, this.ghast.blockPosition(), 0);
					}

					LargeFireball largeFireball = new LargeFireball(level, this.ghast, f, g, h, this.ghast.getExplosionPower());
					largeFireball.setPos(this.ghast.getX() + vec3.x * 4.0, this.ghast.getY(0.5) + 0.5, largeFireball.getZ() + vec3.z * 4.0);
					level.addFreshEntity(largeFireball);
					this.chargeTime = -40;
				}
			} else if (this.chargeTime > 0) {
				this.chargeTime--;
			}

			this.ghast.setCharging(this.chargeTime > 10);
		}
	}

	static class RandomFloatAroundGoal extends Goal {
		private final Ghast ghast;

		public RandomFloatAroundGoal(Ghast ghast) {
			this.ghast = ghast;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			MoveControl moveControl = this.ghast.getMoveControl();
			if (!moveControl.hasWanted()) {
				return true;
			} else {
				double d = moveControl.getWantedX() - this.ghast.getX();
				double e = moveControl.getWantedY() - this.ghast.getY();
				double f = moveControl.getWantedZ() - this.ghast.getZ();
				double g = d * d + e * e + f * f;
				return g < 1.0 || g > 3600.0;
			}
		}

		@Override
		public boolean canContinueToUse() {
			return false;
		}

		@Override
		public void start() {
			Random random = this.ghast.getRandom();
			double d = this.ghast.getX() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
			double e = this.ghast.getY() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
			double f = this.ghast.getZ() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
			this.ghast.getMoveControl().setWantedPosition(d, e, f, 1.0);
		}
	}
}
