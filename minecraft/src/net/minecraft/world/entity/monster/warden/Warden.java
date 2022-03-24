package net.minecraft.world.entity.monster.warden;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class Warden extends Monster implements VibrationListener.VibrationListenerConfig {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int GAME_EVENT_LISTENER_RANGE = 16;
	private static final int VIBRATION_COOLDOWN_TICKS = 40;
	private static final int MAX_HEALTH = 500;
	private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.3F;
	private static final float KNOCKBACK_RESISTANCE = 1.0F;
	private static final float ATTACK_KNOCKBACK = 1.5F;
	private static final int ATTACK_DAMAGE = 30;
	private static final EntityDataAccessor<Integer> CLIENT_ANGER_LEVEL = SynchedEntityData.defineId(Warden.class, EntityDataSerializers.INT);
	private static final int DARKNESS_DISPLAY_LIMIT = 200;
	private static final int DARKNESS_DURATION = 260;
	private static final int DARKNESS_RADIUS = 20;
	private static final int DARKNESS_INTERVAL = 120;
	private static final int ANGERMANAGEMENT_TICK_DELAY = 20;
	private static final int ANGER_DECAY_SOUND_TRESHOLD = 70;
	private static final int DEFAULT_ANGER = 35;
	private static final int PROJECTILE_ANGER = 20;
	private static final int RECENT_PROJECTILE_TICK_THRESHOLD = 100;
	private static final int TOUCH_COOLDOWN_TICKS = 20;
	protected static final List<SensorType<? extends Sensor<? super Warden>>> SENSOR_TYPES = List.of(
		SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.WARDEN_ENTITY_SENSOR
	);
	protected static final List<MemoryModuleType<?>> MEMORY_TYPES = List.of(
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
		MemoryModuleType.NEAREST_ATTACKABLE,
		MemoryModuleType.ROAR_TARGET,
		MemoryModuleType.DISTURBANCE_LOCATION,
		MemoryModuleType.RECENT_PROJECTILE,
		MemoryModuleType.IS_SNIFFING,
		MemoryModuleType.IS_EMERGING,
		MemoryModuleType.ROAR_SOUND_DELAY,
		MemoryModuleType.DIG_COOLDOWN,
		MemoryModuleType.ROAR_SOUND_COOLDOWN,
		MemoryModuleType.SNIFF_COOLDOWN,
		MemoryModuleType.TOUCH_COOLDOWN,
		MemoryModuleType.VIBRATION_COOLDOWN
	);
	private static final int DIGGING_PARTICLES_AMOUNT = 30;
	private static final float DIGGING_PARTICLES_DURATION = 4.5F;
	private static final float DIGGING_PARTICLES_OFFSET = 0.7F;
	private float earAnimation;
	private float earAnimationO;
	private float heartAnimation;
	private float heartAnimationO;
	public AnimationState roarAnimationState = new AnimationState();
	public AnimationState sniffAnimationState = new AnimationState();
	public AnimationState emergeAnimationState = new AnimationState();
	public AnimationState diggingAnimationState = new AnimationState();
	public AnimationState attackAnimationState = new AnimationState();
	private final DynamicGameEventListener dynamicGameEventListener;
	private final VibrationListener vibrationListener;
	private AngerManagement angerManagement = new AngerManagement(Collections.emptyMap());

	public Warden(EntityType<? extends Monster> entityType, Level level) {
		super(entityType, level);
		this.vibrationListener = new VibrationListener(new EntityPositionSource(this, this.getEyeHeight()), 16, this, null, 0, 0);
		this.dynamicGameEventListener = new DynamicGameEventListener(this.vibrationListener);
		this.xpReward = 5;
		this.getNavigation().setCanFloat(true);
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader levelReader) {
		return levelReader.noCollision(this);
	}

	@Override
	public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
		return 0.0F;
	}

	@Override
	public boolean isInvulnerableTo(DamageSource damageSource) {
		return this.hasPose(Pose.DIGGING) || this.hasPose(Pose.EMERGING) || super.isInvulnerableTo(damageSource);
	}

	@Override
	protected boolean canRide(Entity entity) {
		return false;
	}

	@Override
	public boolean canDisableShield() {
		return true;
	}

	@Override
	protected float nextStep() {
		return this.moveDist + 0.55F;
	}

	@Override
	public double getFluidJumpThreshold() {
		return (double)this.getEyeHeight() * 0.9;
	}

	@Override
	protected void jumpInLiquid(TagKey<Fluid> tagKey) {
		this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.4, 0.0));
	}

	@Override
	protected float getWaterSlowDown() {
		return 0.98F;
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 500.0)
			.add(Attributes.MOVEMENT_SPEED, 0.3F)
			.add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
			.add(Attributes.ATTACK_KNOCKBACK, 1.5)
			.add(Attributes.ATTACK_DAMAGE, 30.0);
	}

	@Override
	public boolean occludesVibrations() {
		return true;
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.HOSTILE;
	}

	@Override
	protected float getSoundVolume() {
		return 4.0F;
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return this.hasPose(Pose.ROARING) ? null : this.getAngerLevel().getAmbientSound();
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
	public boolean doHurtTarget(Entity entity) {
		this.level.broadcastEntityEvent(this, (byte)4);
		this.playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 10.0F, this.getVoicePitch());
		return super.doHurtTarget(entity);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(CLIENT_ANGER_LEVEL, 0);
	}

	public int getClientAngerLevel() {
		return this.entityData.get(CLIENT_ANGER_LEVEL);
	}

	private void syncClientAngerLevel() {
		this.entityData.set(CLIENT_ANGER_LEVEL, this.angerManagement.getActiveAnger());
	}

	@Override
	public void tick() {
		if (this.level instanceof ServerLevel serverLevel) {
			this.vibrationListener.tick(serverLevel);
			if (this.hasCustomName()) {
				WardenAi.setDigCooldown(this);
			}
		}

		super.tick();
		if (this.level.isClientSide()) {
			if (this.tickCount % this.getHeartBeatDelay() == 0) {
				this.heartAnimation = 0.0F;
				this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_HEARTBEAT, this.getSoundSource(), 5.0F, this.getVoicePitch(), false);
			}

			this.earAnimationO = this.earAnimation;
			if (this.earAnimation < 1.0F) {
				this.earAnimation += 0.1F;
			}

			this.heartAnimationO = this.heartAnimation;
			if (this.heartAnimation < 1.0F) {
				this.heartAnimation += 0.1F;
			}

			switch (this.getPose()) {
				case EMERGING:
					this.clientDiggingParticles(this.emergeAnimationState);
					break;
				case DIGGING:
					this.clientDiggingParticles(this.diggingAnimationState);
			}
		}
	}

	@Override
	protected void customServerAiStep() {
		this.level.getProfiler().push("wardenBrain");
		this.getBrain().tick((ServerLevel)this.level, this);
		this.level.getProfiler().pop();
		super.customServerAiStep();
		if ((this.tickCount + this.getId()) % 120 == 0) {
			applyDarknessAround((ServerLevel)this.level, this.position(), this, 20);
		}

		if (this.tickCount % 20 == 0) {
			int i = this.angerManagement.getActiveAnger();
			this.angerManagement.tick();
			this.onDecayedAnger(i, this.angerManagement.getActiveAnger());
		}

		this.syncClientAngerLevel();
		WardenAi.updateActivity(this);
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 4) {
			this.attackAnimationState.start();
		} else if (b == 61) {
			this.earAnimation = 0.0F;
		} else {
			super.handleEntityEvent(b);
		}
	}

	private int getHeartBeatDelay() {
		float f = (float)this.getClientAngerLevel() / (float)AngerLevel.ANGRY.getMinimumAnger();
		return 40 - Mth.floor(Mth.clamp(f, 0.0F, 1.0F) * 30.0F);
	}

	public float getEarAnimation(float f) {
		return Math.max(1.0F - Mth.lerp(f, this.earAnimationO, this.earAnimation), 0.0F);
	}

	public float getHeartAnimation(float f) {
		return Math.max(1.0F - Mth.lerp(f, this.heartAnimationO, this.heartAnimation), 0.0F);
	}

	private void clientDiggingParticles(AnimationState animationState) {
		if ((float)(Util.getMillis() - animationState.startTime()) < 4500.0F) {
			Random random = this.getRandom();
			BlockState blockState = this.level.getBlockState(this.blockPosition().below());

			for (int i = 0; i < 30; i++) {
				double d = this.getX() + (double)Mth.randomBetween(random, -0.7F, 0.7F);
				double e = this.getY();
				double f = this.getZ() + (double)Mth.randomBetween(random, -0.7F, 0.7F);
				this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), d, e, f, 0.0, 0.0, 0.0);
			}
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_POSE.equals(entityDataAccessor)) {
			switch (this.getPose()) {
				case EMERGING:
					this.emergeAnimationState.start();
					break;
				case DIGGING:
					this.diggingAnimationState.start();
					break;
				case ROARING:
					this.roarAnimationState.start();
					break;
				case SNIFFING:
					this.sniffAnimationState.start();
			}
		}

		super.onSyncedDataUpdated(entityDataAccessor);
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
	public Brain<Warden> getBrain() {
		return (Brain<Warden>)super.getBrain();
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	@Override
	public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener, ServerLevel> biConsumer) {
		if (this.level instanceof ServerLevel serverLevel) {
			biConsumer.accept(this.dynamicGameEventListener, serverLevel);
		}
	}

	@Override
	public TagKey<GameEvent> getListenableEvents() {
		return GameEventTags.WARDEN_EVENTS_CAN_LISTEN;
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

	public static void applyDarknessAround(ServerLevel serverLevel, Vec3 vec3, @Nullable Entity entity, int i) {
		MobEffectInstance mobEffectInstance = new MobEffectInstance(MobEffects.DARKNESS, 260, 0, false, false);
		MobEffectUtil.addEffectToPlayersAround(serverLevel, entity, vec3, (double)i, mobEffectInstance, 200);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		AngerManagement.CODEC.encodeStart(NbtOps.INSTANCE, this.angerManagement).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("anger", tag));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("anger")) {
			AngerManagement.CODEC
				.parse(new Dynamic<>(NbtOps.INSTANCE, compoundTag.get("anger")))
				.resultOrPartial(LOGGER::error)
				.ifPresent(angerManagement -> this.angerManagement = angerManagement);
			this.syncClientAngerLevel();
		}
	}

	private void onDecayedAnger(int i, int j) {
		if (i >= 70 && j < 70) {
			this.playListeningSound();
		}
	}

	private void playListeningSound() {
		if (!this.hasPose(Pose.ROARING)) {
			SoundEvent soundEvent = this.getAngerLevel() == AngerLevel.CALM ? SoundEvents.WARDEN_LISTENING : SoundEvents.WARDEN_LISTENING_ANGRY;
			this.playSound(soundEvent, 10.0F, this.getVoicePitch());
		}
	}

	public AngerLevel getAngerLevel() {
		return AngerLevel.byAnger(this.angerManagement.getActiveAnger());
	}

	public void clearAnger(Entity entity) {
		this.angerManagement.clearAnger(entity);
	}

	public void increaseAngerAt(@Nullable Entity entity) {
		this.increaseAngerAt(entity, entity instanceof Projectile ? 20 : 35);
	}

	@VisibleForTesting
	public void increaseAngerAt(@Nullable Entity entity, int i) {
		if (canTargetEntity(entity)) {
			WardenAi.setDigCooldown(this);
			boolean bl = this.getEntityAngryAt().filter(livingEntity -> !(livingEntity instanceof Player)).isPresent();
			int j = this.angerManagement.addAnger(entity, i);
			if (entity instanceof Player player && bl && AngerLevel.byAnger(j) == AngerLevel.ANGRY) {
				this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, player);
			}

			this.playListeningSound();
		}
	}

	public Optional<LivingEntity> getEntityAngryAt() {
		return this.getAngerLevel() == AngerLevel.ANGRY ? this.angerManagement.getActiveEntity(this.level) : Optional.empty();
	}

	@Nullable
	@Override
	public LivingEntity getTarget() {
		return (LivingEntity)this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
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
		this.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200L);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	@Override
	public double getMeleeAttackRangeSqr(LivingEntity livingEntity) {
		return 8.0;
	}

	@Override
	public boolean isWithinMeleeAttackRange(LivingEntity livingEntity) {
		double d = this.distanceToSqr(livingEntity.getX(), livingEntity.getY() - (double)(this.getBbHeight() / 2.0F), livingEntity.getZ());
		return d <= this.getMeleeAttackRangeSqr(livingEntity);
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		boolean bl = super.hurt(damageSource, f);
		if (this.level.isClientSide) {
			return false;
		} else {
			if (bl) {
				Entity entity = damageSource.getEntity();
				this.increaseAngerAt(entity, AngerLevel.ANGRY.getMinimumAnger() + 20);
				if (this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty()
					&& entity instanceof LivingEntity livingEntity
					&& (!(damageSource instanceof IndirectEntityDamageSource) || this.closerThan(livingEntity, 5.0))) {
					this.brain.setMemory(MemoryModuleType.ATTACK_TARGET, livingEntity);
				}
			}

			return bl;
		}
	}

	@Override
	public void playerTouch(Player player) {
		if (!this.getBrain().hasMemoryValue(MemoryModuleType.TOUCH_COOLDOWN)) {
			this.getBrain().setMemoryWithExpiry(MemoryModuleType.TOUCH_COOLDOWN, Unit.INSTANCE, 20L);
			this.increaseAngerAt(player);
			WardenAi.setDisturbanceLocation(this, player.blockPosition());
		}
	}

	@Override
	public boolean shouldListen(ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity) {
		if (this.getBrain().getMemory(MemoryModuleType.VIBRATION_COOLDOWN).isPresent()) {
			return false;
		} else {
			Pose pose = this.getPose();
			return pose != Pose.DIGGING && pose != Pose.EMERGING ? !(entity instanceof LivingEntity) || canTargetEntity(entity) : false;
		}
	}

	@Override
	public void onSignalReceive(
		ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity, int i
	) {
		this.brain.setMemoryWithExpiry(MemoryModuleType.VIBRATION_COOLDOWN, Unit.INSTANCE, 40L);
		serverLevel.broadcastEntityEvent(this, (byte)61);
		this.playSound(SoundEvents.WARDEN_TENDRIL_CLICKS, 5.0F, this.getVoicePitch());
		BlockPos blockPos2 = blockPos;
		if (entity instanceof Projectile projectile) {
			if (this.getBrain().hasMemoryValue(MemoryModuleType.RECENT_PROJECTILE)) {
				Entity entity2 = projectile.getOwner();
				if (canTargetEntity(entity2) && this.closerThan(entity2, 30.0)) {
					blockPos2 = entity2.blockPosition();
					this.increaseAngerAt(entity2);
				}
			}

			this.getBrain().setMemoryWithExpiry(MemoryModuleType.RECENT_PROJECTILE, Unit.INSTANCE, 100L);
		} else {
			this.increaseAngerAt(entity);
		}

		if (this.getAngerLevel() != AngerLevel.ANGRY
			&& (entity instanceof Projectile || (Boolean)this.angerManagement.getActiveEntity(serverLevel).map(livingEntity -> livingEntity == entity).orElse(true))) {
			WardenAi.setDisturbanceLocation(this, blockPos2);
		}
	}

	@VisibleForTesting
	public AngerManagement getAngerManagement() {
		return this.angerManagement;
	}
}
