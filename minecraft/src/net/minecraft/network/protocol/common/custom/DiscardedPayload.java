package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record DiscardedPayload(ResourceLocation id) implements CustomPacketPayload {
	public static <T extends FriendlyByteBuf> StreamCodec<T, DiscardedPayload> codec(ResourceLocation resourceLocation, int i) {
		return CustomPacketPayload.codec((discardedPayload, friendlyByteBuf) -> {
		}, friendlyByteBuf -> {
			int j = friendlyByteBuf.readableBytes();
			if (j >= 0 && j <= i) {
				friendlyByteBuf.skipBytes(j);
				return new DiscardedPayload(resourceLocation);
			} else {
				throw new IllegalArgumentException("Payload may not be larger than " + i + " bytes");
			}
		});
	}

	@Override
	public CustomPacketPayload.Type<DiscardedPayload> type() {
		return new CustomPacketPayload.Type<>(this.id);
	}
}
