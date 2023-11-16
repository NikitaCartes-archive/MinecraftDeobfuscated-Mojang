package net.minecraft.network.chat.numbers;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.FriendlyByteBuf;

public interface NumberFormatType<T extends NumberFormat> {
	MapCodec<T> mapCodec();

	void writeToStream(FriendlyByteBuf friendlyByteBuf, T numberFormat);

	T readFromStream(FriendlyByteBuf friendlyByteBuf);
}
