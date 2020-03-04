package com.mojang.realmsclient.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class UploadTokenCache {
	private static final Long2ObjectMap<String> TOKEN_CACHE = new Long2ObjectOpenHashMap<>();

	public static String get(long l) {
		return TOKEN_CACHE.get(l);
	}

	public static void invalidate(long l) {
		TOKEN_CACHE.remove(l);
	}

	public static void put(long l, String string) {
		TOKEN_CACHE.put(l, string);
	}
}
