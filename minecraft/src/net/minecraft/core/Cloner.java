package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.JavaOps;

public class Cloner<T> {
	private final Codec<T> directCodec;

	Cloner(Codec<T> codec) {
		this.directCodec = codec;
	}

	public T clone(T object, HolderLookup.Provider provider, HolderLookup.Provider provider2) {
		DynamicOps<Object> dynamicOps = RegistryOps.create(JavaOps.INSTANCE, provider);
		DynamicOps<Object> dynamicOps2 = RegistryOps.create(JavaOps.INSTANCE, provider2);
		Object object2 = Util.getOrThrow(this.directCodec.encodeStart(dynamicOps, object), string -> new IllegalStateException("Failed to encode: " + string));
		return Util.getOrThrow(this.directCodec.parse(dynamicOps2, object2), string -> new IllegalStateException("Failed to decode: " + string));
	}

	public static class Factory {
		private final Map<ResourceKey<? extends Registry<?>>, Cloner<?>> codecs = new HashMap();

		public <T> Cloner.Factory addCodec(ResourceKey<? extends Registry<? extends T>> resourceKey, Codec<T> codec) {
			this.codecs.put(resourceKey, new Cloner<>(codec));
			return this;
		}

		@Nullable
		public <T> Cloner<T> cloner(ResourceKey<? extends Registry<? extends T>> resourceKey) {
			return (Cloner<T>)this.codecs.get(resourceKey);
		}
	}
}
