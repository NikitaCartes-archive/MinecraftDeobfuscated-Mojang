/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster.warden;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.monster.warden.AngerManagement;
import net.minecraft.world.entity.monster.warden.WardenAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Warden
extends Monster
implements VibrationListener.VibrationListenerConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int GAME_EVENT_LISTENER_RANGE = 16;
    private static final int VIBRATION_COOLDOWN_TICKS = 40;
    private static final int TIME_TO_USE_MELEE_UNTIL_SONIC_BOOM = 200;
    private static final int MAX_HEALTH = 500;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.3f;
    private static final float KNOCKBACK_RESISTANCE = 1.0f;
    private static final float ATTACK_KNOCKBACK = 1.5f;
    private static final int ATTACK_DAMAGE = 30;
    private static final EntityDataAccessor<Integer> CLIENT_ANGER_LEVEL = SynchedEntityData.defineId(Warden.class, EntityDataSerializers.INT);
    private static final int DARKNESS_DISPLAY_LIMIT = 200;
    private static final int DARKNESS_DURATION = 260;
    private static final int DARKNESS_RADIUS = 20;
    private static final int DARKNESS_INTERVAL = 120;
    private static final int ANGERMANAGEMENT_TICK_DELAY = 20;
    private static final int DEFAULT_ANGER = 35;
    private static final int PROJECTILE_ANGER = 10;
    private static final int ON_HURT_ANGER_BOOST = 20;
    private static final int RECENT_PROJECTILE_TICK_THRESHOLD = 100;
    private static final int TOUCH_COOLDOWN_TICKS = 20;
    private static final int DIGGING_PARTICLES_AMOUNT = 30;
    private static final float DIGGING_PARTICLES_DURATION = 4.5f;
    private static final float DIGGING_PARTICLES_OFFSET = 0.7f;
    private static final int PROJECTILE_ANGER_DISTANCE = 30;
    private int tendrilAnimation;
    private int tendrilAnimationO;
    private int heartAnimation;
    private int heartAnimationO;
    public AnimationState roarAnimationState = new AnimationState();
    public AnimationState sniffAnimationState = new AnimationState();
    public AnimationState emergeAnimationState = new AnimationState();
    public AnimationState diggingAnimationState = new AnimationState();
    public AnimationState attackAnimationState = new AnimationState();
    public AnimationState sonicBoomAnimationState = new AnimationState();
    private final DynamicGameEventListener<VibrationListener> dynamicGameEventListener;
    private AngerManagement angerManagement = new AngerManagement(this::canTargetEntity, Collections.emptyList());

    public Warden(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.dynamicGameEventListener = new DynamicGameEventListener<VibrationListener>(new VibrationListener(new EntityPositionSource(this, this.getEyeHeight()), 16, this));
        this.xpReward = 5;
        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0f);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, 8.0f);
        this.setPathfindingMalus(BlockPathTypes.POWDER_SNOW, 8.0f);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0f);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0f);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0f);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, this.hasPose(Pose.EMERGING) ? 1 : 0);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        if (clientboundAddEntityPacket.getData() == 1) {
            this.setPose(Pose.EMERGING);
        }
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return super.checkSpawnObstruction(levelReader) && levelReader.noCollision(this, this.getType().getDimensions().makeBoundingBox(this.position()));
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        return 0.0f;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if (this.isDiggingOrEmerging() && !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        return super.isInvulnerableTo(damageSource);
    }

    private boolean isDiggingOrEmerging() {
        return this.hasPose(Pose.DIGGING) || this.hasPose(Pose.EMERGING);
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
        return this.moveDist + 0.55f;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 500.0).add(Attributes.MOVEMENT_SPEED, 0.3f).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.ATTACK_KNOCKBACK, 1.5).add(Attributes.ATTACK_DAMAGE, 30.0);
    }

    @Override
    public boolean dampensVibrations() {
        return true;
    }

    @Override
    protected float getSoundVolume() {
        return 4.0f;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.hasPose(Pose.ROARING) || this.isDiggingOrEmerging()) {
            return null;
        }
        return this.getAngerLevel().getAmbientSound();
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
        this.playSound(SoundEvents.WARDEN_STEP, 10.0f, 1.0f);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        this.level.broadcastEntityEvent(this, (byte)4);
        this.playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 10.0f, this.getVoicePitch());
        SonicBoom.setCooldown(this, 40);
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
        this.entityData.set(CLIENT_ANGER_LEVEL, this.getActiveAnger());
    }

    @Override
    public void tick() {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.dynamicGameEventListener.getListener().tick(serverLevel);
            if (this.isPersistenceRequired() || this.requiresCustomPersistence()) {
                WardenAi.setDigCooldown(this);
            }
        }
        super.tick();
        if (this.level.isClientSide()) {
            if (this.tickCount % this.getHeartBeatDelay() == 0) {
                this.heartAnimation = 10;
                if (!this.isSilent()) {
                    this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_HEARTBEAT, this.getSoundSource(), 5.0f, this.getVoicePitch(), false);
                }
            }
            this.tendrilAnimationO = this.tendrilAnimation;
            if (this.tendrilAnimation > 0) {
                --this.tendrilAnimation;
            }
            this.heartAnimationO = this.heartAnimation;
            if (this.heartAnimation > 0) {
                --this.heartAnimation;
            }
            switch (this.getPose()) {
                case EMERGING: {
                    this.clientDiggingParticles(this.emergeAnimationState);
                    break;
                }
                case DIGGING: {
                    this.clientDiggingParticles(this.diggingAnimationState);
                }
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel serverLevel = (ServerLevel)this.level;
        serverLevel.getProfiler().push("wardenBrain");
        this.getBrain().tick(serverLevel, this);
        this.level.getProfiler().pop();
        super.customServerAiStep();
        if ((this.tickCount + this.getId()) % 120 == 0) {
            Warden.applyDarknessAround(serverLevel, this.position(), this, 20);
        }
        if (this.tickCount % 20 == 0) {
            this.angerManagement.tick(serverLevel, this::canTargetEntity);
            this.syncClientAngerLevel();
        }
        WardenAi.updateActivity(this);
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == 4) {
            this.roarAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
        } else if (b == 61) {
            this.tendrilAnimation = 10;
        } else if (b == 62) {
            this.sonicBoomAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(b);
        }
    }

    private int getHeartBeatDelay() {
        float f = (float)this.getClientAngerLevel() / (float)AngerLevel.ANGRY.getMinimumAnger();
        return 40 - Mth.floor(Mth.clamp(f, 0.0f, 1.0f) * 30.0f);
    }

    public float getTendrilAnimation(float f) {
        return Mth.lerp(f, this.tendrilAnimationO, this.tendrilAnimation) / 10.0f;
    }

    public float getHeartAnimation(float f) {
        return Mth.lerp(f, this.heartAnimationO, this.heartAnimation) / 10.0f;
    }

    private void clientDiggingParticles(AnimationState animationState) {
        if ((float)animationState.getAccumulatedTime() < 4500.0f) {
            RandomSource randomSource = this.getRandom();
            BlockState blockState = this.getBlockStateOn();
            if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
                for (int i = 0; i < 30; ++i) {
                    double d = this.getX() + (double)Mth.randomBetween(randomSource, -0.7f, 0.7f);
                    double e = this.getY();
                    double f = this.getZ() + (double)Mth.randomBetween(randomSource, -0.7f, 0.7f);
                    this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), d, e, f, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_POSE.equals(entityDataAccessor)) {
            switch (this.getPose()) {
                case ROARING: {
                    this.roarAnimationState.start(this.tickCount);
                    break;
                }
                case SNIFFING: {
                    this.sniffAnimationState.start(this.tickCount);
                    break;
                }
                case EMERGING: {
                    this.emergeAnimationState.start(this.tickCount);
                    break;
                }
                case DIGGING: {
                    this.diggingAnimationState.start(this.tickCount);
                }
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override
    public boolean ignoreExplosion() {
        return this.isDiggingOrEmerging();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return WardenAi.makeBrain(this, dynamic);
    }

    public Brain<Warden> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> biConsumer) {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            biConsumer.accept(this.dynamicGameEventListener, serverLevel);
        }
    }

    @Override
    public TagKey<GameEvent> getListenableEvents() {
        return GameEventTags.WARDEN_CAN_LISTEN;
    }

    @Override
    public boolean canTriggerAvoidVibration() {
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Contract(value="null->false")
    public boolean canTargetEntity(@Nullable Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        LivingEntity livingEntity = (LivingEntity)entity;
        if (this.level != entity.level) return false;
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)) return false;
        if (this.isAlliedTo(entity)) return false;
        if (livingEntity.getType() == EntityType.ARMOR_STAND) return false;
        if (livingEntity.getType() == EntityType.WARDEN) return false;
        if (livingEntity.isInvulnerable()) return false;
        if (livingEntity.isDeadOrDying()) return false;
        if (!this.level.getWorldBorder().isWithinBounds(livingEntity.getBoundingBox())) return false;
        return true;
    }

    public static void applyDarknessAround(ServerLevel serverLevel, Vec3 vec3, @Nullable Entity entity, int i) {
        MobEffectInstance mobEffectInstance = new MobEffectInstance(MobEffects.DARKNESS, 260, 0, false, false);
        MobEffectUtil.addEffectToPlayersAround(serverLevel, entity, vec3, i, mobEffectInstance, 200);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        AngerManagement.codec(this::canTargetEntity).encodeStart(NbtOps.INSTANCE, this.angerManagement).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("anger", (Tag)tag));
        VibrationListener.codec(this).encodeStart(NbtOps.INSTANCE, this.dynamicGameEventListener.getListener()).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("listener", (Tag)tag));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("anger")) {
            AngerManagement.codec(this::canTargetEntity).parse(new Dynamic<Tag>(NbtOps.INSTANCE, compoundTag.get("anger"))).resultOrPartial(LOGGER::error).ifPresent(angerManagement -> {
                this.angerManagement = angerManagement;
            });
            this.syncClientAngerLevel();
        }
        if (compoundTag.contains("listener", 10)) {
            VibrationListener.codec(this).parse(new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag.getCompound("listener"))).resultOrPartial(LOGGER::error).ifPresent(vibrationListener -> this.dynamicGameEventListener.updateListener((VibrationListener)vibrationListener, this.level));
        }
    }

    private void playListeningSound() {
        if (!this.hasPose(Pose.ROARING)) {
            this.playSound(this.getAngerLevel().getListeningSound(), 10.0f, this.getVoicePitch());
        }
    }

    public AngerLevel getAngerLevel() {
        return AngerLevel.byAnger(this.getActiveAnger());
    }

    private int getActiveAnger() {
        return this.angerManagement.getActiveAnger(this.getTarget());
    }

    public void clearAnger(Entity entity) {
        this.angerManagement.clearAnger(entity);
    }

    public void increaseAngerAt(@Nullable Entity entity) {
        this.increaseAngerAt(entity, 35, true);
    }

    @VisibleForTesting
    public void increaseAngerAt(@Nullable Entity entity, int i, boolean bl) {
        if (!this.isNoAi() && this.canTargetEntity(entity)) {
            WardenAi.setDigCooldown(this);
            boolean bl2 = !(this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) instanceof Player);
            int j = this.angerManagement.increaseAnger(entity, i);
            if (entity instanceof Player && bl2 && AngerLevel.byAnger(j).isAngry()) {
                this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }
            if (bl) {
                this.playListeningSound();
            }
        }
    }

    public Optional<LivingEntity> getEntityAngryAt() {
        if (this.getAngerLevel().isAngry()) {
            return this.angerManagement.getActiveEntity();
        }
        return Optional.empty();
    }

    @Override
    @Nullable
    public LivingEntity getTarget() {
        return this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        this.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200L);
        if (mobSpawnType == MobSpawnType.TRIGGERED) {
            this.setPose(Pose.EMERGING);
            this.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, Unit.INSTANCE, WardenAi.EMERGE_DURATION);
            this.playSound(SoundEvents.WARDEN_AGITATED, 5.0f, 1.0f);
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        boolean bl = super.hurt(damageSource, f);
        if (!(this.level.isClientSide || this.isNoAi() || this.isDiggingOrEmerging())) {
            Entity entity = damageSource.getEntity();
            this.increaseAngerAt(entity, AngerLevel.ANGRY.getMinimumAnger() + 20, false);
            if (this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty() && entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity;
                if (!damageSource.isIndirect() || this.closerThan(livingEntity, 5.0)) {
                    this.setAttackTarget(livingEntity);
                }
            }
        }
        return bl;
    }

    public void setAttackTarget(LivingEntity livingEntity) {
        this.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
        this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, livingEntity);
        this.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        SonicBoom.setCooldown(this, 200);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions entityDimensions = super.getDimensions(pose);
        if (this.isDiggingOrEmerging()) {
            return EntityDimensions.fixed(entityDimensions.width, 1.0f);
        }
        return entityDimensions;
    }

    @Override
    public boolean isPushable() {
        return !this.isDiggingOrEmerging() && super.isPushable();
    }

    @Override
    protected void doPush(Entity entity) {
        if (!this.isNoAi() && !this.getBrain().hasMemoryValue(MemoryModuleType.TOUCH_COOLDOWN)) {
            this.getBrain().setMemoryWithExpiry(MemoryModuleType.TOUCH_COOLDOWN, Unit.INSTANCE, 20L);
            this.increaseAngerAt(entity);
            WardenAi.setDisturbanceLocation(this, entity.blockPosition());
        }
        super.doPush(entity);
    }

    @Override
    public boolean shouldListen(ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, GameEvent.Context context) {
        LivingEntity livingEntity;
        if (this.isNoAi() || this.isDeadOrDying() || this.getBrain().hasMemoryValue(MemoryModuleType.VIBRATION_COOLDOWN) || this.isDiggingOrEmerging() || !serverLevel.getWorldBorder().isWithinBounds(blockPos)) {
            return false;
        }
        Entity entity = context.sourceEntity();
        return !(entity instanceof LivingEntity) || this.canTargetEntity(livingEntity = (LivingEntity)entity);
    }

    @Override
    public void onSignalReceive(ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity, @Nullable Entity entity2, float f) {
        if (this.isDeadOrDying()) {
            return;
        }
        this.brain.setMemoryWithExpiry(MemoryModuleType.VIBRATION_COOLDOWN, Unit.INSTANCE, 40L);
        serverLevel.broadcastEntityEvent(this, (byte)61);
        this.playSound(SoundEvents.WARDEN_TENDRIL_CLICKS, 5.0f, this.getVoicePitch());
        BlockPos blockPos2 = blockPos;
        if (entity2 != null) {
            if (this.closerThan(entity2, 30.0)) {
                if (this.getBrain().hasMemoryValue(MemoryModuleType.RECENT_PROJECTILE)) {
                    if (this.canTargetEntity(entity2)) {
                        blockPos2 = entity2.blockPosition();
                    }
                    this.increaseAngerAt(entity2);
                } else {
                    this.increaseAngerAt(entity2, 10, true);
                }
            }
            this.getBrain().setMemoryWithExpiry(MemoryModuleType.RECENT_PROJECTILE, Unit.INSTANCE, 100L);
        } else {
            this.increaseAngerAt(entity);
        }
        if (!this.getAngerLevel().isAngry()) {
            Optional<LivingEntity> optional = this.angerManagement.getActiveEntity();
            if (entity2 != null || optional.isEmpty() || optional.get() == entity) {
                WardenAi.setDisturbanceLocation(this, blockPos2);
            }
        }
    }

    @VisibleForTesting
    public AngerManagement getAngerManagement() {
        return this.angerManagement;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level){

            @Override
            protected PathFinder createPathFinder(int i) {
                this.nodeEvaluator = new WalkNodeEvaluator();
                this.nodeEvaluator.setCanPassDoors(true);
                return new PathFinder(this.nodeEvaluator, i){

                    @Override
                    protected float distance(Node node, Node node2) {
                        return node.distanceToXZ(node2);
                    }
                };
            }
        };
    }
}

