/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.player;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.ExtraCodecs;

public record ProfilePublicKey(Instant expiresAt, String keyString, String signature) {
    public static final Codec<ProfilePublicKey> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ExtraCodecs.INSTANT_ISO8601.fieldOf("expires_at")).forGetter(ProfilePublicKey::expiresAt), ((MapCodec)Codec.STRING.fieldOf("key")).forGetter(ProfilePublicKey::keyString), ((MapCodec)Codec.STRING.fieldOf("signature")).forGetter(ProfilePublicKey::signature)).apply((Applicative<ProfilePublicKey, ?>)instance, ProfilePublicKey::new));
    private static final String PROFILE_PROPERTY_KEY = "publicKey";

    public ProfilePublicKey(Pair<Instant, String> pair, String string) {
        this(pair.getFirst(), pair.getSecond(), string);
    }

    public static Optional<ProfilePublicKey> parseFromGameProfile(GameProfile gameProfile) {
        Property property = Iterables.getFirst(gameProfile.getProperties().get(PROFILE_PROPERTY_KEY), null);
        if (property == null) {
            return Optional.empty();
        }
        String string = property.getValue();
        String string2 = property.getSignature();
        if (Strings.isNullOrEmpty(string) || Strings.isNullOrEmpty(string2)) {
            return Optional.empty();
        }
        return ProfilePublicKey.parsePublicKeyString(string).map(pair -> new ProfilePublicKey((Pair<Instant, String>)pair, string2));
    }

    public GameProfile fillGameProfile(GameProfile gameProfile) {
        gameProfile.getProperties().put(PROFILE_PROPERTY_KEY, this.property());
        return gameProfile;
    }

    public Trusted verify(MinecraftSessionService minecraftSessionService) throws InsecurePublicKeyException, CryptException {
        if (Strings.isNullOrEmpty(this.keyString)) {
            throw new InsecurePublicKeyException.MissingException();
        }
        String string = minecraftSessionService.getSecurePropertyValue(this.property());
        if (!(this.expiresAt.toEpochMilli() + this.keyString).equals(string)) {
            throw new InsecurePublicKeyException.InvalidException("Invalid profile public key signature");
        }
        Pair<Instant, String> pair = ProfilePublicKey.parsePublicKeyString(string).orElseThrow(() -> new InsecurePublicKeyException.InvalidException("Invalid profile public key"));
        if (pair.getFirst().isBefore(Instant.now())) {
            throw new InsecurePublicKeyException.InvalidException("Expired profile public key");
        }
        PublicKey publicKey = Crypt.stringToRsaPublicKey(pair.getSecond());
        return new Trusted(publicKey, this);
    }

    private static Optional<Pair<Instant, String>> parsePublicKeyString(String string) {
        long l;
        int i = string.indexOf("-----BEGIN RSA PUBLIC KEY-----");
        try {
            l = Long.parseLong(string.substring(0, i));
        } catch (NumberFormatException numberFormatException) {
            return Optional.empty();
        }
        return Optional.of(Pair.of(Instant.ofEpochMilli(l), string.substring(i)));
    }

    public Property property() {
        return new Property(PROFILE_PROPERTY_KEY, this.expiresAt.toEpochMilli() + this.keyString, this.signature);
    }

    public boolean hasExpired() {
        return this.expiresAt.isBefore(Instant.now());
    }

    public record Trusted(PublicKey key, ProfilePublicKey data) {
        public Signature verifySignature() throws CryptException {
            try {
                Signature signature = Signature.getInstance("SHA1withRSA");
                signature.initVerify(this.key);
                return signature;
            } catch (GeneralSecurityException generalSecurityException) {
                throw new CryptException(generalSecurityException);
            }
        }
    }
}

