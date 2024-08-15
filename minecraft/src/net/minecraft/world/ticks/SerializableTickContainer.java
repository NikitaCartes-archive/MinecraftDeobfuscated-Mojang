package net.minecraft.world.ticks;

import java.util.List;

public interface SerializableTickContainer<T> {
	List<SavedTick<T>> pack(long l);
}
