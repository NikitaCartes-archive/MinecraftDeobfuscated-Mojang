package net.minecraft.world.ticks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;

public class LevelChunkTicks<T> implements SerializableTickContainer<T>, TickContainerAccess<T> {
	private final Queue<ScheduledTick<T>> tickQueue = new PriorityQueue(ScheduledTick.DRAIN_ORDER);
	@Nullable
	private List<SavedTick<T>> pendingTicks;
	private final Set<ScheduledTick<?>> ticksPerPosition = new ObjectOpenCustomHashSet<>(ScheduledTick.UNIQUE_TICK_HASH);
	@Nullable
	private BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> onTickAdded;

	public LevelChunkTicks() {
	}

	public LevelChunkTicks(List<SavedTick<T>> list) {
		this.pendingTicks = list;

		for (SavedTick<T> savedTick : list) {
			this.ticksPerPosition.add(ScheduledTick.probe(savedTick.type(), savedTick.pos()));
		}
	}

	public void setOnTickAdded(@Nullable BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> biConsumer) {
		this.onTickAdded = biConsumer;
	}

	@Nullable
	public ScheduledTick<T> peek() {
		return (ScheduledTick<T>)this.tickQueue.peek();
	}

	@Nullable
	public ScheduledTick<T> poll() {
		ScheduledTick<T> scheduledTick = (ScheduledTick<T>)this.tickQueue.poll();
		if (scheduledTick != null) {
			this.ticksPerPosition.remove(scheduledTick);
		}

		return scheduledTick;
	}

	@Override
	public void schedule(ScheduledTick<T> scheduledTick) {
		if (this.ticksPerPosition.add(scheduledTick)) {
			this.scheduleUnchecked(scheduledTick);
		}
	}

	private void scheduleUnchecked(ScheduledTick<T> scheduledTick) {
		this.tickQueue.add(scheduledTick);
		if (this.onTickAdded != null) {
			this.onTickAdded.accept(this, scheduledTick);
		}
	}

	@Override
	public boolean hasScheduledTick(BlockPos blockPos, T object) {
		return this.ticksPerPosition.contains(ScheduledTick.probe(object, blockPos));
	}

	public void removeIf(Predicate<ScheduledTick<T>> predicate) {
		Iterator<ScheduledTick<T>> iterator = this.tickQueue.iterator();

		while (iterator.hasNext()) {
			ScheduledTick<T> scheduledTick = (ScheduledTick<T>)iterator.next();
			if (predicate.test(scheduledTick)) {
				iterator.remove();
				this.ticksPerPosition.remove(scheduledTick);
			}
		}
	}

	public Stream<ScheduledTick<T>> getAll() {
		return this.tickQueue.stream();
	}

	@Override
	public int count() {
		return this.tickQueue.size() + (this.pendingTicks != null ? this.pendingTicks.size() : 0);
	}

	public ListTag save(long l, Function<T, String> function) {
		ListTag listTag = new ListTag();
		if (this.pendingTicks != null) {
			for (SavedTick<T> savedTick : this.pendingTicks) {
				listTag.add(savedTick.save(function));
			}
		}

		for (ScheduledTick<T> scheduledTick : this.tickQueue) {
			listTag.add(SavedTick.saveTick(scheduledTick, function, l));
		}

		return listTag;
	}

	public void unpack(long l) {
		if (this.pendingTicks != null) {
			int i = -this.pendingTicks.size();

			for (SavedTick<T> savedTick : this.pendingTicks) {
				this.scheduleUnchecked(savedTick.unpack(l, (long)(i++)));
			}
		}

		this.pendingTicks = null;
	}

	public static <T> LevelChunkTicks<T> load(ListTag listTag, Function<String, Optional<T>> function, ChunkPos chunkPos) {
		Builder<SavedTick<T>> builder = ImmutableList.builder();
		SavedTick.loadTickList(listTag, function, chunkPos, builder::add);
		return new LevelChunkTicks<>(builder.build());
	}
}
