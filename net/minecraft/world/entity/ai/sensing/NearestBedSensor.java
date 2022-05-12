/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.pathfinder.Path;

public class NearestBedSensor
extends Sensor<Mob> {
    private static final int CACHE_TIMEOUT = 40;
    private static final int BATCH_SIZE = 5;
    private static final int RATE = 20;
    private final Long2LongMap batchCache = new Long2LongOpenHashMap();
    private int triedCount;
    private long lastUpdate;

    public NearestBedSensor() {
        super(20);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_BED);
    }

    @Override
    protected void doTick(ServerLevel serverLevel, Mob mob) {
        Predicate<BlockPos> predicate;
        if (!mob.isBaby()) {
            return;
        }
        this.triedCount = 0;
        this.lastUpdate = serverLevel.getGameTime() + (long)serverLevel.getRandom().nextInt(20);
        PoiManager poiManager = serverLevel.getPoiManager();
        Set<Pair<Holder<PoiType>, BlockPos>> set = poiManager.findAllWithType(holder -> holder.is(PoiTypes.HOME), predicate = blockPos -> {
            long l = blockPos.asLong();
            if (this.batchCache.containsKey(l)) {
                return false;
            }
            if (++this.triedCount >= 5) {
                return false;
            }
            this.batchCache.put(l, this.lastUpdate + 40L);
            return true;
        }, mob.blockPosition(), 48, PoiManager.Occupancy.ANY).collect(Collectors.toSet());
        Path path = AcquirePoi.findPathToPois(mob, set);
        if (path != null && path.canReach()) {
            BlockPos blockPos2 = path.getTarget();
            Optional<Holder<PoiType>> optional = poiManager.getType(blockPos2);
            if (optional.isPresent()) {
                mob.getBrain().setMemory(MemoryModuleType.NEAREST_BED, blockPos2);
            }
        } else if (this.triedCount < 5) {
            this.batchCache.long2LongEntrySet().removeIf(entry -> entry.getLongValue() < this.lastUpdate);
        }
    }
}

