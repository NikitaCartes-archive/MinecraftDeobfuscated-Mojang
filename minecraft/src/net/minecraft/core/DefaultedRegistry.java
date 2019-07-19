package net.minecraft.core;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public class DefaultedRegistry<T> extends MappedRegistry<T> {
	private final ResourceLocation defaultKey;
	private T defaultValue;

	public DefaultedRegistry(String string) {
		this.defaultKey = new ResourceLocation(string);
	}

	@Override
	public <V extends T> V registerMapping(int i, ResourceLocation resourceLocation, V object) {
		if (this.defaultKey.equals(resourceLocation)) {
			this.defaultValue = (T)object;
		}

		return super.registerMapping(i, resourceLocation, object);
	}

	@Override
	public int getId(@Nullable T object) {
		int i = super.getId(object);
		return i == -1 ? super.getId(this.defaultValue) : i;
	}

	@Nonnull
	@Override
	public ResourceLocation getKey(T object) {
		ResourceLocation resourceLocation = super.getKey(object);
		return resourceLocation == null ? this.defaultKey : resourceLocation;
	}

	@Nonnull
	@Override
	public T get(@Nullable ResourceLocation resourceLocation) {
		T object = super.get(resourceLocation);
		return object == null ? this.defaultValue : object;
	}

	@Nonnull
	@Override
	public T byId(int i) {
		T object = super.byId(i);
		return object == null ? this.defaultValue : object;
	}

	@Nonnull
	@Override
	public T getRandom(Random random) {
		T object = super.getRandom(random);
		return object == null ? this.defaultValue : object;
	}

	public ResourceLocation getDefaultKey() {
		return this.defaultKey;
	}
}
