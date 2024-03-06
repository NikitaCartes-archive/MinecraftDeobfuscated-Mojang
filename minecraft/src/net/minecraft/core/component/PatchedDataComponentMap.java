package net.minecraft.core.component;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public final class PatchedDataComponentMap implements DataComponentMap {
	private final DataComponentMap prototype;
	private Reference2ObjectMap<DataComponentType<?>, Optional<?>> patch;
	private boolean copyOnWrite;

	public PatchedDataComponentMap(DataComponentMap dataComponentMap) {
		this(dataComponentMap, Reference2ObjectMaps.emptyMap(), true);
	}

	private PatchedDataComponentMap(DataComponentMap dataComponentMap, Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2ObjectMap, boolean bl) {
		this.prototype = dataComponentMap;
		this.patch = reference2ObjectMap;
		this.copyOnWrite = bl;
	}

	public static PatchedDataComponentMap fromPatch(DataComponentMap dataComponentMap, DataComponentPatch dataComponentPatch) {
		if (isPatchSanitized(dataComponentMap, dataComponentPatch.map)) {
			return new PatchedDataComponentMap(dataComponentMap, dataComponentPatch.map, true);
		} else {
			PatchedDataComponentMap patchedDataComponentMap = new PatchedDataComponentMap(dataComponentMap);
			patchedDataComponentMap.applyPatch(dataComponentPatch);
			return patchedDataComponentMap;
		}
	}

	private static boolean isPatchSanitized(DataComponentMap dataComponentMap, Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2ObjectMap) {
		for (Entry<DataComponentType<?>, Optional<?>> entry : Reference2ObjectMaps.fastIterable(reference2ObjectMap)) {
			Object object = dataComponentMap.get((DataComponentType)entry.getKey());
			Optional<?> optional = (Optional<?>)entry.getValue();
			if (optional.isPresent() && optional.get().equals(object)) {
				return false;
			}

			if (optional.isEmpty() && object == null) {
				return false;
			}
		}

		return true;
	}

	@Nullable
	@Override
	public <T> T get(DataComponentType<? extends T> dataComponentType) {
		Optional<? extends T> optional = (Optional<? extends T>)this.patch.get(dataComponentType);
		return (T)(optional != null ? optional.orElse(null) : this.prototype.get(dataComponentType));
	}

	@Nullable
	public <T> T set(DataComponentType<? super T> dataComponentType, @Nullable T object) {
		this.ensureMapOwnership();
		T object2 = this.prototype.get((DataComponentType<? extends T>)dataComponentType);
		Optional<T> optional;
		if (Objects.equals(object, object2)) {
			optional = (Optional<T>)this.patch.remove(dataComponentType);
		} else {
			optional = (Optional<T>)this.patch.put(dataComponentType, Optional.ofNullable(object));
		}

		return (T)(optional != null ? optional.orElse(object2) : object2);
	}

	@Nullable
	public <T> T remove(DataComponentType<? extends T> dataComponentType) {
		this.ensureMapOwnership();
		T object = this.prototype.get(dataComponentType);
		Optional<? extends T> optional;
		if (object != null) {
			optional = (Optional<? extends T>)this.patch.put(dataComponentType, Optional.empty());
		} else {
			optional = (Optional<? extends T>)this.patch.remove(dataComponentType);
		}

		return (T)(optional != null ? optional.orElse(null) : object);
	}

	public void applyPatch(DataComponentPatch dataComponentPatch) {
		this.ensureMapOwnership();

		for (Entry<DataComponentType<?>, Optional<?>> entry : Reference2ObjectMaps.fastIterable(dataComponentPatch.map)) {
			this.applyPatch((DataComponentType<?>)entry.getKey(), (Optional<?>)entry.getValue());
		}
	}

	private void applyPatch(DataComponentType<?> dataComponentType, Optional<?> optional) {
		Object object = this.prototype.get(dataComponentType);
		if (optional.isPresent()) {
			if (optional.get().equals(object)) {
				this.patch.remove(dataComponentType);
			} else {
				this.patch.put(dataComponentType, optional);
			}
		} else if (object != null) {
			this.patch.put(dataComponentType, Optional.empty());
		} else {
			this.patch.remove(dataComponentType);
		}
	}

	public void setAll(DataComponentMap dataComponentMap) {
		for (TypedDataComponent<?> typedDataComponent : dataComponentMap) {
			typedDataComponent.applyTo(this);
		}
	}

	private void ensureMapOwnership() {
		if (this.copyOnWrite) {
			this.patch = new Reference2ObjectArrayMap<>(this.patch);
			this.copyOnWrite = false;
		}
	}

	@Override
	public Set<DataComponentType<?>> keySet() {
		if (this.patch.isEmpty()) {
			return this.prototype.keySet();
		} else {
			Set<DataComponentType<?>> set = new ReferenceArraySet<>(this.prototype.keySet());

			for (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> entry : Reference2ObjectMaps.fastIterable(this.patch)) {
				Optional<?> optional = (Optional<?>)entry.getValue();
				if (optional.isPresent()) {
					set.add((DataComponentType)entry.getKey());
				} else {
					set.remove(entry.getKey());
				}
			}

			return set;
		}
	}

	@Override
	public Iterator<TypedDataComponent<?>> iterator() {
		if (this.patch.isEmpty()) {
			return this.prototype.iterator();
		} else {
			List<TypedDataComponent<?>> list = new ArrayList(this.patch.size() + this.prototype.size());

			for (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> entry : Reference2ObjectMaps.fastIterable(this.patch)) {
				if (((Optional)entry.getValue()).isPresent()) {
					list.add(TypedDataComponent.createUnchecked((DataComponentType)entry.getKey(), ((Optional)entry.getValue()).get()));
				}
			}

			for (TypedDataComponent<?> typedDataComponent : this.prototype) {
				if (!this.patch.containsKey(typedDataComponent.type())) {
					list.add(typedDataComponent);
				}
			}

			return list.iterator();
		}
	}

	@Override
	public int size() {
		int i = this.prototype.size();

		for (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> entry : Reference2ObjectMaps.fastIterable(this.patch)) {
			boolean bl = ((Optional)entry.getValue()).isPresent();
			boolean bl2 = this.prototype.has((DataComponentType<?>)entry.getKey());
			if (bl != bl2) {
				i += bl ? 1 : -1;
			}
		}

		return i;
	}

	public DataComponentPatch asPatch() {
		if (this.patch.isEmpty()) {
			return DataComponentPatch.EMPTY;
		} else {
			this.copyOnWrite = true;
			return new DataComponentPatch(this.patch);
		}
	}

	public PatchedDataComponentMap copy() {
		this.copyOnWrite = true;
		return new PatchedDataComponentMap(this.prototype, this.patch, true);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof PatchedDataComponentMap patchedDataComponentMap
				&& this.prototype.equals(patchedDataComponentMap.prototype)
				&& this.patch.equals(patchedDataComponentMap.patch)) {
				return true;
			}

			return false;
		}
	}

	public int hashCode() {
		return this.prototype.hashCode() + this.patch.hashCode() * 31;
	}

	public String toString() {
		return "{" + (String)this.stream().map(TypedDataComponent::toString).collect(Collectors.joining(", ")) + "}";
	}
}
