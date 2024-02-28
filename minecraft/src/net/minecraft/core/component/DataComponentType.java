package net.minecraft.core.component;

import com.mojang.serialization.Codec;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public interface DataComponentType<T> {
	Codec<DataComponentType<?>> CODEC = ExtraCodecs.lazyInitializedCodec(() -> BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec());
	StreamCodec<RegistryFriendlyByteBuf, DataComponentType<?>> STREAM_CODEC = StreamCodec.recursive(
		streamCodec -> ByteBufCodecs.registry(Registries.DATA_COMPONENT_TYPE)
	);

	static <T> DataComponentType.Builder<T> builder() {
		return new DataComponentType.Builder<>();
	}

	@Nullable
	Codec<T> codec();

	default Codec<T> codecOrThrow() {
		Codec<T> codec = this.codec();
		if (codec == null) {
			throw new IllegalStateException(this + " is not a persistent component");
		} else {
			return codec;
		}
	}

	default boolean isTransient() {
		return this.codec() == null;
	}

	StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec();

	public static class Builder<T> {
		@Nullable
		private Codec<T> codec;
		@Nullable
		private StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;

		public DataComponentType.Builder<T> persistent(Codec<T> codec) {
			this.codec = codec;
			return this;
		}

		public DataComponentType.Builder<T> networkSynchronized(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
			this.streamCodec = streamCodec;
			return this;
		}

		public DataComponentType<T> build() {
			StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec = (StreamCodec<? super RegistryFriendlyByteBuf, T>)Objects.requireNonNullElseGet(
				this.streamCodec, () -> ByteBufCodecs.fromCodecWithRegistries((Codec<T>)Objects.requireNonNull(this.codec, "Missing Codec for component"))
			);
			return new DataComponentType.Builder.SimpleType<>(this.codec, streamCodec);
		}

		static class SimpleType<T> implements DataComponentType<T> {
			@Nullable
			private final Codec<T> codec;
			private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;

			SimpleType(@Nullable Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
				this.codec = codec;
				this.streamCodec = streamCodec;
			}

			@Nullable
			@Override
			public Codec<T> codec() {
				return this.codec;
			}

			@Override
			public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
				return this.streamCodec;
			}

			public String toString() {
				return Util.getRegisteredName((Registry<DataComponentType.Builder.SimpleType<T>>)BuiltInRegistries.DATA_COMPONENT_TYPE, this);
			}
		}
	}
}
