/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.phys.Vec3;

public class ShootTongue
extends Behavior<Frog> {
    public static final int TIME_OUT_DURATION = 100;
    public static final int CATCH_ANIMATION_DURATION = 6;
    public static final int TONGUE_ANIMATION_DURATION = 10;
    private static final float EATING_DISTANCE = 1.75f;
    private static final float EATING_MOVEMENT_FACTOR = 0.75f;
    private int eatAnimationTimer;
    private int calculatePathCounter;
    private final SoundEvent tongueSound;
    private final SoundEvent eatSound;
    private Vec3 itemSpawnPos;
    private State state = State.DONE;

    public ShootTongue(SoundEvent soundEvent, SoundEvent soundEvent2) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 100);
        this.tongueSound = soundEvent;
        this.eatSound = soundEvent2;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Frog frog) {
        return super.checkExtraStartConditions(serverLevel, frog) && Frog.canEat(frog.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get()) && frog.getPose() != Pose.CROAKING;
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Frog frog, long l) {
        return frog.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.state != State.DONE;
    }

    @Override
    protected void start(ServerLevel serverLevel, Frog frog, long l) {
        LivingEntity livingEntity = frog.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        BehaviorUtils.lookAtEntity(frog, livingEntity);
        frog.setTongueTarget(livingEntity);
        frog.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(livingEntity.position(), 2.0f, 0));
        this.calculatePathCounter = 10;
        this.state = State.MOVE_TO_TARGET;
    }

    @Override
    protected void stop(ServerLevel serverLevel, Frog frog, long l) {
        frog.setPose(Pose.STANDING);
    }

    private void eatEntity(ServerLevel serverLevel, Frog frog) {
        Entity entity;
        serverLevel.playSound(null, frog, this.eatSound, SoundSource.NEUTRAL, 2.0f, 1.0f);
        Optional<Entity> optional = frog.getTongueTarget();
        if (optional.isPresent() && (entity = optional.get()).isAlive()) {
            frog.doHurtTarget(entity);
            if (!entity.isAlive()) {
                entity.remove(Entity.RemovalReason.KILLED);
            }
        }
        frog.eraseTongueTarget();
    }

    @Override
    protected void tick(ServerLevel serverLevel, Frog frog, long l) {
        LivingEntity livingEntity = frog.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        frog.setTongueTarget(livingEntity);
        switch (this.state) {
            case MOVE_TO_TARGET: {
                if (livingEntity.distanceTo(frog) < 1.75f) {
                    serverLevel.playSound(null, frog, this.tongueSound, SoundSource.NEUTRAL, 2.0f, 1.0f);
                    frog.setPose(Pose.USING_TONGUE);
                    livingEntity.setDeltaMovement(livingEntity.position().vectorTo(frog.position()).normalize().scale(0.75));
                    this.itemSpawnPos = livingEntity.position();
                    this.eatAnimationTimer = 0;
                    this.state = State.CATCH_ANIMATION;
                    break;
                }
                if (this.calculatePathCounter <= 0) {
                    frog.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(livingEntity.position(), 2.0f, 0));
                    this.calculatePathCounter = 10;
                    break;
                }
                --this.calculatePathCounter;
                break;
            }
            case CATCH_ANIMATION: {
                if (this.eatAnimationTimer++ < 6) break;
                this.state = State.EAT_ANIMATION;
                this.eatEntity(serverLevel, frog);
                break;
            }
            case EAT_ANIMATION: {
                if (this.eatAnimationTimer >= 10) {
                    this.state = State.DONE;
                    break;
                }
                ++this.eatAnimationTimer;
                break;
            }
        }
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Frog)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Frog)livingEntity, l);
    }

    static enum State {
        MOVE_TO_TARGET,
        CATCH_ANIMATION,
        EAT_ANIMATION,
        DONE;

    }
}

