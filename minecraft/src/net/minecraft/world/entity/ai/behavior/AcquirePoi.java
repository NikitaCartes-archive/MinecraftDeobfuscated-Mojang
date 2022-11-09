package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableLong;

public class AcquirePoi {
	public static final int SCAN_RANGE = 48;

	public static BehaviorControl<PathfinderMob> create(
		Predicate<Holder<PoiType>> predicate, MemoryModuleType<GlobalPos> memoryModuleType, boolean bl, Optional<Byte> optional
	) {
		return create(predicate, memoryModuleType, memoryModuleType, bl, optional);
	}

	public static BehaviorControl<PathfinderMob> create(
		Predicate<Holder<PoiType>> predicate,
		MemoryModuleType<GlobalPos> memoryModuleType,
		MemoryModuleType<GlobalPos> memoryModuleType2,
		boolean bl,
		Optional<Byte> optional
	) {
		int i = 5;
		int j = 20;
		MutableLong mutableLong = new MutableLong(0L);
		Long2ObjectMap<AcquirePoi.JitteredLinearRetry> long2ObjectMap = new Long2ObjectOpenHashMap<>();
		OneShot<PathfinderMob> oneShot = BehaviorBuilder.create(
			instance -> instance.group(instance.absent(memoryModuleType2))
					.apply(
						instance,
						memoryAccessor -> (serverLevel, pathfinderMob, l) -> {
								if (bl && pathfinderMob.isBaby()) {
									return false;
								} else if (mutableLong.getValue() == 0L) {
									mutableLong.setValue(serverLevel.getGameTime() + (long)serverLevel.random.nextInt(20));
									return false;
								} else if (serverLevel.getGameTime() < mutableLong.getValue()) {
									return false;
								} else {
									mutableLong.setValue(l + 20L + (long)serverLevel.getRandom().nextInt(20));
									PoiManager poiManager = serverLevel.getPoiManager();
									long2ObjectMap.long2ObjectEntrySet().removeIf(entry -> !((AcquirePoi.JitteredLinearRetry)entry.getValue()).isStillValid(l));
									Predicate<BlockPos> predicate2 = blockPos -> {
										AcquirePoi.JitteredLinearRetry jitteredLinearRetry = long2ObjectMap.get(blockPos.asLong());
										if (jitteredLinearRetry == null) {
											return true;
										} else if (!jitteredLinearRetry.shouldRetry(l)) {
											return false;
										} else {
											jitteredLinearRetry.markAttempt(l);
											return true;
										}
									};
									Set<Pair<Holder<PoiType>, BlockPos>> set = (Set<Pair<Holder<PoiType>, BlockPos>>)poiManager.findAllClosestFirstWithType(
											predicate, predicate2, pathfinderMob.blockPosition(), 48, PoiManager.Occupancy.HAS_SPACE
										)
										.limit(5L)
										.collect(Collectors.toSet());
									Path path = findPathToPois(pathfinderMob, set);
									if (path != null && path.canReach()) {
										BlockPos blockPos = path.getTarget();
										poiManager.getType(blockPos).ifPresent(holder -> {
											poiManager.take(predicate, (holderx, blockPos2) -> blockPos2.equals(blockPos), blockPos, 1);
											memoryAccessor.set(GlobalPos.of(serverLevel.dimension(), blockPos));
											optional.ifPresent(byte_ -> serverLevel.broadcastEntityEvent(pathfinderMob, byte_));
											long2ObjectMap.clear();
											DebugPackets.sendPoiTicketCountPacket(serverLevel, blockPos);
										});
									} else {
										for (Pair<Holder<PoiType>, BlockPos> pair : set) {
											long2ObjectMap.computeIfAbsent(
												pair.getSecond().asLong(),
												(Long2ObjectFunction<? extends AcquirePoi.JitteredLinearRetry>)(m -> new AcquirePoi.JitteredLinearRetry(serverLevel.random, l))
											);
										}
									}

									return true;
								}
							}
					)
		);
		return memoryModuleType2 == memoryModuleType
			? oneShot
			: BehaviorBuilder.create(instance -> instance.group(instance.absent(memoryModuleType)).apply(instance, memoryAccessor -> oneShot));
	}

	@Nullable
	public static Path findPathToPois(Mob mob, Set<Pair<Holder<PoiType>, BlockPos>> set) {
		if (set.isEmpty()) {
			return null;
		} else {
			Set<BlockPos> set2 = new HashSet();
			int i = 1;

			for (Pair<Holder<PoiType>, BlockPos> pair : set) {
				i = Math.max(i, pair.getFirst().value().validRange());
				set2.add(pair.getSecond());
			}

			return mob.getNavigation().createPath(set2, i);
		}
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
			return "RetryMarker{, previousAttemptAt="
				+ this.previousAttemptTimestamp
				+ ", nextScheduledAttemptAt="
				+ this.nextScheduledAttemptTimestamp
				+ ", currentDelay="
				+ this.currentDelay
				+ "}";
		}
	}
}
