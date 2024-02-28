package net.minecraft.network.chat.numbers;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;

public class FixedFormat implements NumberFormat {
	public static final NumberFormatType<FixedFormat> TYPE = new NumberFormatType<FixedFormat>() {
		private static final MapCodec<FixedFormat> CODEC = ComponentSerialization.CODEC.fieldOf("value").xmap(FixedFormat::new, fixedFormat -> fixedFormat.value);
		private static final StreamCodec<RegistryFriendlyByteBuf, FixedFormat> STREAM_CODEC = StreamCodec.composite(
			ComponentSerialization.TRUSTED_STREAM_CODEC, fixedFormat -> fixedFormat.value, FixedFormat::new
		);

		@Override
		public MapCodec<FixedFormat> mapCodec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, FixedFormat> streamCodec() {
			return STREAM_CODEC;
		}
	};
	final Component value;

	public FixedFormat(Component component) {
		this.value = component;
	}

	@Override
	public MutableComponent format(int i) {
		return this.value.copy();
	}

	@Override
	public NumberFormatType<FixedFormat> type() {
		return TYPE;
	}
}
