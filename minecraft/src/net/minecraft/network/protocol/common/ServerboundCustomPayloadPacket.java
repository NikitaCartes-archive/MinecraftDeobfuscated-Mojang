package net.minecraft.network.protocol.common;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundCustomPayloadPacket(CustomPacketPayload payload) implements Packet<ServerCommonPacketListener> {
	private static final int MAX_PAYLOAD_SIZE = 32767;
	private static final Map<ResourceLocation, FriendlyByteBuf.Reader<? extends CustomPacketPayload>> KNOWN_TYPES = ImmutableMap.<ResourceLocation, FriendlyByteBuf.Reader<? extends CustomPacketPayload>>builder()
		.put(BrandPayload.ID, BrandPayload::new)
		.build();

	public ServerboundCustomPayloadPacket(FriendlyByteBuf friendlyByteBuf) {
		this(readPayload(friendlyByteBuf.readResourceLocation(), friendlyByteBuf));
	}

	private static CustomPacketPayload readPayload(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
		FriendlyByteBuf.Reader<? extends CustomPacketPayload> reader = (FriendlyByteBuf.Reader<? extends CustomPacketPayload>)KNOWN_TYPES.get(resourceLocation);
		return (CustomPacketPayload)(reader != null ? (CustomPacketPayload)reader.apply(friendlyByteBuf) : readUnknownPayload(resourceLocation, friendlyByteBuf));
	}

	private static DiscardedPayload readUnknownPayload(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
		int i = friendlyByteBuf.readableBytes();
		if (i >= 0 && i <= 32767) {
			friendlyByteBuf.skipBytes(i);
			return new DiscardedPayload(resourceLocation);
		} else {
			throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeResourceLocation(this.payload.id());
		this.payload.write(friendlyByteBuf);
	}

	public void handle(ServerCommonPacketListener serverCommonPacketListener) {
		serverCommonPacketListener.handleCustomPayload(this);
	}
}
