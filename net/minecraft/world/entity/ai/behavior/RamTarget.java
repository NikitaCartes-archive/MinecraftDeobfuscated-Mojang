/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.Vec3;

public class RamTarget<E extends PathfinderMob>
extends Behavior<E> {
    public static final int TIME_OUT_DURATION = 200;
    public static final float RAM_SPEED_FORCE_FACTOR = 1.65f;
    private final UniformInt timeBetweenRams;
    private final TargetingConditions targeting;
    private final Function<E, Integer> getDamage;
    private final float speed;
    private final Function<E, Float> getKnockbackForce;
    private Vec3 ramDirection;
    private final Function<E, SoundEvent> getImpactSound;

    public RamTarget(UniformInt uniformInt, TargetingConditions targetingConditions, Function<E, Integer> function, float f, Function<E, Float> function2, Function<E, SoundEvent> function3) {
        super(ImmutableMap.of(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT, MemoryModuleType.RAM_TARGET, MemoryStatus.VALUE_PRESENT), 200);
        this.timeBetweenRams = uniformInt;
        this.targeting = targetingConditions;
        this.getDamage = function;
        this.speed = f;
        this.getKnockbackForce = function2;
        this.getImpactSound = function3;
        this.ramDirection = Vec3.ZERO;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        return pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.RAM_TARGET);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        return pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.RAM_TARGET);
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        BlockPos blockPos = pathfinderMob.blockPosition();
        Brain<?> brain = pathfinderMob.getBrain();
        Vec3 vec3 = brain.getMemory(MemoryModuleType.RAM_TARGET).get();
        this.ramDirection = new Vec3((double)blockPos.getX() - vec3.x(), 0.0, (double)blockPos.getZ() - vec3.z()).normalize();
        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speed, 0));
    }

    @Override
    protected void tick(ServerLevel serverLevel, E pathfinderMob, long l) {
        List<LivingEntity> list = serverLevel.getNearbyEntities(LivingEntity.class, this.targeting, (LivingEntity)pathfinderMob, ((Entity)pathfinderMob).getBoundingBox());
        Brain<?> brain = ((LivingEntity)pathfinderMob).getBrain();
        if (!list.isEmpty()) {
            LivingEntity livingEntity = list.get(0);
            livingEntity.hurt(DamageSource.mobAttack(pathfinderMob), this.getDamage.apply(pathfinderMob).intValue());
            float f = livingEntity.isDamageSourceBlocked(DamageSource.mobAttack(pathfinderMob)) ? 0.5f : 1.0f;
            float g = Mth.clamp(((LivingEntity)pathfinderMob).getSpeed() * 1.65f, 0.2f, 3.0f);
            livingEntity.knockback(f * g * this.getKnockbackForce.apply(pathfinderMob).floatValue(), this.ramDirection.x(), this.ramDirection.z());
            this.finishRam(serverLevel, (PathfinderMob)pathfinderMob);
            serverLevel.playSound(null, (Entity)pathfinderMob, this.getImpactSound.apply(pathfinderMob), SoundSource.HOSTILE, 1.0f, 1.0f);
        } else {
            boolean bl;
            Optional<WalkTarget> optional = brain.getMemory(MemoryModuleType.WALK_TARGET);
            Optional<Vec3> optional2 = brain.getMemory(MemoryModuleType.RAM_TARGET);
            boolean bl2 = bl = !optional.isPresent() || !optional2.isPresent() || optional.get().getTarget().currentPosition().distanceTo(optional2.get()) < 0.25;
            if (bl) {
                this.finishRam(serverLevel, (PathfinderMob)pathfinderMob);
            }
        }
    }

    protected void finishRam(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        serverLevel.broadcastEntityEvent(pathfinderMob, (byte)59);
        pathfinderMob.getBrain().setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, this.timeBetweenRams.sample(serverLevel.random));
        pathfinderMob.getBrain().eraseMemory(MemoryModuleType.RAM_TARGET);
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (E)((PathfinderMob)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (PathfinderMob)livingEntity, l);
    }
}

