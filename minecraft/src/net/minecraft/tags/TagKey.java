package net.minecraft.tags;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record TagKey<T>(ResourceKey<? extends Registry<T>> registry, ResourceLocation location) {
	private static final Interner<TagKey<?>> VALUES = Interners.newWeakInterner();

	@Deprecated
	public TagKey(ResourceKey<? extends Registry<T>> registry, ResourceLocation location) {
		this.registry = registry;
		this.location = location;
	}

	public static <T> Codec<TagKey<T>> codec(ResourceKey<? extends Registry<T>> resourceKey) {
		return ResourceLocation.CODEC.xmap(resourceLocation -> create(resourceKey, resourceLocation), TagKey::location);
	}

	public static <T> Codec<TagKey<T>> hashedCodec(ResourceKey<? extends Registry<T>> resourceKey) {
		return Codec.STRING
			.comapFlatMap(
				string -> string.startsWith("#")
						? ResourceLocation.read(string.substring(1)).map(resourceLocation -> create(resourceKey, resourceLocation))
						: DataResult.error(() -> "Not a tag id"),
				tagKey -> "#" + tagKey.location
			);
	}

	public static <T> TagKey<T> create(ResourceKey<? extends Registry<T>> resourceKey, ResourceLocation resourceLocation) {
		return (TagKey<T>)VALUES.intern(new TagKey<>(resourceKey, resourceLocation));
	}

	public boolean isFor(ResourceKey<? extends Registry<?>> resourceKey) {
		return this.registry == resourceKey;
	}

	public <E> Optional<TagKey<E>> cast(ResourceKey<? extends Registry<E>> resourceKey) {
		return this.isFor(resourceKey) ? Optional.of(this) : Optional.empty();
	}

	public String toString() {
		return "TagKey[" + this.registry.location() + " / " + this.location + "]";
	}
}
