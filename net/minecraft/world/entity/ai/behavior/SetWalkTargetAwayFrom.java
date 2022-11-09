/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetAwayFrom {
    public static BehaviorControl<PathfinderMob> pos(MemoryModuleType<BlockPos> memoryModuleType, float f, int i, boolean bl) {
        return SetWalkTargetAwayFrom.create(memoryModuleType, f, i, bl, Vec3::atBottomCenterOf);
    }

    public static OneShot<PathfinderMob> entity(MemoryModuleType<? extends Entity> memoryModuleType, float f, int i, boolean bl) {
        return SetWalkTargetAwayFrom.create(memoryModuleType, f, i, bl, Entity::position);
    }

    private static <T> OneShot<PathfinderMob> create(MemoryModuleType<T> memoryModuleType, float f, int i, boolean bl, Function<T, Vec3> function) {
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.WALK_TARGET), instance.present(memoryModuleType)).apply(instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, pathfinderMob, l) -> {
            Vec3 vec34;
            Vec3 vec33;
            Vec3 vec32;
            Optional optional = instance.tryGet(memoryAccessor);
            if (optional.isPresent() && !bl) {
                return false;
            }
            Vec3 vec3 = pathfinderMob.position();
            if (!vec3.closerThan(vec32 = (Vec3)function.apply(instance.get(memoryAccessor2)), i)) {
                return false;
            }
            if (optional.isPresent() && ((WalkTarget)optional.get()).getSpeedModifier() == f && (vec33 = ((WalkTarget)optional.get()).getTarget().currentPosition().subtract(vec3)).dot(vec34 = vec32.subtract(vec3)) < 0.0) {
                return false;
            }
            for (int j = 0; j < 10; ++j) {
                vec34 = LandRandomPos.getPosAway(pathfinderMob, 16, 7, vec32);
                if (vec34 == null) continue;
                memoryAccessor.set(new WalkTarget(vec34, f, 0));
                break;
            }
            return true;
        }));
    }
}

