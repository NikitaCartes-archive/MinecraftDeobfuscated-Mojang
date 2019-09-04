/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.world.level.timers.TimerCallback;
import net.minecraft.world.level.timers.TimerCallbacks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimerQueue<T> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final TimerCallbacks<T> callbacksRegistry;
    private final Queue<Event<T>> queue = new PriorityQueue<Event<T>>(TimerQueue.createComparator());
    private UnsignedLong sequentialId = UnsignedLong.ZERO;
    private final Map<String, Event<T>> events = Maps.newHashMap();

    private static <T> Comparator<Event<T>> createComparator() {
        return Comparator.comparingLong(event -> event.triggerTime).thenComparing(event -> event.sequentialId);
    }

    public TimerQueue(TimerCallbacks<T> timerCallbacks) {
        this.callbacksRegistry = timerCallbacks;
    }

    public void tick(T object, long l) {
        Event<T> event;
        while ((event = this.queue.peek()) != null && event.triggerTime <= l) {
            this.queue.remove();
            this.events.remove(event.id);
            event.callback.handle(object, this, l);
        }
    }

    private void addEvent(String string, long l, TimerCallback<T> timerCallback) {
        this.sequentialId = this.sequentialId.plus(UnsignedLong.ONE);
        Event event = new Event(l, this.sequentialId, string, timerCallback);
        this.events.put(string, event);
        this.queue.add(event);
    }

    public boolean schedule(String string, long l, TimerCallback<T> timerCallback) {
        if (this.events.containsKey(string)) {
            return false;
        }
        this.addEvent(string, l, timerCallback);
        return true;
    }

    public void reschedule(String string, long l, TimerCallback<T> timerCallback) {
        Event<T> event = this.events.remove(string);
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
        if (listTag.isEmpty()) {
            return;
        }
        if (listTag.getElementType() != 10) {
            LOGGER.warn("Invalid format of events: " + listTag);
            return;
        }
        for (Tag tag : listTag) {
            this.loadEvent((CompoundTag)tag);
        }
    }

    private CompoundTag storeEvent(Event<T> event) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Name", event.id);
        compoundTag.putLong("TriggerTime", event.triggerTime);
        compoundTag.put("Callback", this.callbacksRegistry.serialize(event.callback));
        return compoundTag;
    }

    public ListTag store() {
        ListTag listTag = new ListTag();
        this.queue.stream().sorted(TimerQueue.createComparator()).map(this::storeEvent).forEach(listTag::add);
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

