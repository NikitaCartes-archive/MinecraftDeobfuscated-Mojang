package net.minecraft.network.chat.numbers;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;

public class BlankFormat implements NumberFormat {
	public static final BlankFormat INSTANCE = new BlankFormat();
	public static final NumberFormatType<BlankFormat> TYPE = new NumberFormatType<BlankFormat>() {
		private static final MapCodec<BlankFormat> CODEC = MapCodec.unit(BlankFormat.INSTANCE);
		private static final StreamCodec<RegistryFriendlyByteBuf, BlankFormat> STREAM_CODEC = StreamCodec.unit(BlankFormat.INSTANCE);

		@Override
		public MapCodec<BlankFormat> mapCodec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, BlankFormat> streamCodec() {
			return STREAM_CODEC;
		}
	};

	@Override
	public MutableComponent format(int i) {
		return Component.empty();
	}

	@Override
	public NumberFormatType<BlankFormat> type() {
		return TYPE;
	}
}
