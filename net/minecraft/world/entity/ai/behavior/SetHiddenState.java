/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.apache.commons.lang3.mutable.MutableInt;

public class SetHiddenState {
    private static final int HIDE_TIMEOUT = 300;

    public static BehaviorControl<LivingEntity> create(int i, int j) {
        int k = i * 20;
        MutableInt mutableInt = new MutableInt(0);
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.HIDING_PLACE), instance.present(MemoryModuleType.HEARD_BELL_TIME)).apply(instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
            boolean bl;
            long m = (Long)instance.get(memoryAccessor2);
            boolean bl2 = bl = m + 300L <= l;
            if (mutableInt.getValue() > k || bl) {
                memoryAccessor2.erase();
                memoryAccessor.erase();
                livingEntity.getBrain().updateActivityFromSchedule(serverLevel.getDayTime(), serverLevel.getGameTime());
                mutableInt.setValue(0);
                return true;
            }
            BlockPos blockPos = ((GlobalPos)instance.get(memoryAccessor)).pos();
            if (blockPos.closerThan(livingEntity.blockPosition(), j)) {
                mutableInt.increment();
            }
            return true;
        }));
    }
}

