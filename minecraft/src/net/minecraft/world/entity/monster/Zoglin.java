package net.minecraft.world.entity.monster;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunIf;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.RunSometimes;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class Zoglin extends Monster implements Enemy, HoglinBase {
	private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Zoglin.class, EntityDataSerializers.BOOLEAN);
	private static final int MAX_HEALTH = 40;
	private static final int ATTACK_KNOCKBACK = 1;
	private static final float KNOCKBACK_RESISTANCE = 0.6F;
	private static final int ATTACK_DAMAGE = 6;
	private static final float BABY_ATTACK_DAMAGE = 0.5F;
	private static final int ATTACK_INTERVAL = 40;
	private static final int BABY_ATTACK_INTERVAL = 15;
	private static final int ATTACK_DURATION = 200;
	private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.3F;
	private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.4F;
	private int attackAnimationRemainingTicks;
	protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Zoglin>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS
	);
	protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.NEAREST_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.PATH,
		MemoryModuleType.ATTACK_TARGET,
		MemoryModuleType.ATTACK_COOLING_DOWN
	);

	public Zoglin(EntityType<? extends Zoglin> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 5;
	}

	@Override
	protected Brain.Provider<Zoglin> brainProvider() {
		return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		Brain<Zoglin> brain = this.brainProvider().makeBrain(dynamic);
		initCoreActivity(brain);
		initIdleActivity(brain);
		initFightActivity(brain);
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.useDefaultActivity();
		return brain;
	}

	private static void initCoreActivity(Brain<Zoglin> brain) {
		brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink()));
	}

	private static void initIdleActivity(Brain<Zoglin> brain) {
		brain.addActivity(
			Activity.IDLE,
			10,
			ImmutableList.of(
				new StartAttacking<>(Zoglin::findNearestValidAttackTarget),
				new RunSometimes(new SetEntityLookTarget(8.0F), UniformInt.of(30, 60)),
				new RunOne(ImmutableList.of(Pair.of(new RandomStroll(0.4F), 2), Pair.of(new SetWalkTargetFromLookTarget(0.4F, 3), 2), Pair.of(new DoNothing(30, 60), 1)))
			)
		);
	}

	private static void initFightActivity(Brain<Zoglin> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
			Activity.FIGHT,
			10,
			ImmutableList.of(
				new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0F),
				new RunIf<>(Zoglin::isAdult, new MeleeAttack(40)),
				new RunIf<>(Zoglin::isBaby, new MeleeAttack(15)),
				new StopAttackingIfTargetInvalid()
			),
			MemoryModuleType.ATTACK_TARGET
		);
	}

	private Optional<? extends LivingEntity> findNearestValidAttackTarget() {
		return ((List)this.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(ImmutableList.of()))
			.stream()
			.filter(this::isTargetable)
			.findFirst();
	}

	private boolean isTargetable(LivingEntity livingEntity) {
		EntityType<?> entityType = livingEntity.getType();
		return entityType != EntityType.ZOGLIN && entityType != EntityType.CREEPER && Sensor.isEntityAttackable(this, livingEntity);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_BABY_ID, false);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		super.onSyncedDataUpdated(entityDataAccessor);
		if (DATA_BABY_ID.equals(entityDataAccessor)) {
			this.refreshDimensions();
		}
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 40.0)
			.add(Attributes.MOVEMENT_SPEED, 0.3F)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.6F)
			.add(Attributes.ATTACK_KNOCKBACK, 1.0)
			.add(Attributes.ATTACK_DAMAGE, 6.0);
	}

	public boolean isAdult() {
		return !this.isBaby();
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		if (!(entity instanceof LivingEntity)) {
			return false;
		} else {
			this.attackAnimationRemainingTicks = 10;
			this.level.broadcastEntityEvent(this, (byte)4);
			this.playSound(SoundEvents.ZOGLIN_ATTACK, 1.0F, this.getVoicePitch());
			return HoglinBase.hurtAndThrowTarget(this, (LivingEntity)entity);
		}
	}

	@Override
	public boolean canBeLeashed(Player player) {
		return !this.isLeashed();
	}

	@Override
	protected void blockedByShield(LivingEntity livingEntity) {
		if (!this.isBaby()) {
			HoglinBase.throwTarget(this, livingEntity);
		}
	}

	@Override
	public double getPassengersRidingOffset() {
		return (double)this.getBbHeight() - (this.isBaby() ? 0.2 : 0.15);
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		boolean bl = super.hurt(damageSource, f);
		if (this.level.isClientSide) {
			return false;
		} else if (bl && damageSource.getEntity() instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity)damageSource.getEntity();
			if (this.canAttack(livingEntity) && !BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(this, livingEntity, 4.0)) {
				this.setAttackTarget(livingEntity);
			}

			return bl;
		} else {
			return bl;
		}
	}

	private void setAttackTarget(LivingEntity livingEntity) {
		this.brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		this.brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, livingEntity, 200L);
	}

	@Override
	public Brain<Zoglin> getBrain() {
		return (Brain<Zoglin>)super.getBrain();
	}

	protected void updateActivity() {
		Activity activity = (Activity)this.brain.getActiveNonCoreActivity().orElse(null);
		this.brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
		Activity activity2 = (Activity)this.brain.getActiveNonCoreActivity().orElse(null);
		if (activity2 == Activity.FIGHT && activity != Activity.FIGHT) {
			this.playAngrySound();
		}

		this.setAggressive(this.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
	}

	@Override
	protected void customServerAiStep() {
		this.level.getProfiler().push("zoglinBrain");
		this.getBrain().tick((ServerLevel)this.level, this);
		this.level.getProfiler().pop();
		this.updateActivity();
	}

	@Override
	public void setBaby(boolean bl) {
		this.getEntityData().set(DATA_BABY_ID, bl);
		if (!this.level.isClientSide && bl) {
			this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.5);
		}
	}

	@Override
	public boolean isBaby() {
		return this.getEntityData().get(DATA_BABY_ID);
	}

	@Override
	public void aiStep() {
		if (this.attackAnimationRemainingTicks > 0) {
			this.attackAnimationRemainingTicks--;
		}

		super.aiStep();
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 4) {
			this.attackAnimationRemainingTicks = 10;
			this.playSound(SoundEvents.ZOGLIN_ATTACK, 1.0F, this.getVoicePitch());
		} else {
			super.handleEntityEvent(b);
		}
	}

	@Override
	public int getAttackAnimationRemainingTicks() {
		return this.attackAnimationRemainingTicks;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		if (this.level.isClientSide) {
			return null;
		} else {
			return this.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) ? SoundEvents.ZOGLIN_ANGRY : SoundEvents.ZOGLIN_AMBIENT;
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.ZOGLIN_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ZOGLIN_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.ZOGLIN_STEP, 0.15F, 1.0F);
	}

	protected void playAngrySound() {
		this.playSound(SoundEvents.ZOGLIN_ANGRY, 1.0F, this.getVoicePitch());
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	@Override
	public MobType getMobType() {
		return MobType.UNDEAD;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		if (this.isBaby()) {
			compoundTag.putBoolean("IsBaby", true);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.getBoolean("IsBaby")) {
			this.setBaby(true);
		}
	}
}
