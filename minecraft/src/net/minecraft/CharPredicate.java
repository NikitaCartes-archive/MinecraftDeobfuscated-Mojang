package net.minecraft;

import java.util.Objects;

@FunctionalInterface
public interface CharPredicate {
	boolean test(char c);

	default CharPredicate and(CharPredicate charPredicate) {
		Objects.requireNonNull(charPredicate);
		return c -> this.test(c) && charPredicate.test(c);
	}

	default CharPredicate negate() {
		return c -> !this.test(c);
	}

	default CharPredicate or(CharPredicate charPredicate) {
		Objects.requireNonNull(charPredicate);
		return c -> this.test(c) || charPredicate.test(c);
	}
}
