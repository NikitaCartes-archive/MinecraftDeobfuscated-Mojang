package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ServerboundCustomPayloadPacket implements Packet<ServerGamePacketListener> {
	public static final ResourceLocation BRAND = new ResourceLocation("brand");
	private final ResourceLocation identifier;
	private final FriendlyByteBuf data;

	@Environment(EnvType.CLIENT)
	public ServerboundCustomPayloadPacket(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
		this.identifier = resourceLocation;
		this.data = friendlyByteBuf;
	}

	public ServerboundCustomPayloadPacket(FriendlyByteBuf friendlyByteBuf) {
		this.identifier = friendlyByteBuf.readResourceLocation();
		int i = friendlyByteBuf.readableBytes();
		if (i >= 0 && i <= 32767) {
			this.data = new FriendlyByteBuf(friendlyByteBuf.readBytes(i));
		} else {
			throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeResourceLocation(this.identifier);
		friendlyByteBuf.writeBytes(this.data);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleCustomPayload(this);
		this.data.release();
	}
}
