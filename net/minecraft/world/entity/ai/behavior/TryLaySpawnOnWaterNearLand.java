/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class TryLaySpawnOnWaterNearLand
extends Behavior<Frog> {
    private final Block spawnBlock;
    private final MemoryModuleType<?> memoryModule;

    public TryLaySpawnOnWaterNearLand(Block block, MemoryModuleType<?> memoryModuleType) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.IS_PREGNANT, MemoryStatus.VALUE_PRESENT));
        this.spawnBlock = block;
        this.memoryModule = memoryModuleType;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Frog frog) {
        return !frog.isInWater() && frog.isOnGround();
    }

    @Override
    protected void start(ServerLevel serverLevel, Frog frog, long l) {
        BlockPos blockPos = frog.blockPosition().below();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos3;
            BlockPos blockPos2 = blockPos.relative(direction);
            if (!serverLevel.getBlockState(blockPos2).is(Blocks.WATER) || !serverLevel.getBlockState(blockPos3 = blockPos2.above()).isAir()) continue;
            serverLevel.setBlock(blockPos3, this.spawnBlock.defaultBlockState(), 3);
            serverLevel.playSound(null, frog, SoundEvents.FROG_LAY_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
            frog.getBrain().eraseMemory(this.memoryModule);
            return;
        }
    }
}

