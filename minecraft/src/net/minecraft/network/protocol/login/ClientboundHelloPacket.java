package net.minecraft.network.protocol.login;

import java.io.IOException;
import java.security.PublicKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

public class ClientboundHelloPacket implements Packet<ClientLoginPacketListener> {
	private String serverId;
	private byte[] publicKey;
	private byte[] nonce;

	public ClientboundHelloPacket() {
	}

	public ClientboundHelloPacket(String string, byte[] bs, byte[] cs) {
		this.serverId = string;
		this.publicKey = bs;
		this.nonce = cs;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.serverId = friendlyByteBuf.readUtf(20);
		this.publicKey = friendlyByteBuf.readByteArray();
		this.nonce = friendlyByteBuf.readByteArray();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeUtf(this.serverId);
		friendlyByteBuf.writeByteArray(this.publicKey);
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
	public PublicKey getPublicKey() throws CryptException {
		return Crypt.byteToPublicKey(this.publicKey);
	}

	@Environment(EnvType.CLIENT)
	public byte[] getNonce() {
		return this.nonce;
	}
}
