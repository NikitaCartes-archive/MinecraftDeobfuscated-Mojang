package net.minecraft.core;

import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface WritableRegistry<T> extends Registry<T> {
	Holder.Reference<T> register(ResourceKey<T> resourceKey, T object, RegistrationInfo registrationInfo);

	void bindTag(TagKey<T> tagKey, List<Holder<T>> list);

	boolean isEmpty();

	HolderGetter<T> createRegistrationLookup();
}
