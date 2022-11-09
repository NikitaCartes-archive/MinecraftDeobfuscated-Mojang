/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ValidateNearbyPoi {
    private static final int MAX_DISTANCE = 16;

    public static BehaviorControl<LivingEntity> create(Predicate<Holder<PoiType>> predicate, MemoryModuleType<GlobalPos> memoryModuleType) {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(memoryModuleType)).apply(instance, memoryAccessor -> (serverLevel, livingEntity, l) -> {
            GlobalPos globalPos = (GlobalPos)instance.get(memoryAccessor);
            BlockPos blockPos = globalPos.pos();
            if (serverLevel.dimension() != globalPos.dimension() || !blockPos.closerToCenterThan(livingEntity.position(), 16.0)) {
                return false;
            }
            ServerLevel serverLevel2 = serverLevel.getServer().getLevel(globalPos.dimension());
            if (serverLevel2 == null || !serverLevel2.getPoiManager().exists(blockPos, predicate)) {
                memoryAccessor.erase();
            } else if (ValidateNearbyPoi.bedIsOccupied(serverLevel2, blockPos, livingEntity)) {
                memoryAccessor.erase();
                serverLevel.getPoiManager().release(blockPos);
                DebugPackets.sendPoiTicketCountPacket(serverLevel, blockPos);
            }
            return true;
        }));
    }

    private static boolean bedIsOccupied(ServerLevel serverLevel, BlockPos blockPos, LivingEntity livingEntity) {
        BlockState blockState = serverLevel.getBlockState(blockPos);
        return blockState.is(BlockTags.BEDS) && blockState.getValue(BedBlock.OCCUPIED) != false && !livingEntity.isSleeping();
    }
}

