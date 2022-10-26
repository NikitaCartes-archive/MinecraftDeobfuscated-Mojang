package net.minecraft.network.protocol.login;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import javax.crypto.SecretKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

public class ServerboundKeyPacket implements Packet<ServerLoginPacketListener> {
	private final byte[] keybytes;
	private final byte[] encryptedChallenge;

	public ServerboundKeyPacket(SecretKey secretKey, PublicKey publicKey, byte[] bs) throws CryptException {
		this.keybytes = Crypt.encryptUsingKey(publicKey, secretKey.getEncoded());
		this.encryptedChallenge = Crypt.encryptUsingKey(publicKey, bs);
	}

	public ServerboundKeyPacket(FriendlyByteBuf friendlyByteBuf) {
		this.keybytes = friendlyByteBuf.readByteArray();
		this.encryptedChallenge = friendlyByteBuf.readByteArray();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByteArray(this.keybytes);
		friendlyByteBuf.writeByteArray(this.encryptedChallenge);
	}

	public void handle(ServerLoginPacketListener serverLoginPacketListener) {
		serverLoginPacketListener.handleKey(this);
	}

	public SecretKey getSecretKey(PrivateKey privateKey) throws CryptException {
		return Crypt.decryptByteToSecretKey(privateKey, this.keybytes);
	}

	public boolean isChallengeValid(byte[] bs, PrivateKey privateKey) {
		try {
			return Arrays.equals(bs, Crypt.decryptUsingKey(privateKey, this.encryptedChallenge));
		} catch (CryptException var4) {
			return false;
		}
	}
}
