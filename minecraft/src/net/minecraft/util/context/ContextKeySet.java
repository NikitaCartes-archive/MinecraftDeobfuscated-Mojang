package net.minecraft.util.context;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.util.Set;

public class ContextKeySet {
	private final Set<ContextKey<?>> required;
	private final Set<ContextKey<?>> allowed;

	ContextKeySet(Set<ContextKey<?>> set, Set<ContextKey<?>> set2) {
		this.required = Set.copyOf(set);
		this.allowed = Set.copyOf(Sets.union(set, set2));
	}

	public Set<ContextKey<?>> required() {
		return this.required;
	}

	public Set<ContextKey<?>> allowed() {
		return this.allowed;
	}

	public String toString() {
		return "["
			+ Joiner.on(", ").join(this.allowed.stream().map(contextKey -> (this.required.contains(contextKey) ? "!" : "") + contextKey.name()).iterator())
			+ "]";
	}

	public static class Builder {
		private final Set<ContextKey<?>> required = Sets.newIdentityHashSet();
		private final Set<ContextKey<?>> optional = Sets.newIdentityHashSet();

		public ContextKeySet.Builder required(ContextKey<?> contextKey) {
			if (this.optional.contains(contextKey)) {
				throw new IllegalArgumentException("Parameter " + contextKey.name() + " is already optional");
			} else {
				this.required.add(contextKey);
				return this;
			}
		}

		public ContextKeySet.Builder optional(ContextKey<?> contextKey) {
			if (this.required.contains(contextKey)) {
				throw new IllegalArgumentException("Parameter " + contextKey.name() + " is already required");
			} else {
				this.optional.add(contextKey);
				return this;
			}
		}

		public ContextKeySet build() {
			return new ContextKeySet(this.required, this.optional);
		}
	}
}
