/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.player;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.PrivateKey;
import java.time.Instant;
import net.minecraft.util.Crypt;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record ProfileKeyPair(PrivateKey privateKey, ProfilePublicKey publicKey, Instant refreshedAfter) {
    public static final Codec<ProfileKeyPair> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Crypt.PRIVATE_KEY_CODEC.fieldOf("private_key")).forGetter(ProfileKeyPair::privateKey), ((MapCodec)ProfilePublicKey.CODEC.fieldOf("public_key")).forGetter(ProfileKeyPair::publicKey), ((MapCodec)ExtraCodecs.INSTANT_ISO8601.fieldOf("refreshed_after")).forGetter(ProfileKeyPair::refreshedAfter)).apply((Applicative<ProfileKeyPair, ?>)instance, ProfileKeyPair::new));

    public boolean dueRefresh() {
        return this.refreshedAfter.isBefore(Instant.now());
    }
}

