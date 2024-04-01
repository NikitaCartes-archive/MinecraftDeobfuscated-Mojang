package net.minecraft.util;

import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Maybe;

public class SingleKeyCache<K, V> {
	private final Function<K, V> computeValue;
	@Nullable
	private K cacheKey = (K)null;
	private Maybe<V> cachedValue = Maybe.no();

	public SingleKeyCache(Function<K, V> function) {
		this.computeValue = function;
	}

	public V getValue(K object) {
		if (this.cachedValue.isEmpty() || !Objects.equals(this.cacheKey, object)) {
			this.cachedValue = Maybe.yes((V)this.computeValue.apply(object));
			this.cacheKey = object;
		}

		return this.cachedValue.getValue();
	}
}
