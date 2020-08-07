package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class DefaultedRegistry<T> extends MappedRegistry<T> {
	private final ResourceLocation defaultKey;
	private T defaultValue;

	public DefaultedRegistry(String string, ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle) {
		super(resourceKey, lifecycle);
		this.defaultKey = new ResourceLocation(string);
	}

	@Override
	public <V extends T> V registerMapping(int i, ResourceKey<T> resourceKey, V object, Lifecycle lifecycle) {
		if (this.defaultKey.equals(resourceKey.location())) {
			this.defaultValue = (T)object;
		}

		return super.registerMapping(i, resourceKey, object, lifecycle);
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

	@Override
	public Optional<T> getOptional(@Nullable ResourceLocation resourceLocation) {
		return Optional.ofNullable(super.get(resourceLocation));
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
