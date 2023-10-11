package net.minecraft.commands;

public interface CommandResultConsumer<T> {
	void storeResult(T object, boolean bl, int i);
}
