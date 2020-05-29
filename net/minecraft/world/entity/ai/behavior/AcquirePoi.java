/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;

public class AcquirePoi
extends Behavior<PathfinderMob> {
    private final PoiType poiType;
    private final MemoryModuleType<GlobalPos> memoryToAcquire;
    private final boolean onlyIfAdult;
    private long lastUpdate;
    private final Long2LongMap batchCache = new Long2LongOpenHashMap();
    private int triedCount;

    public AcquirePoi(PoiType poiType, MemoryModuleType<GlobalPos> memoryModuleType, MemoryModuleType<GlobalPos> memoryModuleType2, boolean bl) {
        super(AcquirePoi.constructEntryConditionMap(memoryModuleType, memoryModuleType2));
        this.poiType = poiType;
        this.memoryToAcquire = memoryModuleType2;
        this.onlyIfAdult = bl;
    }

    public AcquirePoi(PoiType poiType, MemoryModuleType<GlobalPos> memoryModuleType, boolean bl) {
        this(poiType, memoryModuleType, memoryModuleType, bl);
    }

    private static ImmutableMap<MemoryModuleType<?>, MemoryStatus> constructEntryConditionMap(MemoryModuleType<GlobalPos> memoryModuleType, MemoryModuleType<GlobalPos> memoryModuleType2) {
        ImmutableMap.Builder<MemoryModuleType<GlobalPos>, MemoryStatus> builder = ImmutableMap.builder();
        builder.put(memoryModuleType, MemoryStatus.VALUE_ABSENT);
        if (memoryModuleType2 != memoryModuleType) {
            builder.put(memoryModuleType2, MemoryStatus.VALUE_ABSENT);
        }
        return builder.build();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        if (this.onlyIfAdult && pathfinderMob.isBaby()) {
            return false;
        }
        return serverLevel.getGameTime() - this.lastUpdate >= 20L;
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        this.triedCount = 0;
        this.lastUpdate = serverLevel.getGameTime() + (long)serverLevel.getRandom().nextInt(20);
        PoiManager poiManager = serverLevel.getPoiManager();
        Predicate<BlockPos> predicate = blockPos -> {
            long l = blockPos.asLong();
            if (this.batchCache.containsKey(l)) {
                return false;
            }
            if (++this.triedCount >= 5) {
                return false;
            }
            this.batchCache.put(l, this.lastUpdate + 40L);
            return true;
        };
        Stream<BlockPos> stream = poiManager.findAll(this.poiType.getPredicate(), predicate, pathfinderMob.blockPosition(), 48, PoiManager.Occupancy.HAS_SPACE);
        Path path = pathfinderMob.getNavigation().createPath(stream, this.poiType.getValidRange());
        if (path != null && path.canReach()) {
            BlockPos blockPos2 = path.getTarget();
            poiManager.getType(blockPos2).ifPresent(poiType -> {
                poiManager.take(this.poiType.getPredicate(), blockPos2 -> blockPos2.equals(blockPos2), blockPos2, 1);
                pathfinderMob.getBrain().setMemory(this.memoryToAcquire, GlobalPos.of(serverLevel.dimension(), blockPos2));
                DebugPackets.sendPoiTicketCountPacket(serverLevel, blockPos2);
            });
        } else if (this.triedCount < 5) {
            this.batchCache.long2LongEntrySet().removeIf(entry -> entry.getLongValue() < this.lastUpdate);
        }
    }
}

