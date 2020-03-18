/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InsideBrownianWalk
extends Behavior<PathfinderMob> {
    private final float speedModifier;

    public InsideBrownianWalk(float f) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.speedModifier = f;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        return !serverLevel.canSeeSky(pathfinderMob.blockPosition());
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        BlockPos blockPos2 = pathfinderMob.blockPosition();
        List list = BlockPos.betweenClosedStream(blockPos2.offset(-1, -1, -1), blockPos2.offset(1, 1, 1)).map(BlockPos::immutable).collect(Collectors.toList());
        Collections.shuffle(list);
        Optional<BlockPos> optional = list.stream().filter(blockPos -> !serverLevel.canSeeSky((BlockPos)blockPos)).filter(blockPos -> serverLevel.loadedAndEntityCanStandOn((BlockPos)blockPos, pathfinderMob)).filter(blockPos -> serverLevel.noCollision(pathfinderMob)).findFirst();
        optional.ifPresent(blockPos -> pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget((BlockPos)blockPos, this.speedModifier, 0)));
    }
}

