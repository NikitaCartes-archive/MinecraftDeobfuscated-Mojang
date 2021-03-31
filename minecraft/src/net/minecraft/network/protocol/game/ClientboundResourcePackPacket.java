package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundResourcePackPacket implements Packet<ClientGamePacketListener> {
	public static final int MAX_HASH_LENGTH = 40;
	private final String url;
	private final String hash;
	private final boolean required;

	public ClientboundResourcePackPacket(String string, String string2, boolean bl) {
		if (string2.length() > 40) {
			throw new IllegalArgumentException("Hash is too long (max 40, was " + string2.length() + ")");
		} else {
			this.url = string;
			this.hash = string2;
			this.required = bl;
		}
	}

	public ClientboundResourcePackPacket(FriendlyByteBuf friendlyByteBuf) {
		this.url = friendlyByteBuf.readUtf();
		this.hash = friendlyByteBuf.readUtf(40);
		this.required = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.url);
		friendlyByteBuf.writeUtf(this.hash);
		friendlyByteBuf.writeBoolean(this.required);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleResourcePack(this);
	}

	public String getUrl() {
		return this.url;
	}

	public String getHash() {
		return this.hash;
	}

	public boolean isRequired() {
		return this.required;
	}
}
