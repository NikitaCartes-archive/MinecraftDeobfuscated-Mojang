package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;

public class Slime extends Mob implements Enemy {
	private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(Slime.class, EntityDataSerializers.INT);
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
			.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, 10, true, false, livingEntity -> Math.abs(livingEntity.y - this.y) <= 4.0));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ID_SIZE, 1);
	}

	protected void setSize(int i, boolean bl) {
		this.entityData.set(ID_SIZE, i);
		this.setPos(this.x, this.y, this.z);
		this.refreshDimensions();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)(i * i));
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)(0.2F + 0.1F * (float)i));
		this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)i);
		if (bl) {
			this.setHealth(this.getMaxHealth());
		}

		this.xpReward = i;
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
		super.readAdditionalSaveData(compoundTag);
		int i = compoundTag.getInt("Size");
		if (i < 0) {
			i = 0;
		}

		this.setSize(i + 1, false);
		this.wasOnGround = compoundTag.getBoolean("wasOnGround");
	}

	public boolean isTiny() {
		return this.getSize() <= 1;
	}

	protected ParticleOptions getParticleType() {
		return ParticleTypes.ITEM_SLIME;
	}

	@Override
	public void tick() {
		if (!this.level.isClientSide && this.level.getDifficulty() == Difficulty.PEACEFUL && this.getSize() > 0) {
			this.removed = true;
		}

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
				Level var10000 = this.level;
				ParticleOptions var10001 = this.getParticleType();
				double var10002 = this.x + (double)h;
				double var10004 = this.z + (double)k;
				var10000.addParticle(var10001, var10002, this.getBoundingBox().minY, var10004, 0.0, 0.0, 0.0);
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
		double d = this.x;
		double e = this.y;
		double f = this.z;
		super.refreshDimensions();
		this.setPos(d, e, f);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (ID_SIZE.equals(entityDataAccessor)) {
			this.refreshDimensions();
			this.yRot = this.yHeadRot;
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
	public void remove() {
		int i = this.getSize();
		if (!this.level.isClientSide && i > 1 && this.getHealth() <= 0.0F) {
			int j = 2 + this.random.nextInt(3);

			for (int k = 0; k < j; k++) {
				float f = ((float)(k % 2) - 0.5F) * (float)i / 4.0F;
				float g = ((float)(k / 2) - 0.5F) * (float)i / 4.0F;
				Slime slime = this.getType().create(this.level);
				if (this.hasCustomName()) {
					slime.setCustomName(this.getCustomName());
				}

				if (this.isPersistenceRequired()) {
					slime.setPersistenceRequired();
				}

				slime.setSize(i / 2, true);
				slime.moveTo(this.x + (double)f, this.y + 0.5, this.z + (double)g, this.random.nextFloat() * 360.0F, 0.0F);
				this.level.addFreshEntity(slime);
			}
		}

		super.remove();
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
				&& this.canSee(livingEntity)
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
		return (float)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
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

	@Override
	protected ResourceLocation getDefaultLootTable() {
		return this.getSize() == 1 ? this.getType().getDefaultLootTable() : BuiltInLootTables.EMPTY;
	}

	public static boolean checkSlimeSpawnRules(
		EntityType<Slime> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		if (levelAccessor.getLevelData().getGeneratorType() == LevelType.FLAT && random.nextInt(4) != 1) {
			return false;
		} else {
			if (levelAccessor.getDifficulty() != Difficulty.PEACEFUL) {
				Biome biome = levelAccessor.getBiome(blockPos);
				if (biome == Biomes.SWAMP
					&& blockPos.getY() > 50
					&& blockPos.getY() < 70
					&& random.nextFloat() < 0.5F
					&& random.nextFloat() < levelAccessor.getMoonBrightness()
					&& levelAccessor.getMaxLocalRawBrightness(blockPos) <= random.nextInt(8)) {
					return checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, random);
				}

				ChunkPos chunkPos = new ChunkPos(blockPos);
				boolean bl = WorldgenRandom.seedSlimeChunk(chunkPos.x, chunkPos.z, levelAccessor.getSeed(), 987234911L).nextInt(10) == 0;
				if (random.nextInt(10) == 0 && bl && blockPos.getY() < 40) {
					return checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, random);
				}
			}

			return false;
		}
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
		this.setDeltaMovement(vec3.x, 0.42F, vec3.z);
		this.hasImpulse = true;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		LevelAccessor levelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		int i = this.random.nextInt(3);
		if (i < 2 && this.random.nextFloat() < 0.5F * difficultyInstance.getSpecialMultiplier()) {
			i++;
		}

		int j = 1 << i;
		this.setSize(j, true);
		return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
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
			} else if (!livingEntity.isAlive()) {
				return false;
			} else {
				return livingEntity instanceof Player && ((Player)livingEntity).abilities.invulnerable
					? false
					: this.slime.getMoveControl() instanceof Slime.SlimeMoveControl;
			}
		}

		@Override
		public void start() {
			this.growTiredTimer = 300;
			super.start();
		}

		@Override
		public boolean canContinueToUse() {
			LivingEntity livingEntity = this.slime.getTarget();
			if (livingEntity == null) {
				return false;
			} else if (!livingEntity.isAlive()) {
				return false;
			} else {
				return livingEntity instanceof Player && ((Player)livingEntity).abilities.invulnerable ? false : --this.growTiredTimer > 0;
			}
		}

		@Override
		public void tick() {
			this.slime.lookAt(this.slime.getTarget(), 10.0F, 10.0F);
			((Slime.SlimeMoveControl)this.slime.getMoveControl()).setDirection(this.slime.yRot, this.slime.isDealsDamage());
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
			this.yRot = 180.0F * slime.yRot / (float) Math.PI;
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
			this.mob.yRot = this.rotlerp(this.mob.yRot, this.yRot, 90.0F);
			this.mob.yHeadRot = this.mob.yRot;
			this.mob.yBodyRot = this.mob.yRot;
			if (this.operation != MoveControl.Operation.MOVE_TO) {
				this.mob.setZza(0.0F);
			} else {
				this.operation = MoveControl.Operation.WAIT;
				if (this.mob.onGround) {
					this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue()));
					if (this.jumpDelay-- <= 0) {
						this.jumpDelay = this.slime.getJumpDelay();
						if (this.isAggressive) {
							this.jumpDelay /= 3;
						}

						this.slime.getJumpControl().jump();
						if (this.slime.doPlayJumpSound()) {
							this.slime
								.playSound(
									this.slime.getJumpSound(),
									this.slime.getSoundVolume(),
									((this.slime.getRandom().nextFloat() - this.slime.getRandom().nextFloat()) * 0.2F + 1.0F) * 0.8F
								);
						}
					} else {
						this.slime.xxa = 0.0F;
						this.slime.zza = 0.0F;
						this.mob.setSpeed(0.0F);
					}
				} else {
					this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue()));
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
				this.nextRandomizeTime = 40 + this.slime.getRandom().nextInt(60);
				this.chosenDegrees = (float)this.slime.getRandom().nextInt(360);
			}

			((Slime.SlimeMoveControl)this.slime.getMoveControl()).setDirection(this.chosenDegrees, false);
		}
	}
}
