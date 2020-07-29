package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;

public class AcquirePoi extends Behavior<PathfinderMob> {
	private final PoiType poiType;
	private final MemoryModuleType<GlobalPos> memoryToAcquire;
	private final boolean onlyIfAdult;
	private final Optional<Byte> onPoiAcquisitionEvent;
	private long nextScheduledStart;
	private final Long2ObjectMap<AcquirePoi.JitteredLinearRetry> batchCache = new Long2ObjectOpenHashMap<>();

	public AcquirePoi(
		PoiType poiType, MemoryModuleType<GlobalPos> memoryModuleType, MemoryModuleType<GlobalPos> memoryModuleType2, boolean bl, Optional<Byte> optional
	) {
		super(constructEntryConditionMap(memoryModuleType, memoryModuleType2));
		this.poiType = poiType;
		this.memoryToAcquire = memoryModuleType2;
		this.onlyIfAdult = bl;
		this.onPoiAcquisitionEvent = optional;
	}

	public AcquirePoi(PoiType poiType, MemoryModuleType<GlobalPos> memoryModuleType, boolean bl, Optional<Byte> optional) {
		this(poiType, memoryModuleType, memoryModuleType, bl, optional);
	}

	private static ImmutableMap<MemoryModuleType<?>, MemoryStatus> constructEntryConditionMap(
		MemoryModuleType<GlobalPos> memoryModuleType, MemoryModuleType<GlobalPos> memoryModuleType2
	) {
		Builder<MemoryModuleType<?>, MemoryStatus> builder = ImmutableMap.builder();
		builder.put(memoryModuleType, MemoryStatus.VALUE_ABSENT);
		if (memoryModuleType2 != memoryModuleType) {
			builder.put(memoryModuleType2, MemoryStatus.VALUE_ABSENT);
		}

		return builder.build();
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
		if (this.onlyIfAdult && pathfinderMob.isBaby()) {
			return false;
		} else if (this.nextScheduledStart == 0L) {
			this.nextScheduledStart = pathfinderMob.level.getGameTime() + (long)serverLevel.random.nextInt(20);
			return false;
		} else {
			return serverLevel.getGameTime() >= this.nextScheduledStart;
		}
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		this.nextScheduledStart = l + 20L + (long)serverLevel.getRandom().nextInt(20);
		PoiManager poiManager = serverLevel.getPoiManager();
		this.batchCache.long2ObjectEntrySet().removeIf(entry -> !((AcquirePoi.JitteredLinearRetry)entry.getValue()).isStillValid(l));
		Predicate<BlockPos> predicate = blockPos -> {
			AcquirePoi.JitteredLinearRetry jitteredLinearRetry = this.batchCache.get(blockPos.asLong());
			if (jitteredLinearRetry == null) {
				return true;
			} else if (!jitteredLinearRetry.shouldRetry(l)) {
				return false;
			} else {
				jitteredLinearRetry.markAttempt(l);
				return true;
			}
		};
		Set<BlockPos> set = (Set<BlockPos>)poiManager.findAllClosestFirst(
				this.poiType.getPredicate(), predicate, pathfinderMob.blockPosition(), 48, PoiManager.Occupancy.HAS_SPACE
			)
			.limit(5L)
			.collect(Collectors.toSet());
		Path path = pathfinderMob.getNavigation().createPath(set, this.poiType.getValidRange());
		if (path != null && path.canReach()) {
			BlockPos blockPos = path.getTarget();
			poiManager.getType(blockPos).ifPresent(poiType -> {
				poiManager.take(this.poiType.getPredicate(), blockPos2x -> blockPos2x.equals(blockPos), blockPos, 1);
				pathfinderMob.getBrain().setMemory(this.memoryToAcquire, GlobalPos.of(serverLevel.dimension(), blockPos));
				this.onPoiAcquisitionEvent.ifPresent(byte_ -> serverLevel.broadcastEntityEvent(pathfinderMob, byte_));
				this.batchCache.clear();
				DebugPackets.sendPoiTicketCountPacket(serverLevel, blockPos);
			});
		} else {
			for (BlockPos blockPos2 : set) {
				this.batchCache.computeIfAbsent(blockPos2.asLong(), m -> new AcquirePoi.JitteredLinearRetry(pathfinderMob.level.random, l));
			}
		}
	}

	static class JitteredLinearRetry {
		private final Random random;
		private long previousAttemptTimestamp;
		private long nextScheduledAttemptTimestamp;
		private int currentDelay;

		JitteredLinearRetry(Random random, long l) {
			this.random = random;
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
			return "RetryMarker{, previousAttemptAt="
				+ this.previousAttemptTimestamp
				+ ", nextScheduledAttemptAt="
				+ this.nextScheduledAttemptTimestamp
				+ ", currentDelay="
				+ this.currentDelay
				+ '}';
		}
	}
}
