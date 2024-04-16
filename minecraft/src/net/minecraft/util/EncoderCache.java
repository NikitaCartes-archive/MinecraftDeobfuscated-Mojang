package net.minecraft.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public class EncoderCache {
	final LoadingCache<EncoderCache.Key<?, ?>, DataResult<?>> cache;

	public EncoderCache(int i) {
		this.cache = CacheBuilder.newBuilder().maximumSize((long)i).concurrencyLevel(1).softValues().build(new CacheLoader<EncoderCache.Key<?, ?>, DataResult<?>>() {
			public DataResult<?> load(EncoderCache.Key<?, ?> key) {
				return key.resolve();
			}
		});
	}

	public <A> Codec<A> wrap(Codec<A> codec) {
		return new Codec<A>() {
			@Override
			public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> dynamicOps, T object) {
				return codec.decode(dynamicOps, object);
			}

			@Override
			public <T> DataResult<T> encode(A object, DynamicOps<T> dynamicOps, T object2) {
				return (DataResult<T>)EncoderCache.this.cache.getUnchecked(new EncoderCache.Key<>(codec, object, dynamicOps));
			}
		};
	}

	static record Key<A, T>(Codec<A> codec, A value, DynamicOps<T> ops) {
		public DataResult<T> resolve() {
			return this.codec.encodeStart(this.ops, this.value);
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else {
				return !(object instanceof EncoderCache.Key<?, ?> key) ? false : this.codec == key.codec && this.value.equals(key.value) && this.ops.equals(key.ops);
			}
		}

		public int hashCode() {
			int i = System.identityHashCode(this.codec);
			i = 31 * i + this.value.hashCode();
			return 31 * i + this.ops.hashCode();
		}
	}
}
