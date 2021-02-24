package net.minecraft.network.protocol.login;

import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

public class ServerboundKeyPacket implements Packet<ServerLoginPacketListener> {
	private final byte[] keybytes;
	private final byte[] nonce;

	@Environment(EnvType.CLIENT)
	public ServerboundKeyPacket(SecretKey secretKey, PublicKey publicKey, byte[] bs) throws CryptException {
		this.keybytes = Crypt.encryptUsingKey(publicKey, secretKey.getEncoded());
		this.nonce = Crypt.encryptUsingKey(publicKey, bs);
	}

	public ServerboundKeyPacket(FriendlyByteBuf friendlyByteBuf) {
		this.keybytes = friendlyByteBuf.readByteArray();
		this.nonce = friendlyByteBuf.readByteArray();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByteArray(this.keybytes);
		friendlyByteBuf.writeByteArray(this.nonce);
	}

	public void handle(ServerLoginPacketListener serverLoginPacketListener) {
		serverLoginPacketListener.handleKey(this);
	}

	public SecretKey getSecretKey(PrivateKey privateKey) throws CryptException {
		return Crypt.decryptByteToSecretKey(privateKey, this.keybytes);
	}

	public byte[] getNonce(PrivateKey privateKey) throws CryptException {
		return Crypt.decryptUsingKey(privateKey, this.nonce);
	}
}
