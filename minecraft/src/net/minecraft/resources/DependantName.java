package net.minecraft.resources;

@FunctionalInterface
public interface DependantName<T, V> {
	V get(ResourceKey<T> resourceKey);

	static <T, V> DependantName<T, V> fixed(V object) {
		return resourceKey -> object;
	}
}
