package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundResourcePackPacket implements Packet<ClientGamePacketListener> {
	private String url;
	private String hash;
	private boolean required;

	public ClientboundResourcePackPacket() {
	}

	public ClientboundResourcePackPacket(String string, String string2, boolean bl) {
		if (string2.length() > 40) {
			throw new IllegalArgumentException("Hash is too long (max 40, was " + string2.length() + ")");
		} else {
			this.url = string;
			this.hash = string2;
			this.required = bl;
		}
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.url = friendlyByteBuf.readUtf(32767);
		this.hash = friendlyByteBuf.readUtf(40);
		this.required = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeUtf(this.url);
		friendlyByteBuf.writeUtf(this.hash);
		friendlyByteBuf.writeBoolean(this.required);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleResourcePack(this);
	}

	@Environment(EnvType.CLIENT)
	public String getUrl() {
		return this.url;
	}

	@Environment(EnvType.CLIENT)
	public String getHash() {
		return this.hash;
	}

	@Environment(EnvType.CLIENT)
	public boolean isRequired() {
		return this.required;
	}
}
