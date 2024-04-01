package net.minecraft.world.entity.monster;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Containers;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;

public class MegaSpud extends PathfinderMob implements PowerableMob, Enemy {
	private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(MegaSpud.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Byte> DATA_SPELL_CASTING_ID = SynchedEntityData.defineId(MegaSpud.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Boolean> HAS_MINIONS = SynchedEntityData.defineId(MegaSpud.class, EntityDataSerializers.BOOLEAN);
	private static final int STARTING_HEALTH = 1024;
	public float targetSquish;
	public float squish;
	public float oSquish;
	private boolean wasOnGround;
	private MegaSpud.Stage currentStage;
	private final List<Mob> adds = new ArrayList();
	private final ServerBossEvent bossEvent;
	private final List<Runnable> minionCalls = new ArrayList();
	private static final ParticleOptions BREAKING_PARICLE = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.POISONOUS_POTATO));

	public MegaSpud(EntityType<? extends MegaSpud> entityType, Level level) {
		super(entityType, level);
		this.moveControl = new MegaSpud.SlimeMoveControl(this);
		this.currentStage = MegaSpud.Stage.CHICKEN;
		this.xpReward = 50;
		this.bossEvent = new ServerBossEvent(this, BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
		this.bossEvent.setDarkenScreen(false);
		this.fixupDimensions();
	}

	@Override
	public void checkDespawn() {
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 1024.0)
			.add(Attributes.FOLLOW_RANGE, 48.0)
			.add(Attributes.JUMP_STRENGTH, 0.62F)
			.add(Attributes.ARMOR, 5.0)
			.add(Attributes.MOVEMENT_SPEED, 0.2F);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(7, new MegaSpud.GhastShootFireballGoal(this));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(0, new FloatGoal(this) {
			@Override
			public void tick() {
				if (MegaSpud.this.getRandom().nextFloat() < 0.8F) {
					MegaSpud.this.jumpFromGround();
				}
			}
		});
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(3, new MegaSpud.SlimeRandomDirectionGoal(this));
		this.goalSelector.addGoal(5, new MegaSpud.SlimeKeepOnJumpingGoal(this));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, true).setUnseenMemoryTicks(300));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, false).setUnseenMemoryTicks(300));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, false));
	}

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		boolean bl = false;
		if (!this.adds.isEmpty()) {
			bl = true;
			this.adds.removeIf(mob -> mob.isRemoved() || mob.isDeadOrDying());
		}

		if (!this.minionCalls.isEmpty()) {
			bl = true;
			if (this.random.nextFloat() < 0.05F) {
				((Runnable)this.minionCalls.remove(0)).run();
				if (this.minionCalls.isEmpty()) {
					this.playSound(SoundEvents.MEGASPUD_CHALLENGE, this.getSoundVolume(), 1.0F);
				}
			}
		}

		if (this.adds.isEmpty() && this.minionCalls.isEmpty()) {
			this.setHasMinions(false);
			if (bl) {
				this.playSound(SoundEvents.MEGASPUD_UPSET, this.getSoundVolume(), 1.0F);
			}
		}

		if ((!this.hasRestriction() || this.getRestrictCenter().distToCenterSqr(this.position()) > 16384.0) && !this.blockPosition().equals(BlockPos.ZERO)) {
			this.restrictTo(this.blockPosition(), 3);
		}

		MegaSpud.Stage stage = this.currentStage.validStageBasedOnHealth(this.getHealth());
		if (this.currentStage != stage) {
			this.setHealth(this.currentStage.getHealth());
			AABB aABB = this.getBoundingBox();
			Vec3 vec3 = this.position();
			Containers.dropItemStack(this.level(), vec3.x, vec3.y, vec3.z, Items.CORRUPTED_POTATO_PEELS.getDefaultInstance());

			for (int i = 0; i < 100; i++) {
				Vec3 vec32 = vec3.add((double)this.random.nextFloat(-5.0F, 5.0F), (double)this.random.nextFloat(0.0F, 10.0F), (double)this.random.nextFloat(-5.0F, 5.0F));
				if (aABB.contains(vec32)) {
					Containers.dropItemStack(this.level(), vec32.x, vec32.y, vec32.z, Items.CORRUPTED_POTATO_PEELS.getDefaultInstance());
				}
			}

			ServerLevel serverLevel = (ServerLevel)this.level();
			PlayerTeam playerTeam = this.getTeam();
			this.summonMinion(this.currentStage, serverLevel, playerTeam);
			MegaSpud.Stage stage2 = this.currentStage;

			for (int j = 1; j <= this.currentStage.ordinal(); j++) {
				this.minionCalls.add((Runnable)() -> this.summonMinion(stage2, serverLevel, playerTeam));
			}

			this.currentStage = stage;
			this.setSize(this.currentStage.size);
			this.setHasMinions(true);
			this.bossEvent.setName(this.getDisplayName());
		}

		this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
		this.bossEvent.setLocation(this.position(), 64);
	}

	private void summonMinion(MegaSpud.Stage stage, ServerLevel serverLevel, PlayerTeam playerTeam) {
		BlockPos blockPos = this.getRestrictCenter()
			.offset(this.random.nextInt(5) - this.random.nextInt(5), this.random.nextInt(5), this.random.nextInt(5) - this.random.nextInt(5));
		Mob mob = stage.getMinion().create(this.level());
		if (mob != null) {
			mob.moveTo(blockPos, 0.0F, 0.0F);
			mob.finalizeSpawn(serverLevel, this.level().getCurrentDifficultyAt(blockPos), MobSpawnType.MOB_SUMMONED, null);
			if (playerTeam != null) {
				serverLevel.getScoreboard().addPlayerToTeam(mob.getScoreboardName(), playerTeam);
			}

			serverLevel.addFreshEntityWithPassengers(mob);
			serverLevel.gameEvent(GameEvent.ENTITY_PLACE, blockPos, GameEvent.Context.of(this));
			mob.setPersistenceRequired();
			mob.restrictTo(this.getRestrictCenter(), 8);
			this.playSound(SoundEvents.MEGASPUD_SUMMON, this.getSoundVolume(), 1.0F);
			serverLevel.levelEvent(3012, blockPos, 0);
			serverLevel.sendParticles(ParticleTypes.TRIAL_SPAWNER_DETECTION, mob.getX(), mob.getY(0.5), mob.getZ(), 100, 0.5, 0.5, 0.5, 0.0);
			this.adds.add(mob);
		}
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return super.hurt(damageSource, f);
		} else {
			f = Math.min(f, 100.0F);
			if (!this.isPowered()) {
				return super.hurt(damageSource, f);
			} else {
				if (damageSource.getEntity() instanceof Player) {
					for (Mob mob : this.adds) {
						mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200), this);
					}
				}

				if (damageSource.getEntity() != null && !damageSource.is(DamageTypes.THORNS)) {
					damageSource.getEntity().hurt(this.level().damageSources().potatoMagic(), f);
				}

				return false;
			}
		}
	}

	@Override
	public void startSeenByPlayer(ServerPlayer serverPlayer) {
		super.startSeenByPlayer(serverPlayer);
		this.bossEvent.addPlayer(serverPlayer);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer serverPlayer) {
		super.stopSeenByPlayer(serverPlayer);
		this.bossEvent.removePlayer(serverPlayer);
	}

	@Override
	public boolean isPotato() {
		return true;
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.HOSTILE;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ID_SIZE, MegaSpud.Stage.CHICKEN.size);
		builder.define(DATA_SPELL_CASTING_ID, (byte)0);
		builder.define(HAS_MINIONS, false);
	}

	public void setHasMinions(boolean bl) {
		this.entityData.set(HAS_MINIONS, bl);
	}

	@Override
	public boolean isInvulnerable() {
		return super.isInvulnerable() || this.isPowered();
	}

	@VisibleForTesting
	public void setSize(int i) {
		this.entityData.set(ID_SIZE, i);
		this.reapplyPosition();
		this.refreshDimensions();
	}

	public int getSize() {
		return this.entityData.get(ID_SIZE);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Size", this.getSize() - 1);
		compoundTag.putBoolean("wasOnGround", this.wasOnGround);
		compoundTag.putInt("homeX", this.getRestrictCenter().getX());
		compoundTag.putInt("homeY", this.getRestrictCenter().getY());
		compoundTag.putInt("homeZ", this.getRestrictCenter().getZ());
		if (this.hasCustomName()) {
			this.bossEvent.setName(this.getDisplayName());
		}
	}

	@Override
	protected Component getTypeName() {
		return this.currentStage.getStageName();
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		this.setSize(compoundTag.getInt("Size") + 1);
		super.readAdditionalSaveData(compoundTag);
		this.wasOnGround = compoundTag.getBoolean("wasOnGround");
		this.restrictTo(new BlockPos(compoundTag.getInt("homeX"), compoundTag.getInt("homeY"), compoundTag.getInt("homeZ")), 3);

		while (this.currentStage != this.currentStage.validStageBasedOnHealth(this.getHealth())) {
			this.currentStage = this.currentStage.nextStage();
		}

		this.bossEvent.setName(this.getDisplayName());
	}

	public boolean isTiny() {
		return this.getSize() <= 1;
	}

	protected ParticleOptions getParticleType() {
		return BREAKING_PARICLE;
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
		if (this.onGround() && !this.wasOnGround) {
			float f = this.getDimensions(this.getPose()).width() * 2.0F;
			float g = f / 2.0F;

			for (int i = 0; (float)i < f * 16.0F; i++) {
				float h = this.random.nextFloat() * (float) (Math.PI * 2);
				float j = this.random.nextFloat() * 0.5F + 0.5F;
				float k = Mth.sin(h) * g * j;
				float l = Mth.cos(h) * g * j;
				this.level().addParticle(this.getParticleType(), this.getX() + (double)k, this.getY(), this.getZ() + (double)l, 0.0, 0.0, 0.0);
			}

			this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) / 0.8F);
			this.targetSquish = -0.5F;
		} else if (!this.onGround() && this.wasOnGround) {
			this.targetSquish = 1.0F;
		}

		this.wasOnGround = this.onGround();
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
	public EntityType<? extends MegaSpud> getType() {
		return (EntityType<? extends MegaSpud>)super.getType();
	}

	@Override
	public void remove(Entity.RemovalReason removalReason) {
		for (Mob mob : this.adds) {
			mob.remove(removalReason);
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
				&& livingEntity.hurt(this.damageSources().mobAttack(this), this.getAttackDamage())) {
				this.doEnchantDamageEffects(this, livingEntity);
			}
		}
	}

	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
		return new Vec3(0.0, (double)entityDimensions.height() - 0.015625 * (double)this.getSize() * (double)f, 0.0);
	}

	protected boolean isDealsDamage() {
		return !this.isTiny() && this.isEffectiveAi();
	}

	protected float getAttackDamage() {
		return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.MEGASPUD_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.MEGASPUD_DEATH;
	}

	protected SoundEvent getSquishSound() {
		return SoundEvents.MEGASPUD_JUMP_HI;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.MEGASPUD_IDLE;
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F + 0.4F * (float)this.getSize();
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
		Vec3 vec32 = this.getLookAngle();
		float f = this.getJumpPower();
		float g = this.isWithinRestriction() ? 0.0F : f;
		this.setDeltaMovement(vec3.x + vec32.x * (double)g, (double)f, vec3.z + vec32.z * (double)g);
		this.hasImpulse = true;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData
	) {
		this.setSize(MegaSpud.Stage.CHICKEN.size);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
	}

	@Override
	public boolean isPowered() {
		return this.entityData.get(HAS_MINIONS);
	}

	float getSoundPitch() {
		float f = this.isTiny() ? 1.4F : 0.8F;
		return ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * f;
	}

	protected SoundEvent getJumpSound() {
		return this.isTiny() ? SoundEvents.MEGASPUD_JUMP_HI : SoundEvents.MEGASPUD_JUMP;
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return super.getDefaultDimensions(pose).scale((float)this.getSize());
	}

	static class GhastShootFireballGoal extends Goal {
		private final MegaSpud spud;
		public int chargeTime;

		public GhastShootFireballGoal(MegaSpud megaSpud) {
			this.spud = megaSpud;
		}

		@Override
		public boolean canUse() {
			return this.spud.getTarget() != null;
		}

		@Override
		public void start() {
			this.chargeTime = 0;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			LivingEntity livingEntity = this.spud.getTarget();
			if (livingEntity != null) {
				double d = 64.0;
				if (livingEntity.distanceToSqr(this.spud) < 4096.0 && this.spud.hasLineOfSight(livingEntity)) {
					Level level = this.spud.level();
					this.chargeTime++;
					if (this.chargeTime == 10 && !this.spud.isSilent()) {
						this.spud.playSound(SoundEvents.MEGASPUD_FIREBALL);
					}

					if (this.chargeTime == 20) {
						AABB aABB = this.spud.getBoundingBox().inflate(0.5);
						Vec3 vec3 = this.spud.getEyePosition();
						Vec3 vec32 = livingEntity.getEyePosition().subtract(vec3).normalize().scale(0.1);
						Vec3 vec33 = vec3;

						while (aABB.contains(vec33)) {
							vec33 = vec33.add(vec32);
						}

						LargeFireball largeFireball = new LargeFireball(level, this.spud, vec32.x, vec32.y, vec32.z, 2, false);
						largeFireball.setPos(vec33);
						level.addFreshEntity(largeFireball);
						this.chargeTime = -40;
					}
				} else if (this.chargeTime > 0) {
					this.chargeTime--;
				}
			}
		}
	}

	public static class SlimeKeepOnJumpingGoal extends Goal {
		private final Mob slime;

		public SlimeKeepOnJumpingGoal(Mob mob) {
			this.slime = mob;
			this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			return !this.slime.isPassenger();
		}

		@Override
		public void tick() {
			if (this.slime.getMoveControl() instanceof MegaSpud.SlimeMoveControl slimeMoveControl) {
				slimeMoveControl.setWantedMovement(1.0);
			}
		}
	}

	static class SlimeMoveControl extends MoveControl {
		private float yRot;
		private int jumpDelay;
		private final MegaSpud slime;
		private boolean isAggressive;

		public SlimeMoveControl(MegaSpud megaSpud) {
			super(megaSpud);
			this.slime = megaSpud;
			this.yRot = 180.0F * megaSpud.getYRot() / (float) Math.PI;
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
				if (this.mob.onGround()) {
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
		private final MegaSpud slime;
		private float chosenDegrees;
		private int nextRandomizeTime;

		public SlimeRandomDirectionGoal(MegaSpud megaSpud) {
			this.slime = megaSpud;
			this.setFlags(EnumSet.of(Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return (this.slime.onGround() || this.slime.isInWater() || this.slime.isInLava() || this.slime.hasEffect(MobEffects.LEVITATION))
				&& this.slime.getMoveControl() instanceof MegaSpud.SlimeMoveControl;
		}

		@Override
		public void tick() {
			if (--this.nextRandomizeTime <= 0) {
				this.nextRandomizeTime = this.adjustedTickDelay(40 + this.slime.getRandom().nextInt(60));
				if (this.slime.getTarget() != null && this.slime.random.nextFloat() < 0.4F) {
					this.chosenDegrees = this.getAngleToTarget(this.slime.getTarget().position()) + 90.0F;
				} else if (this.slime.hasRestriction() && !this.slime.isWithinRestriction()) {
					Vec3 vec3 = Vec3.atBottomCenterOf(this.slime.getRestrictCenter());
					this.chosenDegrees = this.getAngleToTarget(vec3) + 60.0F;
				} else {
					this.chosenDegrees = (float)this.slime.getRandom().nextInt(360);
				}
			}

			if (this.slime.getMoveControl() instanceof MegaSpud.SlimeMoveControl slimeMoveControl) {
				slimeMoveControl.setDirection(this.chosenDegrees + 20.0F - (float)this.slime.random.nextInt(40), false);
			}
		}

		private float getAngleToTarget(Vec3 vec3) {
			return (float)Mth.atan2(this.slime.getZ() - vec3.z, this.slime.getX() - vec3.x) * (180.0F / (float)Math.PI);
		}
	}

	static enum Stage {
		CHICKEN(10, 1.0F, EntityType.CHICKEN),
		ARMADILLO(9, 0.9F, EntityType.ARMADILLO),
		ZOMBIE(8, 0.8F, EntityType.POISONOUS_POTATO_ZOMBIE),
		SPIDER(7, 0.7F, EntityType.SPIDER),
		STRAY(6, 0.6F, EntityType.STRAY),
		CREEPER(5, 0.5F, EntityType.CREEPER),
		BRUTE(4, 0.4F, EntityType.PIGLIN_BRUTE),
		GHAST(3, 0.3F, EntityType.GHAST),
		PLAGUEWHALE(2, 0.2F, EntityType.PLAGUEWHALE),
		GIANT(1, 0.1F, EntityType.GIANT),
		END(1, -1.0F, EntityType.FROG);

		final int size;
		private final float percentHealthTransition;
		private final EntityType<? extends Mob> minion;
		private Component name = EntityType.MEGA_SPUD.getDescription();

		private Stage(int j, float f, EntityType<? extends Mob> entityType) {
			this.size = j;
			this.percentHealthTransition = f;
			this.minion = entityType;
		}

		public MegaSpud.Stage nextStage() {
			int i = this.ordinal() + 1;
			return i >= values().length ? this : values()[i];
		}

		@Nullable
		public MegaSpud.Stage previousStage() {
			int i = this.ordinal() - 1;
			return i < 0 ? null : values()[i];
		}

		public EntityType<? extends Mob> getMinion() {
			return this.minion;
		}

		public float getHealth() {
			return this.percentHealthTransition * 1024.0F;
		}

		public MegaSpud.Stage validStageBasedOnHealth(float f) {
			return f < this.getHealth() ? this.nextStage() : this;
		}

		public Component getStageName() {
			return this.name;
		}

		static {
			for (MegaSpud.Stage stage : values()) {
				MegaSpud.Stage stage2 = stage.previousStage();
				if (stage2 == null) {
					stage.name = EntityType.MEGA_SPUD.getDescription();
				} else {
					stage.name = Component.translatable("entity.minecraft.mega_spud." + BuiltInRegistries.ENTITY_TYPE.getKey(stage2.getMinion()).getPath());
				}
			}
		}
	}
}
