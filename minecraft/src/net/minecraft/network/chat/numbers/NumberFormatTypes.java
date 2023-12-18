package net.minecraft.network.chat.numbers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public class NumberFormatTypes {
	public static final MapCodec<NumberFormat> MAP_CODEC = BuiltInRegistries.NUMBER_FORMAT_TYPE
		.byNameCodec()
		.dispatchMap(NumberFormat::type, numberFormatType -> numberFormatType.mapCodec().codec());
	public static final Codec<NumberFormat> CODEC = MAP_CODEC.codec();

	public static NumberFormatType<?> bootstrap(Registry<NumberFormatType<?>> registry) {
		Registry.register(registry, "blank", BlankFormat.TYPE);
		Registry.register(registry, "styled", StyledFormat.TYPE);
		return Registry.register(registry, "fixed", FixedFormat.TYPE);
	}

	public static <T extends NumberFormat> void writeToStream(FriendlyByteBuf friendlyByteBuf, T numberFormat) {
		NumberFormatType<T> numberFormatType = (NumberFormatType<T>)numberFormat.type();
		friendlyByteBuf.writeId(BuiltInRegistries.NUMBER_FORMAT_TYPE, numberFormatType);
		numberFormatType.writeToStream(friendlyByteBuf, numberFormat);
	}

	public static NumberFormat readFromStream(FriendlyByteBuf friendlyByteBuf) {
		NumberFormatType<?> numberFormatType = friendlyByteBuf.readById(BuiltInRegistries.NUMBER_FORMAT_TYPE);
		return numberFormatType.readFromStream(friendlyByteBuf);
	}
}
