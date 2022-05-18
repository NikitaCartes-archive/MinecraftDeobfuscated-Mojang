/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.player;

import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureValidator;

public record ProfilePublicKey(Data data) {
    public static final Codec<ProfilePublicKey> TRUSTED_CODEC = Data.CODEC.comapFlatMap(data -> {
        try {
            return DataResult.success(ProfilePublicKey.createTrusted(data));
        } catch (CryptException cryptException) {
            return DataResult.error("Malformed public key");
        }
    }, ProfilePublicKey::data);

    public static ProfilePublicKey createTrusted(Data data) throws CryptException {
        return new ProfilePublicKey(data);
    }

    public static ProfilePublicKey createValidated(SignatureValidator signatureValidator, Data data) throws InsecurePublicKeyException, CryptException {
        if (data.hasExpired()) {
            throw new InsecurePublicKeyException.InvalidException("Expired profile public key");
        }
        if (!data.validateSignature(signatureValidator)) {
            throw new InsecurePublicKeyException.InvalidException("Invalid profile public key signature");
        }
        return ProfilePublicKey.createTrusted(data);
    }

    public SignatureValidator createSignatureValidator() {
        return SignatureValidator.from(this.data.key, "SHA256withRSA");
    }

    public record Data(Instant expiresAt, PublicKey key, byte[] keySignature) {
        private static final int MAX_KEY_SIGNATURE_SIZE = 4096;
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ExtraCodecs.INSTANT_ISO8601.fieldOf("expires_at")).forGetter(Data::expiresAt), ((MapCodec)Crypt.PUBLIC_KEY_CODEC.fieldOf("key")).forGetter(Data::key), ((MapCodec)ExtraCodecs.BASE64_STRING.fieldOf("signature")).forGetter(Data::keySignature)).apply((Applicative<Data, ?>)instance, Data::new));

        public Data(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readInstant(), friendlyByteBuf.readPublicKey(), friendlyByteBuf.readByteArray(4096));
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeInstant(this.expiresAt);
            friendlyByteBuf.writePublicKey(this.key);
            friendlyByteBuf.writeByteArray(this.keySignature);
        }

        boolean validateSignature(SignatureValidator signatureValidator) {
            return signatureValidator.validate(this.signedPayload().getBytes(StandardCharsets.US_ASCII), this.keySignature);
        }

        private String signedPayload() {
            String string = Crypt.rsaPublicKeyToString(this.key);
            return this.expiresAt.toEpochMilli() + string;
        }

        public boolean hasExpired() {
            return this.expiresAt.isBefore(Instant.now());
        }
    }
}

