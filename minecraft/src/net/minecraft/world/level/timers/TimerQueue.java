package net.minecraft.world.level.timers;

import com.google.common.collect.Maps;
import com.google.common.primitives.UnsignedLong;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimerQueue<T> {
	private static final Logger LOGGER = LogManager.getLogger();
	private final TimerCallbacks<T> callbacksRegistry;
	private final Queue<TimerQueue.Event<T>> queue = new PriorityQueue(createComparator());
	private UnsignedLong sequentialId = UnsignedLong.ZERO;
	private final Map<String, TimerQueue.Event<T>> events = Maps.<String, TimerQueue.Event<T>>newHashMap();

	private static <T> Comparator<TimerQueue.Event<T>> createComparator() {
		return Comparator.comparingLong(event -> event.triggerTime).thenComparing(event -> event.sequentialId);
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
			this.events.remove(event.id);
			event.callback.handle(object, this, l);
		}
	}

	private void addEvent(String string, long l, TimerCallback<T> timerCallback) {
		this.sequentialId = this.sequentialId.plus(UnsignedLong.ONE);
		TimerQueue.Event<T> event = new TimerQueue.Event<>(l, this.sequentialId, string, timerCallback);
		this.events.put(string, event);
		this.queue.add(event);
	}

	public boolean schedule(String string, long l, TimerCallback<T> timerCallback) {
		if (this.events.containsKey(string)) {
			return false;
		} else {
			this.addEvent(string, l, timerCallback);
			return true;
		}
	}

	public void reschedule(String string, long l, TimerCallback<T> timerCallback) {
		TimerQueue.Event<T> event = (TimerQueue.Event<T>)this.events.remove(string);
		if (event != null) {
			this.queue.remove(event);
		}

		this.addEvent(string, l, timerCallback);
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

	public void load(ListTag listTag) {
		this.queue.clear();
		this.events.clear();
		this.sequentialId = UnsignedLong.ZERO;
		if (!listTag.isEmpty()) {
			if (listTag.getElementType() != 10) {
				LOGGER.warn("Invalid format of events: " + listTag);
			} else {
				for (Tag tag : listTag) {
					this.loadEvent((CompoundTag)tag);
				}
			}
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

		private Event(long l, UnsignedLong unsignedLong, String string, TimerCallback<T> timerCallback) {
			this.triggerTime = l;
			this.sequentialId = unsignedLong;
			this.id = string;
			this.callback = timerCallback;
		}
	}
}
