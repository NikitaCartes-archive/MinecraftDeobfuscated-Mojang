package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundCustomQueryPacket implements Packet<ClientLoginPacketListener> {
	private static final int MAX_PAYLOAD_SIZE = 1048576;
	private final int transactionId;
	private final ResourceLocation identifier;
	private final FriendlyByteBuf data;

	public ClientboundCustomQueryPacket(int i, ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
		this.transactionId = i;
		this.identifier = resourceLocation;
		this.data = friendlyByteBuf;
	}

	public ClientboundCustomQueryPacket(FriendlyByteBuf friendlyByteBuf) {
		this.transactionId = friendlyByteBuf.readVarInt();
		this.identifier = friendlyByteBuf.readResourceLocation();
		int i = friendlyByteBuf.readableBytes();
		if (i >= 0 && i <= 1048576) {
			this.data = new FriendlyByteBuf(friendlyByteBuf.readBytes(i));
		} else {
			throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.transactionId);
		friendlyByteBuf.writeResourceLocation(this.identifier);
		friendlyByteBuf.writeBytes(this.data.copy());
	}

	public void handle(ClientLoginPacketListener clientLoginPacketListener) {
		clientLoginPacketListener.handleCustomQuery(this);
	}

	public int getTransactionId() {
		return this.transactionId;
	}

	public ResourceLocation getIdentifier() {
		return this.identifier;
	}

	public FriendlyByteBuf getData() {
		return this.data;
	}
}
