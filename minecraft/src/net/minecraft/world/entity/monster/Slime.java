package net.minecraft.world.entity.monster;

import com.google.common.annotations.VisibleForTesting;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.phys.Vec3;

public class Slime extends Mob implements Enemy {
	private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(Slime.class, EntityDataSerializers.INT);
	public static final int MIN_SIZE = 1;
	public static final int MAX_SIZE = 127;
	public float targetSquish;
	public float squish;
	public float oSquish;
	private boolean wasOnGround;

	public Slime(EntityType<? extends Slime> entityType, Level level) {
		super(entityType, level);
		this.moveControl = new Slime.SlimeMoveControl(this);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new Slime.SlimeFloatGoal(this));
		this.goalSelector.addGoal(2, new Slime.SlimeAttackGoal(this));
		this.goalSelector.addGoal(3, new Slime.SlimeRandomDirectionGoal(this));
		this.goalSelector.addGoal(5, new Slime.SlimeKeepOnJumpingGoal(this));
		this.targetSelector
			.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, 10, true, false, livingEntity -> Math.abs(livingEntity.getY() - this.getY()) <= 4.0));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ID_SIZE, 1);
	}

	@VisibleForTesting
	public void setSize(int i, boolean bl) {
		int j = Mth.clamp(i, 1, 127);
		this.entityData.set(ID_SIZE, j);
		this.reapplyPosition();
		this.refreshDimensions();
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)(j * j));
		this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double)(0.2F + 0.1F * (float)j));
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double)j);
		if (bl) {
			this.setHealth(this.getMaxHealth());
		}

		this.xpReward = j;
	}

	public int getSize() {
		return this.entityData.get(ID_SIZE);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Size", this.getSize() - 1);
		compoundTag.putBoolean("wasOnGround", this.wasOnGround);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		this.setSize(compoundTag.getInt("Size") + 1, false);
		super.readAdditionalSaveData(compoundTag);
		this.wasOnGround = compoundTag.getBoolean("wasOnGround");
	}

	public boolean isTiny() {
		return this.getSize() <= 1;
	}

	protected ParticleOptions getParticleType() {
		return ParticleTypes.ITEM_SLIME;
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return this.getSize() > 0;
	}

	@Override
	public void tick() {
		this.squish = this.squish + (this.targetSquish - this.squish) * 0.5F;
		this.oSquish = this.squish;
		super.tick();
		if (this.onGround && !this.wasOnGround) {
			int i = this.getSize();

			for (int j = 0; j < i * 8; j++) {
				float f = this.random.nextFloat() * (float) (Math.PI * 2);
				float g = this.random.nextFloat() * 0.5F + 0.5F;
				float h = Mth.sin(f) * (float)i * 0.5F * g;
				float k = Mth.cos(f) * (float)i * 0.5F * g;
				this.level.addParticle(this.getParticleType(), this.getX() + (double)h, this.getY(), this.getZ() + (double)k, 0.0, 0.0, 0.0);
			}

			this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) / 0.8F);
			this.targetSquish = -0.5F;
		} else if (!this.onGround && this.wasOnGround) {
			this.targetSquish = 1.0F;
		}

		this.wasOnGround = this.onGround;
		this.decreaseSquish();
	}

	protected void decreaseSquish() {
		this.targetSquish *= 0.6F;
	}

	protected int getJumpDelay() {
		return this.random.nextInt(20) + 10;
	}

	@Override
	public void refreshDimensions() {
		double d = this.getX();
		double e = this.getY();
		double f = this.getZ();
		super.refreshDimensions();
		this.setPos(d, e, f);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (ID_SIZE.equals(entityDataAccessor)) {
			this.refreshDimensions();
			this.setYRot(this.yHeadRot);
			this.yBodyRot = this.yHeadRot;
			if (this.isInWater() && this.random.nextInt(20) == 0) {
				this.doWaterSplashEffect();
			}
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	@Override
	public EntityType<? extends Slime> getType() {
		return (EntityType<? extends Slime>)super.getType();
	}

	@Override
	public void remove(Entity.RemovalReason removalReason) {
		int i = this.getSize();
		if (!this.level.isClientSide && i > 1 && this.isDeadOrDying()) {
			Component component = this.getCustomName();
			boolean bl = this.isNoAi();
			float f = (float)i / 4.0F;
			int j = i / 2;
			int k = 2 + this.random.nextInt(3);

			for (int l = 0; l < k; l++) {
				float g = ((float)(l % 2) - 0.5F) * f;
				float h = ((float)(l / 2) - 0.5F) * f;
				Slime slime = this.getType().create(this.level);
				if (slime != null) {
					if (this.isPersistenceRequired()) {
						slime.setPersistenceRequired();
					}

					slime.setCustomName(component);
					slime.setNoAi(bl);
					slime.setInvulnerable(this.isInvulnerable());
					slime.setSize(j, true);
					slime.moveTo(this.getX() + (double)g, this.getY() + 0.5, this.getZ() + (double)h, this.random.nextFloat() * 360.0F, 0.0F);
					this.level.addFreshEntity(slime);
				}
			}
		}

		super.remove(removalReason);
	}

	@Override
	public void push(Entity entity) {
		super.push(entity);
		if (entity instanceof IronGolem && this.isDealsDamage()) {
			this.dealDamage((LivingEntity)entity);
		}
	}

	@Override
	public void playerTouch(Player player) {
		if (this.isDealsDamage()) {
			this.dealDamage(player);
		}
	}

	protected void dealDamage(LivingEntity livingEntity) {
		if (this.isAlive()) {
			int i = this.getSize();
			if (this.distanceToSqr(livingEntity) < 0.6 * (double)i * 0.6 * (double)i
				&& this.hasLineOfSight(livingEntity)
				&& livingEntity.hurt(DamageSource.mobAttack(this), this.getAttackDamage())) {
				this.playSound(SoundEvents.SLIME_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
				this.doEnchantDamageEffects(this, livingEntity);
			}
		}
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return 0.625F * entityDimensions.height;
	}

	protected boolean isDealsDamage() {
		return !this.isTiny() && this.isEffectiveAi();
	}

	protected float getAttackDamage() {
		return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return this.isTiny() ? SoundEvents.SLIME_HURT_SMALL : SoundEvents.SLIME_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return this.isTiny() ? SoundEvents.SLIME_DEATH_SMALL : SoundEvents.SLIME_DEATH;
	}

	protected SoundEvent getSquishSound() {
		return this.isTiny() ? SoundEvents.SLIME_SQUISH_SMALL : SoundEvents.SLIME_SQUISH;
	}

	public static boolean checkSlimeSpawnRules(
		EntityType<Slime> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		if (levelAccessor.getDifficulty() != Difficulty.PEACEFUL) {
			if (levelAccessor.getBiome(blockPos).is(BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS)
				&& blockPos.getY() > 50
				&& blockPos.getY() < 70
				&& randomSource.nextFloat() < 0.5F
				&& randomSource.nextFloat() < levelAccessor.getMoonBrightness()
				&& levelAccessor.getMaxLocalRawBrightness(blockPos) <= randomSource.nextInt(8)) {
				return checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, randomSource);
			}

			if (!(levelAccessor instanceof WorldGenLevel)) {
				return false;
			}

			ChunkPos chunkPos = new ChunkPos(blockPos);
			boolean bl = WorldgenRandom.seedSlimeChunk(chunkPos.x, chunkPos.z, ((WorldGenLevel)levelAccessor).getSeed(), 987234911L).nextInt(10) == 0;
			if (randomSource.nextInt(10) == 0 && bl && blockPos.getY() < 40) {
				return checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, randomSource);
			}
		}

		return false;
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F * (float)this.getSize();
	}

	@Override
	public int getMaxHeadXRot() {
		return 0;
	}

	protected boolean doPlayJumpSound() {
		return this.getSize() > 0;
	}

	@Override
	protected void jumpFromGround() {
		Vec3 vec3 = this.getDeltaMovement();
		this.setDeltaMovement(vec3.x, (double)this.getJumpPower(), vec3.z);
		this.hasImpulse = true;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		RandomSource randomSource = serverLevelAccessor.getRandom();
		int i = randomSource.nextInt(3);
		if (i < 2 && randomSource.nextFloat() < 0.5F * difficultyInstance.getSpecialMultiplier()) {
			i++;
		}

		int j = 1 << i;
		this.setSize(j, true);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	float getSoundPitch() {
		float f = this.isTiny() ? 1.4F : 0.8F;
		return ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * f;
	}

	protected SoundEvent getJumpSound() {
		return this.isTiny() ? SoundEvents.SLIME_JUMP_SMALL : SoundEvents.SLIME_JUMP;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return super.getDimensions(pose).scale(0.255F * (float)this.getSize());
	}

	static class SlimeAttackGoal extends Goal {
		private final Slime slime;
		private int growTiredTimer;

		public SlimeAttackGoal(Slime slime) {
			this.slime = slime;
			this.setFlags(EnumSet.of(Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			LivingEntity livingEntity = this.slime.getTarget();
			if (livingEntity == null) {
				return false;
			} else {
				return !this.slime.canAttack(livingEntity) ? false : this.slime.getMoveControl() instanceof Slime.SlimeMoveControl;
			}
		}

		@Override
		public void start() {
			this.growTiredTimer = reducedTickDelay(300);
			super.start();
		}

		@Override
		public boolean canContinueToUse() {
			LivingEntity livingEntity = this.slime.getTarget();
			if (livingEntity == null) {
				return false;
			} else {
				return !this.slime.canAttack(livingEntity) ? false : --this.growTiredTimer > 0;
			}
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			LivingEntity livingEntity = this.slime.getTarget();
			if (livingEntity != null) {
				this.slime.lookAt(livingEntity, 10.0F, 10.0F);
			}

			((Slime.SlimeMoveControl)this.slime.getMoveControl()).setDirection(this.slime.getYRot(), this.slime.isDealsDamage());
		}
	}

	static class SlimeFloatGoal extends Goal {
		private final Slime slime;

		public SlimeFloatGoal(Slime slime) {
			this.slime = slime;
			this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
			slime.getNavigation().setCanFloat(true);
		}

		@Override
		public boolean canUse() {
			return (this.slime.isInWater() || this.slime.isInLava()) && this.slime.getMoveControl() instanceof Slime.SlimeMoveControl;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			if (this.slime.getRandom().nextFloat() < 0.8F) {
				this.slime.getJumpControl().jump();
			}

			((Slime.SlimeMoveControl)this.slime.getMoveControl()).setWantedMovement(1.2);
		}
	}

	static class SlimeKeepOnJumpingGoal extends Goal {
		private final Slime slime;

		public SlimeKeepOnJumpingGoal(Slime slime) {
			this.slime = slime;
			this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			return !this.slime.isPassenger();
		}

		@Override
		public void tick() {
			((Slime.SlimeMoveControl)this.slime.getMoveControl()).setWantedMovement(1.0);
		}
	}

	static class SlimeMoveControl extends MoveControl {
		private float yRot;
		private int jumpDelay;
		private final Slime slime;
		private boolean isAggressive;

		public SlimeMoveControl(Slime slime) {
			super(slime);
			this.slime = slime;
			this.yRot = 180.0F * slime.getYRot() / (float) Math.PI;
		}

		public void setDirection(float f, boolean bl) {
			this.yRot = f;
			this.isAggressive = bl;
		}

		public void setWantedMovement(double d) {
			this.speedModifier = d;
			this.operation = MoveControl.Operation.MOVE_TO;
		}

		@Override
		public void tick() {
			this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.yRot, 90.0F));
			this.mob.yHeadRot = this.mob.getYRot();
			this.mob.yBodyRot = this.mob.getYRot();
			if (this.operation != MoveControl.Operation.MOVE_TO) {
				this.mob.setZza(0.0F);
			} else {
				this.operation = MoveControl.Operation.WAIT;
				if (this.mob.isOnGround()) {
					this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
					if (this.jumpDelay-- <= 0) {
						this.jumpDelay = this.slime.getJumpDelay();
						if (this.isAggressive) {
							this.jumpDelay /= 3;
						}

						this.slime.getJumpControl().jump();
						if (this.slime.doPlayJumpSound()) {
							this.slime.playSound(this.slime.getJumpSound(), this.slime.getSoundVolume(), this.slime.getSoundPitch());
						}
					} else {
						this.slime.xxa = 0.0F;
						this.slime.zza = 0.0F;
						this.mob.setSpeed(0.0F);
					}
				} else {
					this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
				}
			}
		}
	}

	static class SlimeRandomDirectionGoal extends Goal {
		private final Slime slime;
		private float chosenDegrees;
		private int nextRandomizeTime;

		public SlimeRandomDirectionGoal(Slime slime) {
			this.slime = slime;
			this.setFlags(EnumSet.of(Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return this.slime.getTarget() == null
				&& (this.slime.onGround || this.slime.isInWater() || this.slime.isInLava() || this.slime.hasEffect(MobEffects.LEVITATION))
				&& this.slime.getMoveControl() instanceof Slime.SlimeMoveControl;
		}

		@Override
		public void tick() {
			if (--this.nextRandomizeTime <= 0) {
				this.nextRandomizeTime = this.adjustedTickDelay(40 + this.slime.getRandom().nextInt(60));
				this.chosenDegrees = (float)this.slime.getRandom().nextInt(360);
			}

			((Slime.SlimeMoveControl)this.slime.getMoveControl()).setDirection(this.chosenDegrees, false);
		}
	}
}
