package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ServerboundCustomPayloadPacket implements Packet<ServerGamePacketListener> {
	public static final ResourceLocation BRAND = new ResourceLocation("brand");
	private ResourceLocation identifier;
	private FriendlyByteBuf data;

	public ServerboundCustomPayloadPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundCustomPayloadPacket(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
		this.identifier = resourceLocation;
		this.data = friendlyByteBuf;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.identifier = friendlyByteBuf.readResourceLocation();
		int i = friendlyByteBuf.readableBytes();
		if (i >= 0 && i <= 32767) {
			this.data = new FriendlyByteBuf(friendlyByteBuf.readBytes(i));
		} else {
			throw new IOException("Payload may not be larger than 32767 bytes");
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeResourceLocation(this.identifier);
		friendlyByteBuf.writeBytes(this.data);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleCustomPayload(this);
		if (this.data != null) {
			this.data.release();
		}
	}
}
