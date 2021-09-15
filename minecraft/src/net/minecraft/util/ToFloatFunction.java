package net.minecraft.util;

@FunctionalInterface
public interface ToFloatFunction<C> {
	float apply(C object);
}
