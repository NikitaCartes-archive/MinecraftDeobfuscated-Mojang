package net.minecraft.network.protocol.login;

import java.security.PublicKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

public class ClientboundHelloPacket implements Packet<ClientLoginPacketListener> {
	private final String serverId;
	private final byte[] publicKey;
	private final byte[] challenge;

	public ClientboundHelloPacket(String string, byte[] bs, byte[] cs) {
		this.serverId = string;
		this.publicKey = bs;
		this.challenge = cs;
	}

	public ClientboundHelloPacket(FriendlyByteBuf friendlyByteBuf) {
		this.serverId = friendlyByteBuf.readUtf(20);
		this.publicKey = friendlyByteBuf.readByteArray();
		this.challenge = friendlyByteBuf.readByteArray();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.serverId);
		friendlyByteBuf.writeByteArray(this.publicKey);
		friendlyByteBuf.writeByteArray(this.challenge);
	}

	public void handle(ClientLoginPacketListener clientLoginPacketListener) {
		clientLoginPacketListener.handleHello(this);
	}

	public String getServerId() {
		return this.serverId;
	}

	public PublicKey getPublicKey() throws CryptException {
		return Crypt.byteToPublicKey(this.publicKey);
	}

	public byte[] getChallenge() {
		return this.challenge;
	}
}
