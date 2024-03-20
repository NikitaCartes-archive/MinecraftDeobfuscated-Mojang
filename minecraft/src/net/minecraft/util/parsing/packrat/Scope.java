package net.minecraft.util.parsing.packrat;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.util.Objects;
import javax.annotation.Nullable;

public final class Scope {
	private final Object2ObjectMap<Atom<?>, Object> values = new Object2ObjectArrayMap<>();

	public <T> void put(Atom<T> atom, @Nullable T object) {
		this.values.put(atom, object);
	}

	@Nullable
	public <T> T get(Atom<T> atom) {
		return (T)this.values.get(atom);
	}

	public <T> T getOrThrow(Atom<T> atom) {
		return (T)Objects.requireNonNull(this.get(atom));
	}

	public <T> T getOrDefault(Atom<T> atom, T object) {
		return (T)Objects.requireNonNullElse(this.get(atom), object);
	}

	@Nullable
	@SafeVarargs
	public final <T> T getAny(Atom<T>... atoms) {
		for (Atom<T> atom : atoms) {
			T object = this.get(atom);
			if (object != null) {
				return object;
			}
		}

		return null;
	}

	@SafeVarargs
	public final <T> T getAnyOrThrow(Atom<T>... atoms) {
		return (T)Objects.requireNonNull(this.getAny(atoms));
	}

	public String toString() {
		return this.values.toString();
	}

	public void putAll(Scope scope) {
		this.values.putAll(scope.values);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return object instanceof Scope scope ? this.values.equals(scope.values) : false;
		}
	}

	public int hashCode() {
		return this.values.hashCode();
	}
}
