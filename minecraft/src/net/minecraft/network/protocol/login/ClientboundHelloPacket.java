package net.minecraft.network.protocol.login;

import java.security.PublicKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

public class ClientboundHelloPacket implements Packet<ClientLoginPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundHelloPacket> STREAM_CODEC = Packet.codec(
		ClientboundHelloPacket::write, ClientboundHelloPacket::new
	);
	private final String serverId;
	private final byte[] publicKey;
	private final byte[] challenge;
	private final boolean shouldAuthenticate;

	public ClientboundHelloPacket(String string, byte[] bs, byte[] cs, boolean bl) {
		this.serverId = string;
		this.publicKey = bs;
		this.challenge = cs;
		this.shouldAuthenticate = bl;
	}

	private ClientboundHelloPacket(FriendlyByteBuf friendlyByteBuf) {
		this.serverId = friendlyByteBuf.readUtf(20);
		this.publicKey = friendlyByteBuf.readByteArray();
		this.challenge = friendlyByteBuf.readByteArray();
		this.shouldAuthenticate = friendlyByteBuf.readBoolean();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.serverId);
		friendlyByteBuf.writeByteArray(this.publicKey);
		friendlyByteBuf.writeByteArray(this.challenge);
		friendlyByteBuf.writeBoolean(this.shouldAuthenticate);
	}

	@Override
	public PacketType<ClientboundHelloPacket> type() {
		return LoginPacketTypes.CLIENTBOUND_HELLO;
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

	public boolean shouldAuthenticate() {
		return this.shouldAuthenticate;
	}
}
