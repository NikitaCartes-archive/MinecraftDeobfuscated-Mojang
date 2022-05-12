/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

public class AcquirePoi
extends Behavior<PathfinderMob> {
    private static final int BATCH_SIZE = 5;
    private static final int RATE = 20;
    public static final int SCAN_RANGE = 48;
    private final Predicate<Holder<PoiType>> poiType;
    private final MemoryModuleType<GlobalPos> memoryToAcquire;
    private final boolean onlyIfAdult;
    private final Optional<Byte> onPoiAcquisitionEvent;
    private long nextScheduledStart;
    private final Long2ObjectMap<JitteredLinearRetry> batchCache = new Long2ObjectOpenHashMap<JitteredLinearRetry>();

    public AcquirePoi(Predicate<Holder<PoiType>> predicate, MemoryModuleType<GlobalPos> memoryModuleType, MemoryModuleType<GlobalPos> memoryModuleType2, boolean bl, Optional<Byte> optional) {
        super(AcquirePoi.constructEntryConditionMap(memoryModuleType, memoryModuleType2));
        this.poiType = predicate;
        this.memoryToAcquire = memoryModuleType2;
        this.onlyIfAdult = bl;
        this.onPoiAcquisitionEvent = optional;
    }

    public AcquirePoi(Predicate<Holder<PoiType>> predicate, MemoryModuleType<GlobalPos> memoryModuleType, boolean bl, Optional<Byte> optional) {
        this(predicate, memoryModuleType, memoryModuleType, bl, optional);
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
        if (this.nextScheduledStart == 0L) {
            this.nextScheduledStart = pathfinderMob.level.getGameTime() + (long)serverLevel.random.nextInt(20);
            return false;
        }
        return serverLevel.getGameTime() >= this.nextScheduledStart;
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        this.nextScheduledStart = l + 20L + (long)serverLevel.getRandom().nextInt(20);
        PoiManager poiManager = serverLevel.getPoiManager();
        this.batchCache.long2ObjectEntrySet().removeIf(entry -> !((JitteredLinearRetry)entry.getValue()).isStillValid(l));
        Predicate<BlockPos> predicate = blockPos -> {
            JitteredLinearRetry jitteredLinearRetry = (JitteredLinearRetry)this.batchCache.get(blockPos.asLong());
            if (jitteredLinearRetry == null) {
                return true;
            }
            if (!jitteredLinearRetry.shouldRetry(l)) {
                return false;
            }
            jitteredLinearRetry.markAttempt(l);
            return true;
        };
        Set<Pair<Holder<PoiType>, BlockPos>> set = poiManager.findAllClosestFirstWithType(this.poiType, predicate, pathfinderMob.blockPosition(), 48, PoiManager.Occupancy.HAS_SPACE).limit(5L).collect(Collectors.toSet());
        Path path = AcquirePoi.findPathToPois(pathfinderMob, set);
        if (path != null && path.canReach()) {
            BlockPos blockPos2 = path.getTarget();
            poiManager.getType(blockPos2).ifPresent(holder2 -> {
                poiManager.take(this.poiType, (holder, blockPos2) -> blockPos2.equals(blockPos2), blockPos2, 1);
                pathfinderMob.getBrain().setMemory(this.memoryToAcquire, GlobalPos.of(serverLevel.dimension(), blockPos2));
                this.onPoiAcquisitionEvent.ifPresent(byte_ -> serverLevel.broadcastEntityEvent(pathfinderMob, (byte)byte_));
                this.batchCache.clear();
                DebugPackets.sendPoiTicketCountPacket(serverLevel, blockPos2);
            });
        } else {
            for (Pair<Holder<PoiType>, BlockPos> pair : set) {
                this.batchCache.computeIfAbsent(pair.getSecond().asLong(), m -> new JitteredLinearRetry(pathfinderMob.level.random, l));
            }
        }
    }

    @Nullable
    public static Path findPathToPois(Mob mob, Set<Pair<Holder<PoiType>, BlockPos>> set) {
        if (set.isEmpty()) {
            return null;
        }
        HashSet<BlockPos> set2 = new HashSet<BlockPos>();
        int i = 1;
        for (Pair<Holder<PoiType>, BlockPos> pair : set) {
            i = Math.max(i, pair.getFirst().value().validRange());
            set2.add(pair.getSecond());
        }
        return mob.getNavigation().createPath(set2, i);
    }

    static class JitteredLinearRetry {
        private static final int MIN_INTERVAL_INCREASE = 40;
        private static final int MAX_INTERVAL_INCREASE = 80;
        private static final int MAX_RETRY_PATHFINDING_INTERVAL = 400;
        private final RandomSource random;
        private long previousAttemptTimestamp;
        private long nextScheduledAttemptTimestamp;
        private int currentDelay;

        JitteredLinearRetry(RandomSource randomSource, long l) {
            this.random = randomSource;
            this.markAttempt(l);
        }

        public void markAttempt(long l) {
            this.previousAttemptTimestamp = l;
            int i = this.currentDelay + this.random.nextInt(40) + 40;
            this.currentDelay = Math.min(i, 400);
            this.nextScheduledAttemptTimestamp = l + (long)this.currentDelay;
        }

        public boolean isStillValid(long l) {
            return l - this.previousAttemptTimestamp < 400L;
        }

        public boolean shouldRetry(long l) {
            return l >= this.nextScheduledAttemptTimestamp;
        }

        public String toString() {
            return "RetryMarker{, previousAttemptAt=" + this.previousAttemptTimestamp + ", nextScheduledAttemptAt=" + this.nextScheduledAttemptTimestamp + ", currentDelay=" + this.currentDelay + "}";
        }
    }
}

