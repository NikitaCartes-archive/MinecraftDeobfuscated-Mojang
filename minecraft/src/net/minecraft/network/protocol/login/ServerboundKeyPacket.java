package net.minecraft.network.protocol.login;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;

public class ServerboundKeyPacket implements Packet<ServerLoginPacketListener> {
	private byte[] keybytes = new byte[0];
	private byte[] nonce = new byte[0];

	public ServerboundKeyPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundKeyPacket(SecretKey secretKey, PublicKey publicKey, byte[] bs) {
		this.keybytes = Crypt.encryptUsingKey(publicKey, secretKey.getEncoded());
		this.nonce = Crypt.encryptUsingKey(publicKey, bs);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.keybytes = friendlyByteBuf.readByteArray();
		this.nonce = friendlyByteBuf.readByteArray();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeByteArray(this.keybytes);
		friendlyByteBuf.writeByteArray(this.nonce);
	}

	public void handle(ServerLoginPacketListener serverLoginPacketListener) {
		serverLoginPacketListener.handleKey(this);
	}

	public SecretKey getSecretKey(PrivateKey privateKey) {
		return Crypt.decryptByteToSecretKey(privateKey, this.keybytes);
	}

	public byte[] getNonce(PrivateKey privateKey) {
		return privateKey == null ? this.nonce : Crypt.decryptUsingKey(privateKey, this.nonce);
	}
}
