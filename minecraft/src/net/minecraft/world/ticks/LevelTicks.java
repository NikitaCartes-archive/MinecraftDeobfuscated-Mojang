package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class LevelTicks<T> implements LevelTickAccess<T> {
	private static final Comparator<LevelChunkTicks<?>> CONTAINER_DRAIN_ORDER = (levelChunkTicks, levelChunkTicks2) -> ScheduledTick.INTRA_TICK_DRAIN_ORDER
			.compare(levelChunkTicks.peek(), levelChunkTicks2.peek());
	private final LongPredicate tickCheck;
	private final Supplier<ProfilerFiller> profiler;
	private final Long2ObjectMap<LevelChunkTicks<T>> allContainers = new Long2ObjectOpenHashMap<>();
	private final Long2LongMap nextTickForContainer = Util.make(
		new Long2LongOpenHashMap(), long2LongOpenHashMap -> long2LongOpenHashMap.defaultReturnValue(Long.MAX_VALUE)
	);
	private final Queue<LevelChunkTicks<T>> containersToTick = new PriorityQueue(CONTAINER_DRAIN_ORDER);
	private final Queue<ScheduledTick<T>> toRunThisTick = new ArrayDeque();
	private final List<ScheduledTick<T>> alreadyRunThisTick = new ArrayList();
	private final Set<ScheduledTick<?>> toRunThisTickSet = new ObjectOpenCustomHashSet<>(ScheduledTick.UNIQUE_TICK_HASH);
	private final BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> chunkScheduleUpdater = (levelChunkTicks, scheduledTick) -> {
		if (scheduledTick.equals(levelChunkTicks.peek())) {
			this.updateContainerScheduling(scheduledTick);
		}
	};

	public LevelTicks(LongPredicate longPredicate, Supplier<ProfilerFiller> supplier) {
		this.tickCheck = longPredicate;
		this.profiler = supplier;
	}

	public void addContainer(ChunkPos chunkPos, LevelChunkTicks<T> levelChunkTicks) {
		long l = chunkPos.toLong();
		this.allContainers.put(l, levelChunkTicks);
		ScheduledTick<T> scheduledTick = levelChunkTicks.peek();
		if (scheduledTick != null) {
			this.nextTickForContainer.put(l, scheduledTick.triggerTick());
		}

		levelChunkTicks.setOnTickAdded(this.chunkScheduleUpdater);
	}

	public void removeContainer(ChunkPos chunkPos) {
		long l = chunkPos.toLong();
		LevelChunkTicks<T> levelChunkTicks = this.allContainers.remove(l);
		this.nextTickForContainer.remove(l);
		if (levelChunkTicks != null) {
			levelChunkTicks.setOnTickAdded(null);
		}
	}

	@Override
	public void schedule(ScheduledTick<T> scheduledTick) {
		long l = ChunkPos.asLong(scheduledTick.pos());
		LevelChunkTicks<T> levelChunkTicks = this.allContainers.get(l);
		if (levelChunkTicks == null) {
			Util.pauseInIde((T)(new IllegalStateException("Trying to schedule tick in not loaded position " + scheduledTick.pos())));
		} else {
			levelChunkTicks.schedule(scheduledTick);
		}
	}

	public void tick(long l, int i, BiConsumer<BlockPos, T> biConsumer) {
		ProfilerFiller profilerFiller = (ProfilerFiller)this.profiler.get();
		profilerFiller.push("collect");
		this.collectTicks(l, i, profilerFiller);
		profilerFiller.popPush("run");
		profilerFiller.incrementCounter("ticksToRun", this.toRunThisTick.size());
		this.runCollectedTicks(biConsumer);
		profilerFiller.popPush("cleanup");
		this.cleanupAfterTick();
		profilerFiller.pop();
	}

	private void collectTicks(long l, int i, ProfilerFiller profilerFiller) {
		this.sortContainersToTick(l);
		profilerFiller.incrementCounter("containersToTick", this.containersToTick.size());
		this.drainContainers(l, i);
		this.rescheduleLeftoverContainers();
	}

	private void sortContainersToTick(long l) {
		ObjectIterator<Entry> objectIterator = Long2LongMaps.fastIterator(this.nextTickForContainer);

		while (objectIterator.hasNext()) {
			Entry entry = (Entry)objectIterator.next();
			long m = entry.getLongKey();
			long n = entry.getLongValue();
			if (n <= l) {
				LevelChunkTicks<T> levelChunkTicks = this.allContainers.get(m);
				if (levelChunkTicks == null) {
					objectIterator.remove();
				} else {
					ScheduledTick<T> scheduledTick = levelChunkTicks.peek();
					if (scheduledTick == null) {
						objectIterator.remove();
					} else if (scheduledTick.triggerTick() > l) {
						entry.setValue(scheduledTick.triggerTick());
					} else if (this.tickCheck.test(m)) {
						objectIterator.remove();
						this.containersToTick.add(levelChunkTicks);
					}
				}
			}
		}
	}

	private void drainContainers(long l, int i) {
		LevelChunkTicks<T> levelChunkTicks;
		while (this.canScheduleMoreTicks(i) && (levelChunkTicks = (LevelChunkTicks<T>)this.containersToTick.poll()) != null) {
			ScheduledTick<T> scheduledTick = levelChunkTicks.poll();
			this.scheduleForThisTick(scheduledTick);
			this.drainFromCurrentContainer(this.containersToTick, levelChunkTicks, l, i);
			ScheduledTick<T> scheduledTick2 = levelChunkTicks.peek();
			if (scheduledTick2 != null) {
				if (scheduledTick2.triggerTick() <= l && this.canScheduleMoreTicks(i)) {
					this.containersToTick.add(levelChunkTicks);
				} else {
					this.updateContainerScheduling(scheduledTick2);
				}
			}
		}
	}

	private void rescheduleLeftoverContainers() {
		for (LevelChunkTicks<T> levelChunkTicks : this.containersToTick) {
			this.updateContainerScheduling(levelChunkTicks.peek());
		}
	}

	private void updateContainerScheduling(ScheduledTick<T> scheduledTick) {
		this.nextTickForContainer.put(ChunkPos.asLong(scheduledTick.pos()), scheduledTick.triggerTick());
	}

	private void drainFromCurrentContainer(Queue<LevelChunkTicks<T>> queue, LevelChunkTicks<T> levelChunkTicks, long l, int i) {
		if (this.canScheduleMoreTicks(i)) {
			LevelChunkTicks<T> levelChunkTicks2 = (LevelChunkTicks<T>)queue.peek();
			ScheduledTick<T> scheduledTick = levelChunkTicks2 != null ? levelChunkTicks2.peek() : null;

			while (this.canScheduleMoreTicks(i)) {
				ScheduledTick<T> scheduledTick2 = levelChunkTicks.peek();
				if (scheduledTick2 == null
					|| scheduledTick2.triggerTick() > l
					|| scheduledTick != null && ScheduledTick.INTRA_TICK_DRAIN_ORDER.compare(scheduledTick2, scheduledTick) > 0) {
					break;
				}

				levelChunkTicks.poll();
				this.scheduleForThisTick(scheduledTick2);
			}
		}
	}

	private void scheduleForThisTick(ScheduledTick<T> scheduledTick) {
		this.toRunThisTick.add(scheduledTick);
	}

	private boolean canScheduleMoreTicks(int i) {
		return this.toRunThisTick.size() < i;
	}

	private void runCollectedTicks(BiConsumer<BlockPos, T> biConsumer) {
		while (!this.toRunThisTick.isEmpty()) {
			ScheduledTick<T> scheduledTick = (ScheduledTick<T>)this.toRunThisTick.poll();
			if (!this.toRunThisTickSet.isEmpty()) {
				this.toRunThisTickSet.remove(scheduledTick);
			}

			this.alreadyRunThisTick.add(scheduledTick);
			biConsumer.accept(scheduledTick.pos(), scheduledTick.type());
		}
	}

	private void cleanupAfterTick() {
		this.toRunThisTick.clear();
		this.containersToTick.clear();
		this.alreadyRunThisTick.clear();
		this.toRunThisTickSet.clear();
	}

	@Override
	public boolean hasScheduledTick(BlockPos blockPos, T object) {
		LevelChunkTicks<T> levelChunkTicks = this.allContainers.get(ChunkPos.asLong(blockPos));
		return levelChunkTicks != null && levelChunkTicks.hasScheduledTick(blockPos, object);
	}

	@Override
	public boolean willTickThisTick(BlockPos blockPos, T object) {
		this.calculateTickSetIfNeeded();
		return this.toRunThisTickSet.contains(ScheduledTick.probe(object, blockPos));
	}

	private void calculateTickSetIfNeeded() {
		if (this.toRunThisTickSet.isEmpty() && !this.toRunThisTick.isEmpty()) {
			this.toRunThisTickSet.addAll(this.toRunThisTick);
		}
	}

	private void forContainersInArea(BoundingBox boundingBox, LevelTicks.PosAndContainerConsumer<T> posAndContainerConsumer) {
		int i = SectionPos.posToSectionCoord((double)boundingBox.minX());
		int j = SectionPos.posToSectionCoord((double)boundingBox.minZ());
		int k = SectionPos.posToSectionCoord((double)boundingBox.maxX());
		int l = SectionPos.posToSectionCoord((double)boundingBox.maxZ());

		for (int m = i; m <= k; m++) {
			for (int n = j; n <= l; n++) {
				long o = ChunkPos.asLong(m, n);
				LevelChunkTicks<T> levelChunkTicks = this.allContainers.get(o);
				if (levelChunkTicks != null) {
					posAndContainerConsumer.accept(o, levelChunkTicks);
				}
			}
		}
	}

	public void clearArea(BoundingBox boundingBox) {
		Predicate<ScheduledTick<T>> predicate = scheduledTick -> boundingBox.isInside(scheduledTick.pos());
		this.forContainersInArea(boundingBox, (l, levelChunkTicks) -> {
			ScheduledTick<T> scheduledTick = levelChunkTicks.peek();
			levelChunkTicks.removeIf(predicate);
			ScheduledTick<T> scheduledTick2 = levelChunkTicks.peek();
			if (scheduledTick2 != scheduledTick) {
				if (scheduledTick2 != null) {
					this.updateContainerScheduling(scheduledTick2);
				} else {
					this.nextTickForContainer.remove(l);
				}
			}
		});
		this.alreadyRunThisTick.removeIf(predicate);
		this.toRunThisTick.removeIf(predicate);
	}

	public void copyArea(BoundingBox boundingBox, Vec3i vec3i) {
		List<ScheduledTick<T>> list = new ArrayList();
		Predicate<ScheduledTick<T>> predicate = scheduledTick -> boundingBox.isInside(scheduledTick.pos());
		this.alreadyRunThisTick.stream().filter(predicate).forEach(list::add);
		this.toRunThisTick.stream().filter(predicate).forEach(list::add);
		this.forContainersInArea(boundingBox, (lx, levelChunkTicks) -> levelChunkTicks.getAll().filter(predicate).forEach(list::add));
		LongSummaryStatistics longSummaryStatistics = list.stream().mapToLong(ScheduledTick::subTickOrder).summaryStatistics();
		long l = longSummaryStatistics.getMin();
		long m = longSummaryStatistics.getMax();
		list.forEach(
			scheduledTick -> this.schedule(
					new ScheduledTick<>(
						(T)scheduledTick.type(),
						scheduledTick.pos().offset(vec3i),
						scheduledTick.triggerTick(),
						scheduledTick.priority(),
						scheduledTick.subTickOrder() - l + m + 1L
					)
				)
		);
	}

	@Override
	public int count() {
		return this.allContainers.values().stream().mapToInt(TickAccess::count).sum();
	}

	@FunctionalInterface
	interface PosAndContainerConsumer<T> {
		void accept(long l, LevelChunkTicks<T> levelChunkTicks);
	}
}
