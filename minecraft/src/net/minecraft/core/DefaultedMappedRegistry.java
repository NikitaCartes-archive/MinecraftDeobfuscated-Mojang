package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public class DefaultedMappedRegistry<T> extends MappedRegistry<T> implements DefaultedRegistry<T> {
	private final ResourceLocation defaultKey;
	private Holder.Reference<T> defaultValue;

	public DefaultedMappedRegistry(String string, ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, boolean bl) {
		super(resourceKey, lifecycle, bl);
		this.defaultKey = new ResourceLocation(string);
	}

	@Override
	public Holder.Reference<T> register(ResourceKey<T> resourceKey, T object, Lifecycle lifecycle) {
		Holder.Reference<T> reference = super.register(resourceKey, object, lifecycle);
		if (this.defaultKey.equals(resourceKey.location())) {
			this.defaultValue = reference;
		}

		return reference;
	}

	@Override
	public int getId(@Nullable T object) {
		int i = super.getId(object);
		return i == -1 ? super.getId(this.defaultValue.value()) : i;
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
		return object == null ? this.defaultValue.value() : object;
	}

	@Override
	public Optional<T> getOptional(@Nullable ResourceLocation resourceLocation) {
		return Optional.ofNullable(super.get(resourceLocation));
	}

	@Nonnull
	@Override
	public T byId(int i) {
		T object = super.byId(i);
		return object == null ? this.defaultValue.value() : object;
	}

	@Override
	public Optional<Holder.Reference<T>> getRandom(RandomSource randomSource) {
		return super.getRandom(randomSource).or(() -> Optional.of(this.defaultValue));
	}

	@Override
	public ResourceLocation getDefaultKey() {
		return this.defaultKey;
	}
}
