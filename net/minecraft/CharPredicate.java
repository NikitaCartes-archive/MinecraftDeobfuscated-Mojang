/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft;

import java.util.Objects;

@FunctionalInterface
public interface CharPredicate {
    public boolean test(char var1);

    default public CharPredicate and(CharPredicate charPredicate) {
        Objects.requireNonNull(charPredicate);
        return c -> this.test(c) && charPredicate.test(c);
    }

    default public CharPredicate negate() {
        return c -> !this.test(c);
    }

    default public CharPredicate or(CharPredicate charPredicate) {
        Objects.requireNonNull(charPredicate);
        return c -> this.test(c) || charPredicate.test(c);
    }
}

