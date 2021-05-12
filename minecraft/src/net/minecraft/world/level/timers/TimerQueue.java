package net.minecraft.world.level.timers;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.primitives.UnsignedLong;
import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimerQueue<T> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String CALLBACK_DATA_TAG = "Callback";
	private static final String TIMER_NAME_TAG = "Name";
	private static final String TIMER_TRIGGER_TIME_TAG = "TriggerTime";
	private final TimerCallbacks<T> callbacksRegistry;
	private final Queue<TimerQueue.Event<T>> queue = new PriorityQueue(createComparator());
	private UnsignedLong sequentialId = UnsignedLong.ZERO;
	private final Table<String, Long, TimerQueue.Event<T>> events = HashBasedTable.create();

	private static <T> Comparator<TimerQueue.Event<T>> createComparator() {
		return Comparator.comparingLong(event -> event.triggerTime).thenComparing(event -> event.sequentialId);
	}

	public TimerQueue(TimerCallbacks<T> timerCallbacks, Stream<Dynamic<Tag>> stream) {
		this(timerCallbacks);
		this.queue.clear();
		this.events.clear();
		this.sequentialId = UnsignedLong.ZERO;
		stream.forEach(dynamic -> {
			if (!(dynamic.getValue() instanceof CompoundTag)) {
				LOGGER.warn("Invalid format of events: {}", dynamic);
			} else {
				this.loadEvent((CompoundTag)dynamic.getValue());
			}
		});
	}

	public TimerQueue(TimerCallbacks<T> timerCallbacks) {
		this.callbacksRegistry = timerCallbacks;
	}

	public void tick(T object, long l) {
		while (true) {
			TimerQueue.Event<T> event = (TimerQueue.Event<T>)this.queue.peek();
			if (event == null || event.triggerTime > l) {
				return;
			}

			this.queue.remove();
			this.events.remove(event.id, l);
			event.callback.handle(object, this, l);
		}
	}

	public void schedule(String string, long l, TimerCallback<T> timerCallback) {
		if (!this.events.contains(string, l)) {
			this.sequentialId = this.sequentialId.plus(UnsignedLong.ONE);
			TimerQueue.Event<T> event = new TimerQueue.Event<>(l, this.sequentialId, string, timerCallback);
			this.events.put(string, l, event);
			this.queue.add(event);
		}
	}

	public int remove(String string) {
		Collection<TimerQueue.Event<T>> collection = this.events.row(string).values();
		collection.forEach(this.queue::remove);
		int i = collection.size();
		collection.clear();
		return i;
	}

	public Set<String> getEventsIds() {
		return Collections.unmodifiableSet(this.events.rowKeySet());
	}

	private void loadEvent(CompoundTag compoundTag) {
		CompoundTag compoundTag2 = compoundTag.getCompound("Callback");
		TimerCallback<T> timerCallback = this.callbacksRegistry.deserialize(compoundTag2);
		if (timerCallback != null) {
			String string = compoundTag.getString("Name");
			long l = compoundTag.getLong("TriggerTime");
			this.schedule(string, l, timerCallback);
		}
	}

	private CompoundTag storeEvent(TimerQueue.Event<T> event) {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("Name", event.id);
		compoundTag.putLong("TriggerTime", event.triggerTime);
		compoundTag.put("Callback", this.callbacksRegistry.serialize(event.callback));
		return compoundTag;
	}

	public ListTag store() {
		ListTag listTag = new ListTag();
		this.queue.stream().sorted(createComparator()).map(this::storeEvent).forEach(listTag::add);
		return listTag;
	}

	public static class Event<T> {
		public final long triggerTime;
		public final UnsignedLong sequentialId;
		public final String id;
		public final TimerCallback<T> callback;

		Event(long l, UnsignedLong unsignedLong, String string, TimerCallback<T> timerCallback) {
			this.triggerTime = l;
			this.sequentialId = unsignedLong;
			this.id = string;
			this.callback = timerCallback;
		}
	}
}
