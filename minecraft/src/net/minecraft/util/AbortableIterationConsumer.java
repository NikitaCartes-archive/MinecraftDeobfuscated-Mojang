package net.minecraft.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface AbortableIterationConsumer<T> {
	AbortableIterationConsumer.Continuation accept(T object);

	static <T> AbortableIterationConsumer<T> forConsumer(Consumer<T> consumer) {
		return object -> {
			consumer.accept(object);
			return AbortableIterationConsumer.Continuation.CONTINUE;
		};
	}

	public static enum Continuation {
		CONTINUE,
		ABORT;

		public boolean shouldAbort() {
			return this == ABORT;
		}
	}
}
