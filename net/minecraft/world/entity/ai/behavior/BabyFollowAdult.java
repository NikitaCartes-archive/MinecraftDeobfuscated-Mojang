/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class BabyFollowAdult<E extends AgeableMob>
extends Behavior<E> {
    private final UniformInt followRange;
    private final Function<LivingEntity, Float> speedModifier;

    public BabyFollowAdult(UniformInt uniformInt, float f) {
        this(uniformInt, livingEntity -> Float.valueOf(f));
    }

    public BabyFollowAdult(UniformInt uniformInt, Function<LivingEntity, Float> function) {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.followRange = uniformInt;
        this.speedModifier = function;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E ageableMob) {
        if (!((AgeableMob)ageableMob).isBaby()) {
            return false;
        }
        AgeableMob ageableMob2 = this.getNearestAdult(ageableMob);
        return ((Entity)ageableMob).closerThan(ageableMob2, this.followRange.getMaxValue() + 1) && !((Entity)ageableMob).closerThan(ageableMob2, this.followRange.getMinValue());
    }

    @Override
    protected void start(ServerLevel serverLevel, E ageableMob, long l) {
        BehaviorUtils.setWalkAndLookTargetMemories(ageableMob, this.getNearestAdult(ageableMob), this.speedModifier.apply((LivingEntity)ageableMob).floatValue(), this.followRange.getMinValue() - 1);
    }

    private AgeableMob getNearestAdult(E ageableMob) {
        return ((LivingEntity)ageableMob).getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT).get();
    }
}

