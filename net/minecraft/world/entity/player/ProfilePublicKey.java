/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.player;

import com.google.common.base.Strings;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.time.Instant;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.ExtraCodecs;

public record ProfilePublicKey(Data data, PublicKey key) {
    public static final Codec<ProfilePublicKey> TRUSTED_CODEC = Data.CODEC.comapFlatMap(data -> {
        try {
            return DataResult.success(ProfilePublicKey.parseTrusted(data));
        } catch (CryptException cryptException) {
            return DataResult.error("Malformed public key");
        }
    }, ProfilePublicKey::data);
    private static final String PROFILE_PROPERTY_KEY = "publicKey";

    public static ProfilePublicKey parseTrusted(Data data) throws CryptException {
        return new ProfilePublicKey(data, data.parsedKey());
    }

    public static ProfilePublicKey parseAndValidate(MinecraftSessionService minecraftSessionService, Data data) throws InsecurePublicKeyException, CryptException {
        if (Strings.isNullOrEmpty(data.key())) {
            throw new InsecurePublicKeyException.MissingException();
        }
        if (data.hasExpired()) {
            throw new InsecurePublicKeyException.InvalidException("Expired profile public key");
        }
        String string = minecraftSessionService.getSecurePropertyValue(data.signedProperty());
        if (!data.signedPropertyValue().equals(string)) {
            throw new InsecurePublicKeyException.InvalidException("Invalid profile public key signature");
        }
        return ProfilePublicKey.parseTrusted(data);
    }

    public Signature verifySignature() throws CryptException {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(this.key);
            return signature;
        } catch (GeneralSecurityException generalSecurityException) {
            throw new CryptException(generalSecurityException);
        }
    }

    public record Data(Instant expiresAt, String key, String signature) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ExtraCodecs.INSTANT_ISO8601.fieldOf("expires_at")).forGetter(Data::expiresAt), ((MapCodec)Codec.STRING.fieldOf("key")).forGetter(Data::key), ((MapCodec)Codec.STRING.fieldOf("signature")).forGetter(Data::signature)).apply((Applicative<Data, ?>)instance, Data::new));

        public Property signedProperty() {
            return new Property(ProfilePublicKey.PROFILE_PROPERTY_KEY, this.signedPropertyValue(), this.signature);
        }

        public String signedPropertyValue() {
            return this.expiresAt.toEpochMilli() + this.key;
        }

        public PublicKey parsedKey() throws CryptException {
            return Crypt.stringToRsaPublicKey(this.key);
        }

        public boolean hasExpired() {
            return this.expiresAt.isBefore(Instant.now());
        }
    }
}

