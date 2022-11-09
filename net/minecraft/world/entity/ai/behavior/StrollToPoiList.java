/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import org.apache.commons.lang3.mutable.MutableLong;

public class StrollToPoiList {
    public static BehaviorControl<Villager> create(MemoryModuleType<List<GlobalPos>> memoryModuleType, float f, int i, int j, MemoryModuleType<GlobalPos> memoryModuleType2) {
        MutableLong mutableLong = new MutableLong(0L);
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.WALK_TARGET), instance.present(memoryModuleType), instance.present(memoryModuleType2)).apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, villager, l) -> {
            List list = (List)instance.get(memoryAccessor2);
            GlobalPos globalPos = (GlobalPos)instance.get(memoryAccessor3);
            if (list.isEmpty()) {
                return false;
            }
            GlobalPos globalPos2 = (GlobalPos)list.get(serverLevel.getRandom().nextInt(list.size()));
            if (globalPos2 == null || serverLevel.dimension() != globalPos2.dimension() || !globalPos.pos().closerToCenterThan(villager.position(), j)) {
                return false;
            }
            if (l > mutableLong.getValue()) {
                memoryAccessor.set(new WalkTarget(globalPos2.pos(), f, i));
                mutableLong.setValue(l + 100L);
            }
            return true;
        }));
    }
}

