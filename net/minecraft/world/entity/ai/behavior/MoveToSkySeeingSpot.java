/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MoveToSkySeeingSpot {
    public static OneShot<LivingEntity> create(float f) {
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(MemoryModuleType.WALK_TARGET)).apply(instance, memoryAccessor -> (serverLevel, livingEntity, l) -> {
            if (serverLevel.canSeeSky(livingEntity.blockPosition())) {
                return false;
            }
            Optional<Vec3> optional = Optional.ofNullable(MoveToSkySeeingSpot.getOutdoorPosition(serverLevel, livingEntity));
            optional.ifPresent(vec3 -> memoryAccessor.set(new WalkTarget((Vec3)vec3, f, 0)));
            return true;
        }));
    }

    @Nullable
    private static Vec3 getOutdoorPosition(ServerLevel serverLevel, LivingEntity livingEntity) {
        RandomSource randomSource = livingEntity.getRandom();
        BlockPos blockPos = livingEntity.blockPosition();
        for (int i = 0; i < 10; ++i) {
            BlockPos blockPos2 = blockPos.offset(randomSource.nextInt(20) - 10, randomSource.nextInt(6) - 3, randomSource.nextInt(20) - 10);
            if (!MoveToSkySeeingSpot.hasNoBlocksAbove(serverLevel, livingEntity, blockPos2)) continue;
            return Vec3.atBottomCenterOf(blockPos2);
        }
        return null;
    }

    public static boolean hasNoBlocksAbove(ServerLevel serverLevel, LivingEntity livingEntity, BlockPos blockPos) {
        return serverLevel.canSeeSky(blockPos) && (double)serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() <= livingEntity.getY();
    }
}

