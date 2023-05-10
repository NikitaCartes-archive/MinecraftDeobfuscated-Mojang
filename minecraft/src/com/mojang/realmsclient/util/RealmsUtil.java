package com.mojang.realmsclient.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class RealmsUtil {
	static final MinecraftSessionService SESSION_SERVICE = Minecraft.getInstance().getMinecraftSessionService();
	private static final Component RIGHT_NOW = Component.translatable("mco.util.time.now");
	private static final LoadingCache<String, GameProfile> GAME_PROFILE_CACHE = CacheBuilder.newBuilder()
		.expireAfterWrite(60L, TimeUnit.MINUTES)
		.build(new CacheLoader<String, GameProfile>() {
			public GameProfile load(String string) {
				return RealmsUtil.SESSION_SERVICE.fillProfileProperties(new GameProfile(UUIDTypeAdapter.fromString(string), null), false);
			}
		});
	private static final int MINUTES = 60;
	private static final int HOURS = 3600;
	private static final int DAYS = 86400;

	public static String uuidToName(String string) {
		return GAME_PROFILE_CACHE.getUnchecked(string).getName();
	}

	public static GameProfile getGameProfile(String string) {
		return GAME_PROFILE_CACHE.getUnchecked(string);
	}

	public static Component convertToAgePresentation(long l) {
		if (l < 0L) {
			return RIGHT_NOW;
		} else {
			long m = l / 1000L;
			if (m < 60L) {
				return Component.translatable("mco.time.secondsAgo", m);
			} else if (m < 3600L) {
				long n = m / 60L;
				return Component.translatable("mco.time.minutesAgo", n);
			} else if (m < 86400L) {
				long n = m / 3600L;
				return Component.translatable("mco.time.hoursAgo", n);
			} else {
				long n = m / 86400L;
				return Component.translatable("mco.time.daysAgo", n);
			}
		}
	}

	public static Component convertToAgePresentationFromInstant(Date date) {
		return convertToAgePresentation(System.currentTimeMillis() - date.getTime());
	}

	public static void renderPlayerFace(GuiGraphics guiGraphics, int i, int j, int k, String string) {
		GameProfile gameProfile = getGameProfile(string);
		ResourceLocation resourceLocation = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(gameProfile);
		PlayerFaceRenderer.draw(guiGraphics, resourceLocation, i, j, k);
	}
}
