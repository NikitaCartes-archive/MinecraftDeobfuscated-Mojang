/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal.camel;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
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
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.RiderShieldingMount;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.camel.CamelAi;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Camel
extends AbstractHorse
implements PlayerRideableJumping,
RiderShieldingMount,
Saddleable {
    public static final Ingredient TEMPTATION_ITEM = Ingredient.of(Items.CACTUS);
    public static final int DASH_COOLDOWN_TICKS = 55;
    public static final int MAX_HEAD_Y_ROT = 30;
    private static final float RUNNING_SPEED_BONUS = 0.1f;
    private static final float DASH_VERTICAL_MOMENTUM = 1.4285f;
    private static final float DASH_HORIZONTAL_MOMENTUM = 22.2222f;
    private static final int SITDOWN_DURATION_TICKS = 40;
    private static final int STANDUP_DURATION_TICKS = 52;
    private static final int IDLE_MINIMAL_DURATION_TICKS = 80;
    private static final float SITTING_HEIGHT_DIFFERENCE = 1.43f;
    public static final EntityDataAccessor<Boolean> DASH = SynchedEntityData.defineId(Camel.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Long> LAST_POSE_CHANGE_TICK = SynchedEntityData.defineId(Camel.class, EntityDataSerializers.LONG);
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState sitPoseAnimationState = new AnimationState();
    public final AnimationState sitUpAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState dashAnimationState = new AnimationState();
    private static final EntityDimensions SITTING_DIMENSIONS = EntityDimensions.scalable(EntityType.CAMEL.getWidth(), EntityType.CAMEL.getHeight() - 1.43f);
    private int dashCooldown = 0;
    private int idleAnimationTimeout = 0;

    public Camel(EntityType<? extends Camel> entityType, Level level) {
        super((EntityType<? extends AbstractHorse>)entityType, level);
        this.setMaxUpStep(1.5f);
        this.moveControl = new CamelMoveControl();
        GroundPathNavigation groundPathNavigation = (GroundPathNavigation)this.getNavigation();
        groundPathNavigation.setCanFloat(true);
        groundPathNavigation.setCanWalkOverFences(true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putLong("LastPoseTick", this.entityData.get(LAST_POSE_CHANGE_TICK));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        long l = compoundTag.getLong("LastPoseTick");
        if (l < 0L) {
            this.setPose(Pose.SITTING);
        }
        this.resetLastPoseChangeTick(l);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Camel.createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 32.0).add(Attributes.MOVEMENT_SPEED, 0.09f).add(Attributes.JUMP_STRENGTH, 0.42f);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DASH, false);
        this.entityData.define(LAST_POSE_CHANGE_TICK, 0L);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        CamelAi.initMemories(this, serverLevelAccessor.getRandom());
        this.resetLastPoseChangeTickToFullStand(serverLevelAccessor.getLevel().getGameTime());
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    protected Brain.Provider<Camel> brainProvider() {
        return CamelAi.brainProvider();
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CamelAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return pose == Pose.SITTING ? SITTING_DIMENSIONS.scale(this.getScale()) : super.getDimensions(pose);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return entityDimensions.height - 0.1f;
    }

    @Override
    public double getRiderShieldingHeight() {
        return 0.5;
    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("camelBrain");
        Brain<?> brain = this.getBrain();
        brain.tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        this.level.getProfiler().push("camelActivityUpdate");
        CamelAi.updateActivity(this);
        this.level.getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isDashing() && this.dashCooldown < 55 && (this.onGround || this.isInWater())) {
            this.setDashing(false);
        }
        if (this.dashCooldown > 0) {
            --this.dashCooldown;
            if (this.dashCooldown == 0) {
                this.level.playSound(null, this.blockPosition(), SoundEvents.CAMEL_DASH_READY, SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
        }
        if (this.level.isClientSide()) {
            this.setupAnimationStates();
        }
        if (this.refuseToMove()) {
            this.clampHeadRotationToBody(this, 30.0f);
        }
    }

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
        if (this.isCamelVisuallySitting()) {
            this.sitUpAnimationState.stop();
            this.dashAnimationState.stop();
            if (this.isVisuallySittingDown()) {
                this.sitAnimationState.startIfStopped(this.tickCount);
                this.sitPoseAnimationState.stop();
            } else {
                this.sitAnimationState.stop();
                this.sitPoseAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            this.sitAnimationState.stop();
            this.sitPoseAnimationState.stop();
            this.dashAnimationState.animateWhen(this.isDashing(), this.tickCount);
            this.sitUpAnimationState.animateWhen(this.isInPoseTransition() && this.getPoseTime() >= 0L, this.tickCount);
        }
    }

    @Override
    protected void updateWalkAnimation(float f) {
        float g = this.getPose() == Pose.STANDING && !this.dashAnimationState.isStarted() ? Math.min(f * 6.0f, 1.0f) : 0.0f;
        this.walkAnimation.update(g, 0.2f);
    }

    @Override
    public void travel(Vec3 vec3) {
        if (this.refuseToMove() && this.isOnGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
            vec3 = vec3.multiply(0.0, 1.0, 0.0);
        }
        super.travel(vec3);
    }

    @Override
    protected void tickRidden(LivingEntity livingEntity, Vec3 vec3) {
        super.tickRidden(livingEntity, vec3);
        if (livingEntity.zza > 0.0f && this.isCamelSitting() && !this.isInPoseTransition()) {
            this.standUp();
        }
    }

    public boolean refuseToMove() {
        return this.isCamelSitting() || this.isInPoseTransition();
    }

    @Override
    protected float getRiddenSpeed(LivingEntity livingEntity) {
        float f = livingEntity.isSprinting() && this.getJumpCooldown() == 0 ? 0.1f : 0.0f;
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) + f;
    }

    @Override
    protected Vec2 getRiddenRotation(LivingEntity livingEntity) {
        if (this.refuseToMove()) {
            return new Vec2(this.getXRot(), this.getYRot());
        }
        return super.getRiddenRotation(livingEntity);
    }

    @Override
    protected Vec3 getRiddenInput(LivingEntity livingEntity, Vec3 vec3) {
        if (this.refuseToMove()) {
            return Vec3.ZERO;
        }
        return super.getRiddenInput(livingEntity, vec3);
    }

    @Override
    public boolean canJump() {
        return !this.refuseToMove() && super.canJump();
    }

    @Override
    public void onPlayerJump(int i) {
        if (!this.isSaddled() || this.dashCooldown > 0 || !this.isOnGround()) {
            return;
        }
        super.onPlayerJump(i);
    }

    @Override
    public boolean canSprint() {
        return true;
    }

    @Override
    protected void executeRidersJump(float f, Vec3 vec3) {
        double d = this.getAttributeValue(Attributes.JUMP_STRENGTH) * (double)this.getBlockJumpFactor() + this.getJumpBoostPower();
        this.addDeltaMovement(this.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale((double)(22.2222f * f) * this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double)this.getBlockSpeedFactor()).add(0.0, (double)(1.4285f * f) * d, 0.0));
        this.dashCooldown = 55;
        this.setDashing(true);
        this.hasImpulse = true;
    }

    public boolean isDashing() {
        return this.entityData.get(DASH);
    }

    public void setDashing(boolean bl) {
        this.entityData.set(DASH, bl);
    }

    public boolean isPanicking() {
        return this.getBrain().checkMemory(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_PRESENT);
    }

    @Override
    public void handleStartJump(int i) {
        this.playSound(SoundEvents.CAMEL_DASH, 1.0f, 1.0f);
        this.setDashing(true);
    }

    @Override
    public void handleStopJump() {
    }

    @Override
    public int getJumpCooldown() {
        return this.dashCooldown;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CAMEL_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CAMEL_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.CAMEL_HURT;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        if (blockState.getSoundType() == SoundType.SAND) {
            this.playSound(SoundEvents.CAMEL_STEP_SAND, 1.0f, 1.0f);
        } else {
            this.playSound(SoundEvents.CAMEL_STEP, 1.0f, 1.0f);
        }
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return TEMPTATION_ITEM.test(itemStack);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (player.isSecondaryUseActive()) {
            this.openCustomInventoryScreen(player);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        InteractionResult interactionResult = itemStack.interactLivingEntity(player, this, interactionHand);
        if (interactionResult.consumesAction()) {
            return interactionResult;
        }
        if (this.isFood(itemStack)) {
            return this.fedFood(player, itemStack);
        }
        if (this.getPassengers().size() < 2 && !this.isBaby()) {
            this.doPlayerRide(player);
        }
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }

    @Override
    protected void onLeashDistance(float f) {
        if (f > 6.0f && this.isCamelSitting() && !this.isInPoseTransition()) {
            this.standUp();
        }
    }

    @Override
    protected boolean handleEating(Player player, ItemStack itemStack) {
        boolean bl3;
        boolean bl2;
        boolean bl;
        if (!this.isFood(itemStack)) {
            return false;
        }
        boolean bl4 = bl = this.getHealth() < this.getMaxHealth();
        if (bl) {
            this.heal(2.0f);
        }
        boolean bl5 = bl2 = this.isTamed() && this.getAge() == 0 && this.canFallInLove();
        if (bl2) {
            this.setInLove(player);
        }
        if (bl3 = this.isBaby()) {
            this.level.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
            if (!this.level.isClientSide) {
                this.ageUp(10);
            }
        }
        if (bl || bl2 || bl3) {
            SoundEvent soundEvent;
            if (!this.isSilent() && (soundEvent = this.getEatingSound()) != null) {
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean canPerformRearing() {
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean canMate(Animal animal) {
        if (animal == this) return false;
        if (!(animal instanceof Camel)) return false;
        Camel camel = (Camel)animal;
        if (!this.canParent()) return false;
        if (!camel.canParent()) return false;
        return true;
    }

    @Override
    @Nullable
    public Camel getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityType.CAMEL.create(serverLevel);
    }

    @Override
    @Nullable
    protected SoundEvent getEatingSound() {
        return SoundEvents.CAMEL_EAT;
    }

    @Override
    protected void actuallyHurt(DamageSource damageSource, float f) {
        this.standUpPanic();
        super.actuallyHurt(damageSource, f);
    }

    @Override
    public void positionRider(Entity entity) {
        int i = this.getPassengers().indexOf(entity);
        if (i < 0) {
            return;
        }
        boolean bl = i == 0;
        float f = 0.5f;
        float g = (float)(this.isRemoved() ? (double)0.01f : this.getBodyAnchorAnimationYOffset(bl, 0.0f) + entity.getMyRidingOffset());
        if (this.getPassengers().size() > 1) {
            if (!bl) {
                f = -0.7f;
            }
            if (entity instanceof Animal) {
                f += 0.2f;
            }
        }
        Vec3 vec3 = new Vec3(0.0, 0.0, f).yRot(-this.yBodyRot * ((float)Math.PI / 180));
        entity.setPos(this.getX() + vec3.x, this.getY() + (double)g, this.getZ() + vec3.z);
        this.clampRotation(entity);
    }

    private double getBodyAnchorAnimationYOffset(boolean bl, float f) {
        double d = this.getPassengersRidingOffset();
        float g = this.getScale() * 1.43f;
        float h = g - this.getScale() * 0.2f;
        float i = g - h;
        boolean bl2 = this.isInPoseTransition();
        boolean bl3 = this.isCamelSitting();
        if (bl2) {
            float l;
            int k;
            int j;
            int n = j = bl3 ? 40 : 52;
            if (bl3) {
                k = 28;
                l = bl ? 0.5f : 0.1f;
            } else {
                k = bl ? 24 : 32;
                l = bl ? 0.6f : 0.35f;
            }
            float m = Mth.clamp((float)this.getPoseTime() + f, 0.0f, (float)j);
            boolean bl4 = m < (float)k;
            float n2 = bl4 ? m / (float)k : (m - (float)k) / (float)(j - k);
            float o = g - l * h;
            d += bl3 ? (double)Mth.lerp(n2, bl4 ? g : o, bl4 ? o : i) : (double)Mth.lerp(n2, bl4 ? i - g : i - o, bl4 ? i - o : 0.0f);
        }
        if (bl3 && !bl2) {
            d += (double)i;
        }
        return d;
    }

    @Override
    public Vec3 getLeashOffset(float f) {
        return new Vec3(0.0, this.getBodyAnchorAnimationYOffset(true, f) - (double)(0.2f * this.getScale()), this.getBbWidth() * 0.56f);
    }

    @Override
    public double getPassengersRidingOffset() {
        return this.getDimensions((Pose)(this.isCamelSitting() ? Pose.SITTING : Pose.STANDING)).height - (this.isBaby() ? 0.35f : 0.6f);
    }

    @Override
    public void onPassengerTurned(Entity entity) {
        if (this.getControllingPassenger() != entity) {
            this.clampRotation(entity);
        }
    }

    private void clampRotation(Entity entity) {
        entity.setYBodyRot(this.getYRot());
        float f = entity.getYRot();
        float g = Mth.wrapDegrees(f - this.getYRot());
        float h = Mth.clamp(g, -160.0f, 160.0f);
        entity.yRotO += h - g;
        float i = f + h - g;
        entity.setYRot(i);
        entity.setYHeadRot(i);
    }

    private void clampHeadRotationToBody(Entity entity, float f) {
        float g = entity.getYHeadRot();
        float h = Mth.wrapDegrees(this.yBodyRot - g);
        float i = Mth.clamp(Mth.wrapDegrees(this.yBodyRot - g), -f, f);
        float j = g + h - i;
        entity.setYHeadRot(j);
    }

    @Override
    public int getMaxHeadYRot() {
        return 30;
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return this.getPassengers().size() <= 2;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity entity;
        if (!this.getPassengers().isEmpty() && this.isSaddled() && (entity = this.getPassengers().get(0)) instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            return livingEntity;
        }
        return null;
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    public boolean isCamelSitting() {
        return this.entityData.get(LAST_POSE_CHANGE_TICK) < 0L;
    }

    public boolean isCamelVisuallySitting() {
        return this.getPoseTime() < 0L != this.isCamelSitting();
    }

    public boolean isInPoseTransition() {
        long l = this.getPoseTime();
        return l < (long)(this.isCamelSitting() ? 40 : 52);
    }

    private boolean isVisuallySittingDown() {
        return this.isCamelSitting() && this.getPoseTime() < 40L && this.getPoseTime() >= 0L;
    }

    public void sitDown() {
        if (this.isCamelSitting()) {
            return;
        }
        this.playSound(SoundEvents.CAMEL_SIT, 1.0f, 1.0f);
        this.setPose(Pose.SITTING);
        this.resetLastPoseChangeTick(-this.level.getGameTime());
    }

    public void standUp() {
        if (!this.isCamelSitting()) {
            return;
        }
        this.playSound(SoundEvents.CAMEL_STAND, 1.0f, 1.0f);
        this.setPose(Pose.STANDING);
        this.resetLastPoseChangeTick(this.level.getGameTime());
    }

    public void standUpPanic() {
        this.setPose(Pose.STANDING);
        this.resetLastPoseChangeTickToFullStand(this.level.getGameTime());
    }

    @VisibleForTesting
    public void resetLastPoseChangeTick(long l) {
        this.entityData.set(LAST_POSE_CHANGE_TICK, l);
    }

    private void resetLastPoseChangeTickToFullStand(long l) {
        this.resetLastPoseChangeTick(Math.max(0L, l - 52L - 1L));
    }

    public long getPoseTime() {
        return this.level.getGameTime() - Math.abs(this.entityData.get(LAST_POSE_CHANGE_TICK));
    }

    @Override
    public SoundEvent getSaddleSoundEvent() {
        return SoundEvents.CAMEL_SADDLE;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (!this.firstTick && DASH.equals(entityDataAccessor)) {
            this.dashCooldown = this.dashCooldown == 0 ? 55 : this.dashCooldown;
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new CamelBodyRotationControl(this);
    }

    @Override
    public boolean isTamed() {
        return true;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        if (!this.level.isClientSide) {
            player.openHorseInventory(this, this.inventory);
        }
    }

    @Override
    @Nullable
    public /* synthetic */ AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    class CamelMoveControl
    extends MoveControl {
        public CamelMoveControl() {
            super(Camel.this);
        }

        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO && !Camel.this.isLeashed() && Camel.this.isCamelSitting() && !Camel.this.isInPoseTransition()) {
                Camel.this.standUp();
            }
            super.tick();
        }
    }

    class CamelBodyRotationControl
    extends BodyRotationControl {
        public CamelBodyRotationControl(Camel camel2) {
            super(camel2);
        }

        @Override
        public void clientTick() {
            if (!Camel.this.refuseToMove()) {
                super.clientTick();
            }
        }
    }
}

