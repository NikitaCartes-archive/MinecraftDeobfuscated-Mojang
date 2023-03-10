/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal.sniffer;

import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sniffer.SnifferAi;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Sniffer
extends Animal {
    private static final int DIGGING_PARTICLES_DELAY_TICKS = 1700;
    private static final int DIGGING_PARTICLES_DURATION_TICKS = 6000;
    private static final int DIGGING_PARTICLES_AMOUNT = 30;
    private static final int DIGGING_DROP_SEED_OFFSET_TICKS = 120;
    private static final int SNIFFING_PROXIMITY_DISTANCE = 10;
    private static final int SNIFFER_BABY_AGE_TICKS = 48000;
    private static final EntityDataAccessor<State> DATA_STATE = SynchedEntityData.defineId(Sniffer.class, EntityDataSerializers.SNIFFER_STATE);
    private static final EntityDataAccessor<Integer> DATA_DROP_SEED_AT_TICK = SynchedEntityData.defineId(Sniffer.class, EntityDataSerializers.INT);
    public final AnimationState walkingAnimationState = new AnimationState();
    public final AnimationState panicAnimationState = new AnimationState();
    public final AnimationState feelingHappyAnimationState = new AnimationState();
    public final AnimationState scentingAnimationState = new AnimationState();
    public final AnimationState sniffingAnimationState = new AnimationState();
    public final AnimationState searchingAnimationState = new AnimationState();
    public final AnimationState diggingAnimationState = new AnimationState();
    public final AnimationState risingAnimationState = new AnimationState();

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.1f).add(Attributes.MAX_HEALTH, 14.0);
    }

    public Sniffer(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.entityData.define(DATA_STATE, State.IDLING);
        this.entityData.define(DATA_DROP_SEED_AT_TICK, 0);
        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(BlockPathTypes.WATER, -2.0f);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return this.getDimensions((Pose)pose).height * 0.6f;
    }

    private boolean isMoving() {
        boolean bl = this.onGround || this.isInWaterOrBubble();
        return bl && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;
    }

    private boolean isMovingInWater() {
        return this.isMoving() && this.isInWater() && !this.isUnderWater() && this.getDeltaMovement().horizontalDistanceSqr() > 9.999999999999999E-6;
    }

    private boolean isMovingOnLand() {
        return this.isMoving() && !this.isUnderWater() && !this.isInWater();
    }

    public boolean isPanicking() {
        return this.brain.getMemory(MemoryModuleType.IS_PANICKING).isPresent();
    }

    public boolean canPlayDiggingSound() {
        return this.getState() == State.DIGGING || this.getState() == State.SEARCHING;
    }

    private BlockPos getHeadPosition() {
        Vec3 vec3 = this.position().add(this.getForward().scale(2.25));
        return BlockPos.containing(vec3.x(), this.getY(), vec3.z());
    }

    private State getState() {
        return this.entityData.get(DATA_STATE);
    }

    private Sniffer setState(State state) {
        this.entityData.set(DATA_STATE, state);
        return this;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_STATE.equals(entityDataAccessor)) {
            State state = this.getState();
            this.resetAnimations();
            switch (state) {
                case SCENTING: {
                    this.scentingAnimationState.startIfStopped(this.tickCount);
                    break;
                }
                case SNIFFING: {
                    this.sniffingAnimationState.startIfStopped(this.tickCount);
                    break;
                }
                case SEARCHING: {
                    this.searchingAnimationState.startIfStopped(this.tickCount);
                    break;
                }
                case DIGGING: {
                    this.diggingAnimationState.startIfStopped(this.tickCount);
                    break;
                }
                case RISING: {
                    this.risingAnimationState.startIfStopped(this.tickCount);
                    break;
                }
                case FEELING_HAPPY: {
                    this.feelingHappyAnimationState.startIfStopped(this.tickCount);
                }
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    private void resetAnimations() {
        this.searchingAnimationState.stop();
        this.diggingAnimationState.stop();
        this.sniffingAnimationState.stop();
        this.risingAnimationState.stop();
        this.feelingHappyAnimationState.stop();
        this.scentingAnimationState.stop();
    }

    public Sniffer transitionTo(State state) {
        switch (state) {
            case IDLING: {
                this.setState(State.IDLING);
                break;
            }
            case SCENTING: {
                this.playSound(SoundEvents.SNIFFER_SCENTING, 1.0f, 1.0f);
                this.setState(State.SCENTING);
                break;
            }
            case SNIFFING: {
                this.playSound(SoundEvents.SNIFFER_SNIFFING, 1.0f, 1.0f);
                this.setState(State.SNIFFING);
                break;
            }
            case SEARCHING: {
                this.setState(State.SEARCHING);
                break;
            }
            case DIGGING: {
                this.setState(State.DIGGING).onDiggingStart();
                break;
            }
            case RISING: {
                this.playSound(SoundEvents.SNIFFER_DIGGING_STOP, 1.0f, 1.0f);
                this.setState(State.RISING);
                break;
            }
            case FEELING_HAPPY: {
                this.playSound(SoundEvents.SNIFFER_HAPPY, 1.0f, 1.0f);
                this.setState(State.FEELING_HAPPY);
            }
        }
        return this;
    }

    private Sniffer onDiggingStart() {
        this.entityData.set(DATA_DROP_SEED_AT_TICK, this.tickCount + 120);
        this.level.broadcastEntityEvent(this, (byte)63);
        return this;
    }

    public Sniffer onDiggingComplete(boolean bl) {
        if (bl) {
            this.storeExploredPosition(this.getOnPos());
        }
        return this;
    }

    Optional<BlockPos> calculateDigPosition() {
        return IntStream.range(0, 5).mapToObj(i -> LandRandomPos.getPos(this, 10 + 2 * i, 3)).filter(Objects::nonNull).map(BlockPos::containing).map(BlockPos::below).filter(this::canDig).findFirst();
    }

    @Override
    protected boolean canRide(Entity entity) {
        return false;
    }

    boolean canDig() {
        return !this.isPanicking() && !this.isBaby() && !this.isInWater() && this.canDig(this.getHeadPosition().below());
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean canDig(BlockPos blockPos) {
        if (!this.level.getBlockState(blockPos).is(BlockTags.SNIFFER_DIGGABLE_BLOCK)) return false;
        if (!this.level.getBlockState(blockPos.above()).isAir()) return false;
        if (!this.getExploredPositions().noneMatch(blockPos::equals)) return false;
        return true;
    }

    private void dropSeed() {
        if (this.level.isClientSide() || this.entityData.get(DATA_DROP_SEED_AT_TICK) != this.tickCount) {
            return;
        }
        ItemStack itemStack = new ItemStack(Items.TORCHFLOWER_SEEDS);
        BlockPos blockPos = this.getHeadPosition();
        ItemEntity itemEntity = new ItemEntity(this.level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack);
        itemEntity.setDefaultPickUpDelay();
        this.level.addFreshEntity(itemEntity);
        this.playSound(SoundEvents.SNIFFER_DROP_SEED, 1.0f, 1.0f);
    }

    private Sniffer emitDiggingParticles(AnimationState animationState) {
        boolean bl;
        boolean bl2 = bl = animationState.getAccumulatedTime() > 1700L && animationState.getAccumulatedTime() < 6000L;
        if (bl) {
            BlockState blockState = this.getBlockStateOn();
            BlockPos blockPos = this.getHeadPosition();
            if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
                for (int i = 0; i < 30; ++i) {
                    Vec3 vec3 = Vec3.atCenterOf(blockPos);
                    this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), vec3.x, vec3.y, vec3.z, 0.0, 0.0, 0.0);
                }
                if (this.tickCount % 10 == 0) {
                    this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), blockState.getSoundType().getHitSound(), this.getSoundSource(), 0.5f, 0.5f, false);
                }
            }
        }
        return this;
    }

    private Sniffer storeExploredPosition(BlockPos blockPos) {
        List list = this.getExploredPositions().limit(20L).collect(Collectors.toList());
        list.add(0, blockPos);
        this.getBrain().setMemory(MemoryModuleType.SNIFFER_EXPLORED_POSITIONS, list);
        return this;
    }

    private Stream<BlockPos> getExploredPositions() {
        return this.getBrain().getMemory(MemoryModuleType.SNIFFER_EXPLORED_POSITIONS).stream().flatMap(Collection::stream);
    }

    @Override
    protected void jumpFromGround() {
        double e;
        super.jumpFromGround();
        double d = this.moveControl.getSpeedModifier();
        if (d > 0.0 && (e = this.getDeltaMovement().horizontalDistanceSqr()) < 0.01) {
            this.moveRelative(0.1f, new Vec3(0.0, 0.0, 1.0));
        }
    }

    @Override
    public void tick() {
        boolean bl = this.isInWater() && !this.isUnderWater();
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(bl ? (double)0.2f : (double)0.1f);
        if (this.isMovingOnLand() || this.isMovingInWater()) {
            if (this.isPanicking()) {
                this.walkingAnimationState.stop();
                this.panicAnimationState.startIfStopped(this.tickCount);
            } else {
                this.panicAnimationState.stop();
                this.walkingAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            this.panicAnimationState.stop();
            this.walkingAnimationState.stop();
        }
        switch (this.getState()) {
            case DIGGING: {
                this.emitDiggingParticles(this.diggingAnimationState).dropSeed();
                break;
            }
            case SEARCHING: {
                this.playSearchingSound();
            }
        }
        super.tick();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        InteractionResult interactionResult = super.mobInteract(player, interactionHand);
        if (interactionResult.consumesAction() && this.isFood(itemStack)) {
            this.level.playSound(null, this, this.getEatingSound(itemStack), SoundSource.NEUTRAL, 1.0f, Mth.randomBetween(this.level.random, 0.8f, 1.2f));
        }
        return interactionResult;
    }

    private void playSearchingSound() {
        if (this.level.isClientSide() && this.tickCount % 20 == 0) {
            this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.SNIFFER_SEARCHING, this.getSoundSource(), 1.0f, 1.0f, false);
        }
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.SNIFFER_STEP, 0.15f, 1.0f);
    }

    @Override
    public SoundEvent getEatingSound(ItemStack itemStack) {
        return SoundEvents.SNIFFER_EAT;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Set.of(State.DIGGING, State.SEARCHING).contains((Object)this.getState()) ? null : SoundEvents.SNIFFER_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SNIFFER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SNIFFER_DEATH;
    }

    @Override
    public int getMaxHeadYRot() {
        return 50;
    }

    @Override
    public void setBaby(boolean bl) {
        this.setAge(bl ? -48000 : 0);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityType.SNIFFER.create(serverLevel);
    }

    @Override
    public boolean canMate(Animal animal) {
        if (animal instanceof Sniffer) {
            Sniffer sniffer = (Sniffer)animal;
            Set<State> set = Set.of(State.IDLING, State.SCENTING, State.FEELING_HAPPY);
            return set.contains((Object)this.getState()) && set.contains((Object)sniffer.getState()) && super.canMate(animal);
        }
        return false;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return super.getBoundingBoxForCulling().inflate(0.6f);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.SNIFFER_FOOD);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return SnifferAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    public Brain<Sniffer> getBrain() {
        return super.getBrain();
    }

    protected Brain.Provider<Sniffer> brainProvider() {
        return Brain.provider(SnifferAi.MEMORY_TYPES, SnifferAi.SENSOR_TYPES);
    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("snifferBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().popPush("snifferActivityUpdate");
        SnifferAi.updateActivity(this);
        this.level.getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    public static enum State {
        IDLING,
        FEELING_HAPPY,
        SCENTING,
        SNIFFING,
        SEARCHING,
        DIGGING,
        RISING;

    }
}

