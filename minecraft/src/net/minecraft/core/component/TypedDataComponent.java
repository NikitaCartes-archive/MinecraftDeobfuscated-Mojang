package net.minecraft.core.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Map.Entry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record TypedDataComponent<T>(DataComponentType<T> type, T value) {
	public static final StreamCodec<RegistryFriendlyByteBuf, TypedDataComponent<?>> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, TypedDataComponent<?>>() {
		public TypedDataComponent<?> decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			DataComponentType<?> dataComponentType = DataComponentType.STREAM_CODEC.decode(registryFriendlyByteBuf);
			return decodeTyped(registryFriendlyByteBuf, (DataComponentType<T>)dataComponentType);
		}

		private static <T> TypedDataComponent<T> decodeTyped(RegistryFriendlyByteBuf registryFriendlyByteBuf, DataComponentType<T> dataComponentType) {
			return new TypedDataComponent<>(dataComponentType, dataComponentType.streamCodec().decode(registryFriendlyByteBuf));
		}

		public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, TypedDataComponent<?> typedDataComponent) {
			encodeCap(registryFriendlyByteBuf, (TypedDataComponent<T>)typedDataComponent);
		}

		private static <T> void encodeCap(RegistryFriendlyByteBuf registryFriendlyByteBuf, TypedDataComponent<T> typedDataComponent) {
			DataComponentType.STREAM_CODEC.encode(registryFriendlyByteBuf, typedDataComponent.type());
			typedDataComponent.type().streamCodec().encode(registryFriendlyByteBuf, typedDataComponent.value());
		}
	};

	static TypedDataComponent<?> fromEntryUnchecked(Entry<DataComponentType<?>, Object> entry) {
		return createUnchecked((DataComponentType<T>)entry.getKey(), entry.getValue());
	}

	public static <T> TypedDataComponent<T> createUnchecked(DataComponentType<T> dataComponentType, Object object) {
		return new TypedDataComponent<>(dataComponentType, (T)object);
	}

	public void applyTo(PatchedDataComponentMap patchedDataComponentMap) {
		patchedDataComponentMap.set(this.type, this.value);
	}

	public <D> DataResult<D> encodeValue(DynamicOps<D> dynamicOps) {
		Codec<T> codec = this.type.codec();
		return codec == null ? DataResult.error(() -> "Component of type " + this.type + " is not encodable") : codec.encodeStart(dynamicOps, this.value);
	}

	public String toString() {
		return this.type + "=>" + this.value;
	}
}
