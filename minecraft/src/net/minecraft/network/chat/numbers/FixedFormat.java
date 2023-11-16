package net.minecraft.network.chat.numbers;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;

public class FixedFormat implements NumberFormat {
	public static final NumberFormatType<FixedFormat> TYPE = new NumberFormatType<FixedFormat>() {
		private static final MapCodec<FixedFormat> CODEC = ComponentSerialization.CODEC.fieldOf("value").xmap(FixedFormat::new, fixedFormat -> fixedFormat.value);

		@Override
		public MapCodec<FixedFormat> mapCodec() {
			return CODEC;
		}

		public void writeToStream(FriendlyByteBuf friendlyByteBuf, FixedFormat fixedFormat) {
			friendlyByteBuf.writeComponent(fixedFormat.value);
		}

		public FixedFormat readFromStream(FriendlyByteBuf friendlyByteBuf) {
			Component component = friendlyByteBuf.readComponentTrusted();
			return new FixedFormat(component);
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
