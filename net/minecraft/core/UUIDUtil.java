/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.util.UUIDTypeAdapter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.Util;

public final class UUIDUtil {
    public static final Codec<UUID> CODEC = Codec.INT_STREAM.comapFlatMap(intStream -> Util.fixedSize(intStream, 4).map(UUIDUtil::uuidFromIntArray), uUID -> Arrays.stream(UUIDUtil.uuidToIntArray(uUID)));
    public static Codec<UUID> AUTHLIB_CODEC = Codec.either(CODEC, Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success(UUIDTypeAdapter.fromString(string), Lifecycle.stable());
        } catch (IllegalArgumentException illegalArgumentException) {
            return DataResult.error("Invalid UUID " + string + ": " + illegalArgumentException.getMessage());
        }
    }, UUIDTypeAdapter::fromUUID)).xmap(either -> either.map(uUID -> uUID, uUID -> uUID), Either::right);
    public static final int UUID_BYTES = 16;
    private static final String UUID_PREFIX_OFFLINE_PLAYER = "OfflinePlayer:";

    private UUIDUtil() {
    }

    public static UUID uuidFromIntArray(int[] is) {
        return new UUID((long)is[0] << 32 | (long)is[1] & 0xFFFFFFFFL, (long)is[2] << 32 | (long)is[3] & 0xFFFFFFFFL);
    }

    public static int[] uuidToIntArray(UUID uUID) {
        long l = uUID.getMostSignificantBits();
        long m = uUID.getLeastSignificantBits();
        return UUIDUtil.leastMostToIntArray(l, m);
    }

    private static int[] leastMostToIntArray(long l, long m) {
        return new int[]{(int)(l >> 32), (int)l, (int)(m >> 32), (int)m};
    }

    public static byte[] uuidToByteArray(UUID uUID) {
        byte[] bs = new byte[16];
        ByteBuffer.wrap(bs).order(ByteOrder.BIG_ENDIAN).putLong(uUID.getMostSignificantBits()).putLong(uUID.getLeastSignificantBits());
        return bs;
    }

    public static UUID readUUID(Dynamic<?> dynamic) {
        int[] is = dynamic.asIntStream().toArray();
        if (is.length != 4) {
            throw new IllegalArgumentException("Could not read UUID. Expected int-array of length 4, got " + is.length + ".");
        }
        return UUIDUtil.uuidFromIntArray(is);
    }

    public static UUID getOrCreatePlayerUUID(GameProfile gameProfile) {
        UUID uUID = gameProfile.getId();
        if (uUID == null) {
            uUID = UUIDUtil.createOfflinePlayerUUID(gameProfile.getName());
        }
        return uUID;
    }

    public static UUID createOfflinePlayerUUID(String string) {
        return UUID.nameUUIDFromBytes((UUID_PREFIX_OFFLINE_PLAYER + string).getBytes(StandardCharsets.UTF_8));
    }
}

