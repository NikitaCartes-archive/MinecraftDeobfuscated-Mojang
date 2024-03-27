package net.minecraft.core.component;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

public interface DataComponentMap extends Iterable<TypedDataComponent<?>> {
	DataComponentMap EMPTY = new DataComponentMap() {
		@Nullable
		@Override
		public <T> T get(DataComponentType<? extends T> dataComponentType) {
			return null;
		}

		@Override
		public Set<DataComponentType<?>> keySet() {
			return Set.of();
		}

		@Override
		public Iterator<TypedDataComponent<?>> iterator() {
			return Collections.emptyIterator();
		}
	};
	Codec<DataComponentMap> CODEC = DataComponentType.VALUE_MAP_CODEC.flatComapMap(DataComponentMap.Builder::buildFromMapTrusted, dataComponentMap -> {
		int i = dataComponentMap.size();
		if (i == 0) {
			return DataResult.success(Reference2ObjectMaps.emptyMap());
		} else {
			Reference2ObjectMap<DataComponentType<?>, Object> reference2ObjectMap = new Reference2ObjectArrayMap<>(i);

			for (TypedDataComponent<?> typedDataComponent : dataComponentMap) {
				if (!typedDataComponent.type().isTransient()) {
					reference2ObjectMap.put(typedDataComponent.type(), typedDataComponent.value());
				}
			}

			return DataResult.success(reference2ObjectMap);
		}
	});

	static DataComponentMap.Builder builder() {
		return new DataComponentMap.Builder();
	}

	@Nullable
	<T> T get(DataComponentType<? extends T> dataComponentType);

	Set<DataComponentType<?>> keySet();

	default boolean has(DataComponentType<?> dataComponentType) {
		return this.get(dataComponentType) != null;
	}

	default <T> T getOrDefault(DataComponentType<? extends T> dataComponentType, T object) {
		T object2 = this.get(dataComponentType);
		return object2 != null ? object2 : object;
	}

	@Nullable
	default <T> TypedDataComponent<T> getTyped(DataComponentType<T> dataComponentType) {
		T object = this.get(dataComponentType);
		return object != null ? new TypedDataComponent<>(dataComponentType, object) : null;
	}

	default Iterator<TypedDataComponent<?>> iterator() {
		return Iterators.transform(this.keySet().iterator(), dataComponentType -> (TypedDataComponent<?>)Objects.requireNonNull(this.getTyped(dataComponentType)));
	}

	default Stream<TypedDataComponent<?>> stream() {
		return StreamSupport.stream(Spliterators.spliterator(this.iterator(), (long)this.size(), 1345), false);
	}

	default int size() {
		return this.keySet().size();
	}

	default boolean isEmpty() {
		return this.size() == 0;
	}

	default DataComponentMap filter(Predicate<DataComponentType<?>> predicate) {
		return new DataComponentMap() {
			@Nullable
			@Override
			public <T> T get(DataComponentType<? extends T> dataComponentType) {
				return predicate.test(dataComponentType) ? DataComponentMap.this.get(dataComponentType) : null;
			}

			@Override
			public Set<DataComponentType<?>> keySet() {
				return Sets.filter(DataComponentMap.this.keySet(), predicate::test);
			}
		};
	}

	public static class Builder {
		private final Reference2ObjectMap<DataComponentType<?>, Object> map = new Reference2ObjectArrayMap<>();

		Builder() {
		}

		public <T> DataComponentMap.Builder set(DataComponentType<T> dataComponentType, @Nullable T object) {
			this.setUnchecked(dataComponentType, object);
			return this;
		}

		<T> void setUnchecked(DataComponentType<T> dataComponentType, @Nullable Object object) {
			if (object != null) {
				this.map.put(dataComponentType, object);
			} else {
				this.map.remove(dataComponentType);
			}
		}

		public DataComponentMap.Builder addAll(DataComponentMap dataComponentMap) {
			for (TypedDataComponent<?> typedDataComponent : dataComponentMap) {
				this.map.put(typedDataComponent.type(), typedDataComponent.value());
			}

			return this;
		}

		public DataComponentMap build() {
			return buildFromMapTrusted(this.map);
		}

		private static DataComponentMap buildFromMapTrusted(Map<DataComponentType<?>, Object> map) {
			if (map.isEmpty()) {
				return DataComponentMap.EMPTY;
			} else {
				return map.size() < 8
					? new DataComponentMap.Builder.SimpleMap(new Reference2ObjectArrayMap<>(map))
					: new DataComponentMap.Builder.SimpleMap(new Reference2ObjectOpenHashMap<>(map));
			}
		}

		static record SimpleMap(Reference2ObjectMap<DataComponentType<?>, Object> map) implements DataComponentMap {
			@Nullable
			@Override
			public <T> T get(DataComponentType<? extends T> dataComponentType) {
				return (T)this.map.get(dataComponentType);
			}

			@Override
			public boolean has(DataComponentType<?> dataComponentType) {
				return this.map.containsKey(dataComponentType);
			}

			@Override
			public Set<DataComponentType<?>> keySet() {
				return this.map.keySet();
			}

			@Override
			public Iterator<TypedDataComponent<?>> iterator() {
				return Iterators.transform(Reference2ObjectMaps.fastIterator(this.map), TypedDataComponent::fromEntryUnchecked);
			}

			@Override
			public int size() {
				return this.map.size();
			}

			public String toString() {
				return this.map.toString();
			}
		}
	}
}
