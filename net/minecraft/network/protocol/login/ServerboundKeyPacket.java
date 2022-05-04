/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.login;

import com.mojang.datafixers.util.Either;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Optional;
import javax.crypto.SecretKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfilePublicKey;

public class ServerboundKeyPacket
implements Packet<ServerLoginPacketListener> {
    private final byte[] keybytes;
    private final Either<byte[], Crypt.SaltSignaturePair> nonceOrSaltSignature;

    public ServerboundKeyPacket(SecretKey secretKey, PublicKey publicKey, byte[] bs) throws CryptException {
        this.keybytes = Crypt.encryptUsingKey(publicKey, secretKey.getEncoded());
        this.nonceOrSaltSignature = Either.left(Crypt.encryptUsingKey(publicKey, bs));
    }

    public ServerboundKeyPacket(SecretKey secretKey, PublicKey publicKey, long l, byte[] bs) throws CryptException {
        this.keybytes = Crypt.encryptUsingKey(publicKey, secretKey.getEncoded());
        this.nonceOrSaltSignature = Either.right(new Crypt.SaltSignaturePair(l, bs));
    }

    public ServerboundKeyPacket(FriendlyByteBuf friendlyByteBuf) {
        this.keybytes = friendlyByteBuf.readByteArray();
        this.nonceOrSaltSignature = friendlyByteBuf.readEither(FriendlyByteBuf::readByteArray, Crypt.SaltSignaturePair::new);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeByteArray(this.keybytes);
        friendlyByteBuf2.writeEither(this.nonceOrSaltSignature, FriendlyByteBuf::writeByteArray, (friendlyByteBuf, saltSignaturePair) -> saltSignaturePair.write((FriendlyByteBuf)friendlyByteBuf));
    }

    @Override
    public void handle(ServerLoginPacketListener serverLoginPacketListener) {
        serverLoginPacketListener.handleKey(this);
    }

    public SecretKey getSecretKey(PrivateKey privateKey) throws CryptException {
        return Crypt.decryptByteToSecretKey(privateKey, this.keybytes);
    }

    public boolean isChallengeSignatureValid(byte[] bs2, ProfilePublicKey profilePublicKey) {
        return this.nonceOrSaltSignature.map(bs -> false, saltSignaturePair -> {
            try {
                Signature signature = profilePublicKey.verifySignature();
                signature.update(bs2);
                signature.update(saltSignaturePair.saltAsBytes());
                return signature.verify(saltSignaturePair.signature());
            } catch (GeneralSecurityException | CryptException exception) {
                return false;
            }
        });
    }

    public boolean isNonceValid(byte[] bs, PrivateKey privateKey) {
        Optional<byte[]> optional = this.nonceOrSaltSignature.left();
        try {
            return optional.isPresent() && Arrays.equals(bs, Crypt.decryptUsingKey(privateKey, optional.get()));
        } catch (CryptException cryptException) {
            return false;
        }
    }
}

