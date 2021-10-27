package net.minecraft.world.ticks;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;

public class ProtoChunkTicks<T> implements SerializableTickContainer<T>, TickContainerAccess<T> {
	private final List<SavedTick<T>> ticks = Lists.<SavedTick<T>>newArrayList();
	private final Set<SavedTick<?>> ticksPerPosition = new ObjectOpenCustomHashSet<>(SavedTick.UNIQUE_TICK_HASH);

	@Override
	public void schedule(ScheduledTick<T> scheduledTick) {
		SavedTick<T> savedTick = new SavedTick((T)scheduledTick.type(), scheduledTick.pos(), 0, scheduledTick.priority());
		this.schedule(savedTick);
	}

	private void schedule(SavedTick<T> savedTick) {
		if (this.ticksPerPosition.add(savedTick)) {
			this.ticks.add(savedTick);
		}
	}

	@Override
	public boolean hasScheduledTick(BlockPos blockPos, T object) {
		return this.ticksPerPosition.contains(SavedTick.probe(object, blockPos));
	}

	@Override
	public int count() {
		return this.ticks.size();
	}

	@Override
	public Tag save(long l, Function<T, String> function) {
		ListTag listTag = new ListTag();

		for (SavedTick<T> savedTick : this.ticks) {
			listTag.add(savedTick.save(function));
		}

		return listTag;
	}

	public List<SavedTick<T>> scheduledTicks() {
		return List.copyOf(this.ticks);
	}

	public static <T> ProtoChunkTicks<T> load(ListTag listTag, Function<String, Optional<T>> function, ChunkPos chunkPos) {
		ProtoChunkTicks<T> protoChunkTicks = new ProtoChunkTicks<>();
		SavedTick.loadTickList(listTag, function, chunkPos, protoChunkTicks::schedule);
		return protoChunkTicks;
	}
}
