package net.minecraft.core;

import net.minecraft.resources.ResourceLocation;

public abstract class WritableRegistry<T> extends Registry<T> {
	public abstract <V extends T> V registerMapping(int i, ResourceLocation resourceLocation, V object);

	public abstract <V extends T> V register(ResourceLocation resourceLocation, V object);

	public abstract boolean isEmpty();
}
