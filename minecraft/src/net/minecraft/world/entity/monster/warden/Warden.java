package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.GameEventListenerRegistrar;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;

public class Warden extends Monster implements VibrationListener.VibrationListenerConfig {
	public static final Map<EntityType<?>, Integer> ANGER_WEIGHTS = new Builder<EntityType<?>, Integer>()
		.put(EntityType.PLAYER, 35)
		.put(EntityType.SNOWBALL, 10)
		.build();
	private static final int DARKNESS_INTERVAL = 120;
	private static final int DARKNESS_DURATION = 640;
	private static final int DARKNESS_RADIUS = 20;
	public static final int DARKNESS_DISPLAY_LIMIT = 200;
	private static final int LISTENER_RANGE = 16;
	private static final int MAX_HEALTH = 500;
	private static final int ATTACK_KNOCKBACK = 0;
	private static final float KNOCKBACK_RESISTANCE = 1.25F;
	private static final int ATTACK_DAMAGE = 30;
	private static final int DEFAULT_ANGER_INCREASE = 1;
	private static final int MAX_ANGER = 150;
	public static final int TOP_ANGER_CUTOFF = 80;
	private static final int SLIGHT_ANGER_CUTOFF = 40;
	private static final int BASE_SUSPICION_ANGER = 20;
	private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.3F;
	private static final int NUM_DIGGING_PARTICLES = 30;
	private static final float MIN_TICKS_AFTER_VIBRATION_BEFORE_SOUND = 90.0F;
	private static final float DIGGING_PARTICLE_OFFSET = 0.7F;
	private static final int RECENT_PROJECTILE_TICK_THRESHOLD = 100;
	private static final int NON_CALM_VIBRATION_THRESHOLD = 120;
	private static final int CALM_VIBRATION_THRESHOLD = 20;
	private static final int VIBRATION_COOLDOWN_TICKS = 40;
	private float earAnimation;
	private float earAnimationO;
	private float heartAnimation;
	private float heartAnimationO;
	private int attackAnimationTick;
	private int ticksSinceBecameAngrierAt = 80;
	private static final EntityDataAccessor<Integer> TOP_ANGER = SynchedEntityData.defineId(Warden.class, EntityDataSerializers.INT);
	@Nullable
	private SoundEvent queuedListeningEvent;
	private final GameEventListenerRegistrar gameEventListenerRegistrar;
	private final VibrationListener vibrationListener;
	private static final float RUMBLE_TIME = 4.5F;
	public AnimationState roarAnimationState = new AnimationState();
	public AnimationState sniffAnimationState = new AnimationState();
	public AnimationState emergeAnimationState = new AnimationState();
	public AnimationState diggingAnimationState = new AnimationState();
	protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Warden>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.WARDEN_SPECIFIC_SENSOR, SensorType.HURT_BY
	);
	protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.NEAREST_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.PATH,
		MemoryModuleType.ATTACK_TARGET,
		MemoryModuleType.ATTACK_COOLING_DOWN,
		MemoryModuleType.DISTURBANCE_LOCATION,
		MemoryModuleType.ROAR_TARGET,
		MemoryModuleType.IS_SNIFFING,
		MemoryModuleType.HURT_BY,
		MemoryModuleType.IS_EMERGING,
		MemoryModuleType.IS_DIGGING,
		MemoryModuleType.LAST_DISTURBANCE,
		MemoryModuleType.LAST_SNIFF,
		MemoryModuleType.NEAREST_ATTACKABLE,
		MemoryModuleType.RECENT_PROJECTILE,
		MemoryModuleType.LAST_AUDIBLE_ROAR,
		MemoryModuleType.LAST_ROAR_STARTED,
		MemoryModuleType.IN_VIBRATION_COOLDOWN
	);
	private final SuspectTracking suspectTracking;

	public Warden(EntityType<? extends Warden> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 5;
		this.vibrationListener = new VibrationListener(new EntityPositionSource(this, this.getEyeHeight()), 16, this);
		this.gameEventListenerRegistrar = new GameEventListenerRegistrar(this.vibrationListener);
		this.getNavigation().setCanFloat(true);
		this.suspectTracking = new SuspectTracking(1, 150, () -> this.tickCount % 60 == 0 && !this.hasPose(Pose.ROARING) && this.getTarget() == null);
	}

	public Warden(Level level) {
		this(EntityType.WARDEN, level);
	}

	@Override
	public boolean occludesVibrations() {
		return true;
	}

	@Override
	public GameEventListenerRegistrar getGameEventListenerRegistrar() {
		return this.gameEventListenerRegistrar;
	}

	public SuspectTracking getSuspectTracking() {
		return this.suspectTracking;
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
		WardenAi.markDisturbed(this);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	@Override
	protected Brain.Provider<Warden> brainProvider() {
		return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return WardenAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(TOP_ANGER, 0);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 500.0)
			.add(Attributes.MOVEMENT_SPEED, 0.3F)
			.add(Attributes.KNOCKBACK_RESISTANCE, 1.25)
			.add(Attributes.ATTACK_KNOCKBACK, 0.0)
			.add(Attributes.ATTACK_DAMAGE, 30.0);
	}

	@Override
	public Brain<Warden> getBrain() {
		return (Brain<Warden>)super.getBrain();
	}

	@Override
	protected void customServerAiStep() {
		this.level.getProfiler().push("wardenBrain");
		this.getBrain().tick((ServerLevel)this.level, this);
		this.level.getProfiler().pop();
		this.suspectTracking.update();
		WardenAi.updateActivity(this);
		this.giveDarknessToPlayersAround();
		this.ticksSinceBecameAngrierAt++;
		if (this.ticksSinceBecameAngrierAt > 5 && this.queuedListeningEvent != null) {
			if (!this.hasPose(Pose.ROARING)) {
				this.playSound(this.queuedListeningEvent, 10.0F, this.getVoicePitch());
			}

			this.queuedListeningEvent = null;
		}
	}

	private void giveDarknessToPlayersAround() {
		if ((this.tickCount + this.getId()) % 120 == 0) {
			MobEffectInstance mobEffectInstance = new MobEffectInstance(MobEffects.DARKNESS, 640, 0, false, false);
			MobEffectUtil.addEffectToPlayersAround((ServerLevel)this.level, this, this.position(), 20.0, mobEffectInstance, 200);
		}
	}

	private SoundEvent getListeningSound(Warden.AngerLevel angerLevel) {
		return switch (angerLevel) {
			case AGITATED, ANGRY -> SoundEvents.WARDEN_LISTENING_ANGRY;
			default -> SoundEvents.WARDEN_LISTENING;
		};
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_POSE.equals(entityDataAccessor)) {
			if (this.hasPose(Pose.ROARING)) {
				this.roarAnimationState.start();
			}

			if (this.hasPose(Pose.SNIFFING)) {
				this.sniffAnimationState.start();
			}

			if (this.hasPose(Pose.EMERGING)) {
				this.emergeAnimationState.start();
			}

			if (this.hasPose(Pose.DIGGING)) {
				this.diggingAnimationState.start();
			}
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	private void diggingParticles(Warden warden, Level level, AnimationState animationState) {
		if ((float)(Util.getMillis() - animationState.startTime()) < 4500.0F) {
			Random random = warden.getRandom();
			BlockState blockState = level.getBlockState(warden.blockPosition().below());

			for (int i = 0; i < 30; i++) {
				double d = warden.getX() + (double)Mth.randomBetween(random, -0.7F, 0.7F);
				double e = warden.getY();
				double f = warden.getZ() + (double)Mth.randomBetween(random, -0.7F, 0.7F);
				level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), d, e, f, 0.0, 0.0, 0.0);
			}
		}
	}

	@Override
	public void tick() {
		boolean bl = this.level.isClientSide();
		if (bl) {
			if (this.hasPose(Pose.EMERGING)) {
				this.diggingParticles(this, this.level, this.emergeAnimationState);
			}

			if (this.hasPose(Pose.DIGGING)) {
				this.diggingParticles(this, this.level, this.diggingAnimationState);
			}
		}

		this.vibrationListener.tick(this.level);
		if (!this.level.isClientSide()) {
			this.setTopAnger(this.suspectTracking.getActiveAnger());
		}

		super.tick();
		if (bl && this.tickCount % this.getHeartRate() == 0) {
			this.heartAnimation = 0.0F;
			this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_HEARTBEAT, this.getSoundSource(), 5.0F, this.getVoicePitch(), false);
		}
	}

	@Override
	public boolean isInvulnerableTo(DamageSource damageSource) {
		Pose pose = this.getPose();
		return pose == Pose.DIGGING || pose == Pose.EMERGING || super.isInvulnerableTo(damageSource);
	}

	private int getHeartRate() {
		float f = (float)this.getTopAnger() / 80.0F;
		return 40 - Mth.clamp(Mth.floor(f * 30.0F), 0, 30);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.attackAnimationTick > 0) {
			this.attackAnimationTick--;
		}

		this.earAnimationO = this.earAnimation;
		if (this.earAnimation < 1.0F) {
			this.earAnimation = (float)((double)this.earAnimation + 0.1);
		}

		this.heartAnimationO = this.heartAnimation;
		if (this.heartAnimation < 1.0F) {
			this.heartAnimation = (float)((double)this.heartAnimation + 0.1);
		}
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 4) {
			this.attackAnimationTick = 15;
		} else if (b == 61) {
			this.earAnimation = 0.0F;
		} else {
			super.handleEntityEvent(b);
		}
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		this.level.broadcastEntityEvent(this, (byte)4);
		this.playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 10.0F, this.getVoicePitch());
		WardenAi.markDisturbed(this);
		return super.doHurtTarget(entity);
	}

	public void increaseAngerAt(Entity entity) {
		int i = (Integer)ANGER_WEIGHTS.getOrDefault(entity.getType(), 20);
		this.suspectTracking.addSuspicion(entity.getUUID(), i);
		float f = (float)this.getTopAnger();
		this.setTopAnger(this.suspectTracking.getActiveAnger());
		float g = (float)this.getTopAnger();
		Warden.AngerLevel angerLevel = this.getAngerLevel();
		int j = angerLevel == Warden.AngerLevel.CALM ? 20 : 120;
		if (this.ticksSinceBecameAngrierAt > j || f < 40.0F && g >= 40.0F) {
			this.queuedListeningEvent = this.getListeningSound(angerLevel);
			this.ticksSinceBecameAngrierAt = 0;
		}
	}

	public Warden.AngerLevel getAngerLevel() {
		int i = this.getTopAnger();
		if (i >= 80) {
			return Warden.AngerLevel.ANGRY;
		} else {
			return i >= 40 ? Warden.AngerLevel.AGITATED : Warden.AngerLevel.CALM;
		}
	}

	public int getTopAnger() {
		return this.entityData.get(TOP_ANGER);
	}

	public void setTopAnger(int i) {
		this.entityData.set(TOP_ANGER, i);
	}

	@Override
	public Tag.Named<GameEvent> getListenableEvents() {
		return GameEventTags.WARDEN_EVENTS_CAN_LISTEN;
	}

	@Override
	public boolean shouldListen(Level level, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity) {
		Brain<Warden> brain = this.getBrain();
		if (brain.getMemory(MemoryModuleType.IN_VIBRATION_COOLDOWN).isPresent()) {
			return false;
		} else {
			Pose pose = this.getPose();
			if (pose == Pose.DIGGING || pose == Pose.EMERGING) {
				return false;
			} else {
				return entity instanceof LivingEntity && !canTargetEntity(entity) ? false : entity == null || entity.getType() != EntityType.WARDEN;
			}
		}
	}

	public static boolean canTargetEntity(@Nullable Entity entity) {
		if (entity instanceof LivingEntity livingEntity
			&& EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)
			&& livingEntity.getType() != EntityType.ARMOR_STAND
			&& livingEntity.getType() != EntityType.WARDEN
			&& !livingEntity.isDeadOrDying()) {
			return true;
		}

		return false;
	}

	@Override
	public void onSignalReceive(Level level, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity, int i) {
		Brain<Warden> brain = this.getBrain();
		level.broadcastEntityEvent(this, (byte)61);
		this.playSound(SoundEvents.WARDEN_TENDRIL_CLICKS, 5.0F, this.getVoicePitch());
		brain.setMemoryWithExpiry(MemoryModuleType.IN_VIBRATION_COOLDOWN, Unit.INSTANCE, 40L);
		BlockPos blockPos2 = blockPos;
		if (entity instanceof Projectile) {
			if (brain.getMemory(MemoryModuleType.RECENT_PROJECTILE).isPresent()) {
				Entity entity2 = ((Projectile)entity).getOwner();
				if (canTargetEntity(entity2)) {
					blockPos2 = entity2.blockPosition();
					this.increaseAngerAt(entity2);
				}
			}
		} else if (entity != null && (!(entity instanceof LivingEntity) || !((LivingEntity)entity).isDeadOrDying())) {
			this.increaseAngerAt(entity);
		}

		Optional<UUID> optional = this.suspectTracking.getActiveSuspect();
		WardenAi.markDisturbed(this);
		boolean bl = optional.isPresent() && entity != null && optional.get() == entity.getUUID();
		boolean bl2 = entity instanceof Projectile;
		if (optional.isEmpty() || bl || bl2) {
			WardenAi.noticeSuspiciousLocation(this, blockPos2);
			if (bl2) {
				brain.setMemoryWithExpiry(MemoryModuleType.RECENT_PROJECTILE, Unit.INSTANCE, 100L);
			}
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		this.suspectTracking.write(compoundTag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.suspectTracking.read(compoundTag);
		if (!this.level.isClientSide()) {
			this.setTopAnger(this.suspectTracking.getActiveAnger());
		}
	}

	@Override
	public MobType getMobType() {
		return MobType.UNDEAD;
	}

	@Override
	protected boolean canRide(Entity entity) {
		return false;
	}

	@Nullable
	@Override
	public LivingEntity getTarget() {
		return (LivingEntity)this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	public float getEarEmissiveAlpha(float f, float g) {
		return this.getEmissiveAlpha(f, this.earAnimationO, this.earAnimation);
	}

	public float getHeartEmissiveAlpha(float f, float g) {
		return this.getEmissiveAlpha(f, this.heartAnimationO, this.heartAnimation);
	}

	private float getEmissiveAlpha(float f, float g, float h) {
		float i = 1.0F - Mth.lerp(f, g, h);
		return Math.max(i, 0.0F);
	}

	public float getPrimaryPulsatingSpotsAlpha(float f, float g) {
		return Math.max(0.0F, Mth.cos(g * 0.045F) * 0.25F);
	}

	public float getSecondaryPulsatingSpotsAlpha(float f, float g) {
		return Math.max(0.0F, Mth.cos(g * 0.045F + (float) Math.PI) * 0.25F);
	}

	public int getAttackAnimationTick() {
		return this.attackAnimationTick;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		Warden.AngerLevel angerLevel = this.getAngerLevel();
		if (angerLevel != Warden.AngerLevel.CALM) {
			if (!((float)this.ticksSinceBecameAngrierAt <= 90.0F) && !this.hasPose(Pose.ROARING)) {
				return angerLevel == Warden.AngerLevel.AGITATED ? SoundEvents.WARDEN_AGITATED : SoundEvents.WARDEN_ANGRY;
			} else {
				return null;
			}
		} else {
			return SoundEvents.WARDEN_AMBIENT;
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.WARDEN_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WARDEN_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.WARDEN_STEP, 10.0F, 1.0F);
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.HOSTILE;
	}

	@Override
	protected float getSoundVolume() {
		return 4.0F;
	}

	@Override
	protected float nextStep() {
		return this.moveDist + 0.55F;
	}

	@Override
	public boolean canDisableShield() {
		return true;
	}

	@Override
	public void playerTouch(Player player) {
		if (this.tickCount % 20 == 0) {
			this.increaseAngerAt(player);
			WardenAi.markDisturbed(this);
			WardenAi.noticeSuspiciousLocation(this, player.blockPosition());
		}
	}

	public static enum AngerLevel {
		CALM,
		AGITATED,
		ANGRY;
	}
}
