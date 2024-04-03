package net.minecraft.util;

import java.io.Serializable;
import java.util.Deque;
import java.util.List;
import java.util.RandomAccess;
import javax.annotation.Nullable;

public interface ListAndDeque<T> extends Serializable, Cloneable, Deque<T>, List<T>, RandomAccess {
	ListAndDeque<T> reversed();

	T getFirst();

	T getLast();

	void addFirst(T object);

	void addLast(T object);

	T removeFirst();

	T removeLast();

	default boolean offer(T object) {
		return this.offerLast(object);
	}

	default T remove() {
		return this.removeFirst();
	}

	@Nullable
	default T poll() {
		return (T)this.pollFirst();
	}

	default T element() {
		return this.getFirst();
	}

	@Nullable
	default T peek() {
		return (T)this.peekFirst();
	}

	default void push(T object) {
		this.addFirst(object);
	}

	default T pop() {
		return this.removeFirst();
	}
}
