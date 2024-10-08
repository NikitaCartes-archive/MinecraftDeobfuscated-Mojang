package net.minecraft.util.context;

import com.google.common.collect.Sets;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nullable;
import org.jetbrains.annotations.Contract;

public class ContextMap {
	private final Map<ContextKey<?>, Object> params;

	ContextMap(Map<ContextKey<?>, Object> map) {
		this.params = map;
	}

	public boolean has(ContextKey<?> contextKey) {
		return this.params.containsKey(contextKey);
	}

	public <T> T getOrThrow(ContextKey<T> contextKey) {
		T object = (T)this.params.get(contextKey);
		if (object == null) {
			throw new NoSuchElementException(contextKey.name().toString());
		} else {
			return object;
		}
	}

	@Nullable
	public <T> T getOptional(ContextKey<T> contextKey) {
		return (T)this.params.get(contextKey);
	}

	@Nullable
	@Contract("_,!null->!null; _,_->_")
	public <T> T getOrDefault(ContextKey<T> contextKey, @Nullable T object) {
		return (T)this.params.getOrDefault(contextKey, object);
	}

	public static class Builder {
		private final Map<ContextKey<?>, Object> params = new IdentityHashMap();

		public <T> ContextMap.Builder withParameter(ContextKey<T> contextKey, T object) {
			this.params.put(contextKey, object);
			return this;
		}

		public <T> ContextMap.Builder withOptionalParameter(ContextKey<T> contextKey, @Nullable T object) {
			if (object == null) {
				this.params.remove(contextKey);
			} else {
				this.params.put(contextKey, object);
			}

			return this;
		}

		public <T> T getParameter(ContextKey<T> contextKey) {
			T object = (T)this.params.get(contextKey);
			if (object == null) {
				throw new NoSuchElementException(contextKey.name().toString());
			} else {
				return object;
			}
		}

		@Nullable
		public <T> T getOptionalParameter(ContextKey<T> contextKey) {
			return (T)this.params.get(contextKey);
		}

		public ContextMap create(ContextKeySet contextKeySet) {
			Set<ContextKey<?>> set = Sets.<ContextKey<?>>difference(this.params.keySet(), contextKeySet.allowed());
			if (!set.isEmpty()) {
				throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + set);
			} else {
				Set<ContextKey<?>> set2 = Sets.<ContextKey<?>>difference(contextKeySet.required(), this.params.keySet());
				if (!set2.isEmpty()) {
					throw new IllegalArgumentException("Missing required parameters: " + set2);
				} else {
					return new ContextMap(this.params);
				}
			}
		}
	}
}
