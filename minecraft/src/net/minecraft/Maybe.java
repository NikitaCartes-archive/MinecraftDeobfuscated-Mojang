package net.minecraft;

import java.util.Objects;

public class Maybe<T> {
	private static final Maybe<?> NO = new Maybe(null);
	private final T value;

	private Maybe(T object) {
		this.value = object;
	}

	public boolean isEmpty() {
		return this == NO;
	}

	public boolean hasValue() {
		return !this.isEmpty();
	}

	public T getValue() {
		if (this.isEmpty()) {
			throw new UnsupportedOperationException("No value");
		} else {
			return this.value;
		}
	}

	public static <T> Maybe<T> no() {
		return (Maybe<T>)NO;
	}

	public static <T> Maybe<T> yes(T object) {
		return new Maybe<>(object);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			Maybe<?> maybe = (Maybe<?>)object;
			return this.isEmpty() != maybe.isEmpty() ? false : Objects.equals(this.value, maybe.value);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return this.isEmpty() ? 0 : Objects.hashCode(this.value);
	}
}
