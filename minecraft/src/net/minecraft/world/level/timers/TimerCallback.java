package net.minecraft.world.level.timers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface TimerCallback<T> {
	void handle(T object, TimerQueue<T> timerQueue, long l);

	public abstract static class Serializer<T, C extends TimerCallback<T>> {
		private final ResourceLocation id;
		private final Class<?> cls;

		public Serializer(ResourceLocation resourceLocation, Class<?> class_) {
			this.id = resourceLocation;
			this.cls = class_;
		}

		public ResourceLocation getId() {
			return this.id;
		}

		public Class<?> getCls() {
			return this.cls;
		}

		public abstract void serialize(CompoundTag compoundTag, C timerCallback);

		public abstract C deserialize(CompoundTag compoundTag);
	}
}
