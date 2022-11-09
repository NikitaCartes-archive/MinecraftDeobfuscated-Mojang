/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

public class SetClosestHomeAsWalkTarget {
    private static final int CACHE_TIMEOUT = 40;
    private static final int BATCH_SIZE = 5;
    private static final int RATE = 20;
    private static final int OK_DISTANCE_SQR = 4;

    public static BehaviorControl<PathfinderMob> create(float f) {
        Long2LongOpenHashMap long2LongMap = new Long2LongOpenHashMap();
        MutableLong mutableLong = new MutableLong(0L);
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(MemoryModuleType.WALK_TARGET), instance.absent(MemoryModuleType.HOME)).apply(instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, pathfinderMob, l) -> {
            if (serverLevel.getGameTime() - mutableLong.getValue() < 20L) {
                return false;
            }
            PoiManager poiManager = serverLevel.getPoiManager();
            Optional<BlockPos> optional = poiManager.findClosest(holder -> holder.is(PoiTypes.HOME), pathfinderMob.blockPosition(), 48, PoiManager.Occupancy.ANY);
            if (optional.isEmpty() || optional.get().distSqr(pathfinderMob.blockPosition()) <= 4.0) {
                return false;
            }
            MutableInt mutableInt = new MutableInt(0);
            mutableLong.setValue(serverLevel.getGameTime() + (long)serverLevel.getRandom().nextInt(20));
            Predicate<BlockPos> predicate = blockPos -> {
                long l = blockPos.asLong();
                if (long2LongMap.containsKey(l)) {
                    return false;
                }
                if (mutableInt.incrementAndGet() >= 5) {
                    return false;
                }
                long2LongMap.put(l, mutableLong.getValue() + 40L);
                return true;
            };
            Set<Pair<Holder<PoiType>, BlockPos>> set = poiManager.findAllWithType(holder -> holder.is(PoiTypes.HOME), predicate, pathfinderMob.blockPosition(), 48, PoiManager.Occupancy.ANY).collect(Collectors.toSet());
            Path path = AcquirePoi.findPathToPois(pathfinderMob, set);
            if (path != null && path.canReach()) {
                BlockPos blockPos2 = path.getTarget();
                Optional<Holder<PoiType>> optional2 = poiManager.getType(blockPos2);
                if (optional2.isPresent()) {
                    memoryAccessor.set(new WalkTarget(blockPos2, f, 1));
                    DebugPackets.sendPoiTicketCountPacket(serverLevel, blockPos2);
                }
            } else if (mutableInt.getValue() < 5) {
                long2LongMap.long2LongEntrySet().removeIf(entry -> entry.getLongValue() < mutableLong.getValue());
            }
            return true;
        }));
    }
}

