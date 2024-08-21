package net.minecraft.core.component;

import java.util.stream.Stream;
import javax.annotation.Nullable;

public interface DataComponentHolder {
	DataComponentMap getComponents();

	@Nullable
	default <T> T get(DataComponentType<? extends T> dataComponentType) {
		return this.getComponents().get(dataComponentType);
	}

	default <T> Stream<T> getAllOfType(Class<? extends T> class_) {
		return this.getComponents().stream().map(TypedDataComponent::value).filter(object -> class_.isAssignableFrom(object.getClass())).map(object -> object);
	}

	default <T> T getOrDefault(DataComponentType<? extends T> dataComponentType, T object) {
		return this.getComponents().getOrDefault(dataComponentType, object);
	}

	default boolean has(DataComponentType<?> dataComponentType) {
		return this.getComponents().has(dataComponentType);
	}
}
