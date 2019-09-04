/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class UploadTokenCache {
    private static final Map<Long, String> tokenCache = Maps.newHashMap();

    public static String get(long l) {
        return tokenCache.get(l);
    }

    public static void invalidate(long l) {
        tokenCache.remove(l);
    }

    public static void put(long l, String string) {
        tokenCache.put(l, string);
    }
}

