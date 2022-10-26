/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.player;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ThrowingComponent;
import net.minecraft.util.Crypt;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureValidator;

public record ProfilePublicKey(Data data) {
    public static final Component EXPIRED_PROFILE_PUBLIC_KEY = Component.translatable("multiplayer.disconnect.expired_public_key");
    private static final Component INVALID_SIGNATURE = Component.translatable("multiplayer.disconnect.invalid_public_key_signature");
    public static final Duration EXPIRY_GRACE_PERIOD = Duration.ofHours(8L);
    public static final Codec<ProfilePublicKey> TRUSTED_CODEC = Data.CODEC.xmap(ProfilePublicKey::new, ProfilePublicKey::data);

    public static ProfilePublicKey createValidated(SignatureValidator signatureValidator, UUID uUID, Data data, Duration duration) throws ValidationException {
        if (data.hasExpired(duration)) {
            throw new ValidationException(EXPIRED_PROFILE_PUBLIC_KEY);
        }
        if (!data.validateSignature(signatureValidator, uUID)) {
            throw new ValidationException(INVALID_SIGNATURE);
        }
        return new ProfilePublicKey(data);
    }

    public SignatureValidator createSignatureValidator() {
        return SignatureValidator.from(this.data.key, "SHA256withRSA");
    }

    public record Data(Instant expiresAt, PublicKey key, byte[] keySignature) {
        private static final int MAX_KEY_SIGNATURE_SIZE = 4096;
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ExtraCodecs.INSTANT_ISO8601.fieldOf("expires_at")).forGetter(Data::expiresAt), ((MapCodec)Crypt.PUBLIC_KEY_CODEC.fieldOf("key")).forGetter(Data::key), ((MapCodec)ExtraCodecs.BASE64_STRING.fieldOf("signature_v2")).forGetter(Data::keySignature)).apply((Applicative<Data, ?>)instance, Data::new));

        public Data(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readInstant(), friendlyByteBuf.readPublicKey(), friendlyByteBuf.readByteArray(4096));
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeInstant(this.expiresAt);
            friendlyByteBuf.writePublicKey(this.key);
            friendlyByteBuf.writeByteArray(this.keySignature);
        }

        boolean validateSignature(SignatureValidator signatureValidator, UUID uUID) {
            return signatureValidator.validate(this.signedPayload(uUID), this.keySignature);
        }

        private byte[] signedPayload(UUID uUID) {
            byte[] bs = this.key.getEncoded();
            byte[] cs = new byte[24 + bs.length];
            ByteBuffer byteBuffer = ByteBuffer.wrap(cs).order(ByteOrder.BIG_ENDIAN);
            byteBuffer.putLong(uUID.getMostSignificantBits()).putLong(uUID.getLeastSignificantBits()).putLong(this.expiresAt.toEpochMilli()).put(bs);
            return cs;
        }

        public boolean hasExpired() {
            return this.expiresAt.isBefore(Instant.now());
        }

        public boolean hasExpired(Duration duration) {
            return this.expiresAt.plus(duration).isBefore(Instant.now());
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof Data) {
                Data data = (Data)object;
                return this.expiresAt.equals(data.expiresAt) && this.key.equals(data.key) && Arrays.equals(this.keySignature, data.keySignature);
            }
            return false;
        }
    }

    public static class ValidationException
    extends ThrowingComponent {
        public ValidationException(Component component) {
            super(component);
        }
    }
}

