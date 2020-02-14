/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;

public class SetClosestHomeAsWalkTarget
extends Behavior<LivingEntity> {
    private final float speed;
    private final Long2LongMap batchCache = new Long2LongOpenHashMap();
    private int triedCount;
    private long lastUpdate;

    public SetClosestHomeAsWalkTarget(float f) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.HOME, MemoryStatus.VALUE_ABSENT));
        this.speed = f;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
        if (serverLevel.getGameTime() - this.lastUpdate < 20L) {
            return false;
        }
        PathfinderMob pathfinderMob = (PathfinderMob)livingEntity;
        PoiManager poiManager = serverLevel.getPoiManager();
        Optional<BlockPos> optional = poiManager.findClosest(PoiType.HOME.getPredicate(), new BlockPos(livingEntity), 48, PoiManager.Occupancy.ANY);
        return optional.isPresent() && !(optional.get().distSqr(new BlockPos(pathfinderMob)) <= 4.0);
    }

    @Override
    protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.triedCount = 0;
        this.lastUpdate = serverLevel.getGameTime() + (long)serverLevel.getRandom().nextInt(20);
        PathfinderMob pathfinderMob = (PathfinderMob)livingEntity;
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
        Stream<BlockPos> stream = poiManager.findAll(PoiType.HOME.getPredicate(), predicate, new BlockPos(livingEntity), 48, PoiManager.Occupancy.ANY);
        Path path = pathfinderMob.getNavigation().createPath(stream, PoiType.HOME.getValidRange());
        if (path != null && path.canReach()) {
            BlockPos blockPos2 = path.getTarget();
            Optional<PoiType> optional = poiManager.getType(blockPos2);
            if (optional.isPresent()) {
                livingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockPos2, this.speed, 1));
                DebugPackets.sendPoiTicketCountPacket(serverLevel, blockPos2);
            }
        } else if (this.triedCount < 5) {
            this.batchCache.long2LongEntrySet().removeIf(entry -> entry.getLongValue() < this.lastUpdate);
        }
    }
}

