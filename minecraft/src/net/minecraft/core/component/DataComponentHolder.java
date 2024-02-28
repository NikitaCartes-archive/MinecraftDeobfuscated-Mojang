package net.minecraft.core.component;

import javax.annotation.Nullable;

public interface DataComponentHolder {
	DataComponentMap getComponents();

	@Nullable
	default <T> T get(DataComponentType<? extends T> dataComponentType) {
		return this.getComponents().get(dataComponentType);
	}

	default <T> T getOrDefault(DataComponentType<? extends T> dataComponentType, T object) {
		return this.getComponents().getOrDefault(dataComponentType, object);
	}

	default boolean has(DataComponentType<?> dataComponentType) {
		return this.getComponents().has(dataComponentType);
	}
}
