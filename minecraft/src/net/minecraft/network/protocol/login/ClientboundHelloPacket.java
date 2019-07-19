package net.minecraft.network.protocol.login;

import java.io.IOException;
import java.security.PublicKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;

public class ClientboundHelloPacket implements Packet<ClientLoginPacketListener> {
	private String serverId;
	private PublicKey publicKey;
	private byte[] nonce;

	public ClientboundHelloPacket() {
	}

	public ClientboundHelloPacket(String string, PublicKey publicKey, byte[] bs) {
		this.serverId = string;
		this.publicKey = publicKey;
		this.nonce = bs;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.serverId = friendlyByteBuf.readUtf(20);
		this.publicKey = Crypt.byteToPublicKey(friendlyByteBuf.readByteArray());
		this.nonce = friendlyByteBuf.readByteArray();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeUtf(this.serverId);
		friendlyByteBuf.writeByteArray(this.publicKey.getEncoded());
		friendlyByteBuf.writeByteArray(this.nonce);
	}

	public void handle(ClientLoginPacketListener clientLoginPacketListener) {
		clientLoginPacketListener.handleHello(this);
	}

	@Environment(EnvType.CLIENT)
	public String getServerId() {
		return this.serverId;
	}

	@Environment(EnvType.CLIENT)
	public PublicKey getPublicKey() {
		return this.publicKey;
	}

	@Environment(EnvType.CLIENT)
	public byte[] getNonce() {
		return this.nonce;
	}
}
