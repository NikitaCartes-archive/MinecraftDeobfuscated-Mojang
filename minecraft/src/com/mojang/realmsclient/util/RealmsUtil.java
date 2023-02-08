package com.mojang.realmsclient.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class RealmsUtil {
	static final MinecraftSessionService SESSION_SERVICE = Minecraft.getInstance().getMinecraftSessionService();
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

	public static String convertToAgePresentation(long l) {
		if (l < 0L) {
			return "right now";
		} else {
			long m = l / 1000L;
			if (m < 60L) {
				return (m == 1L ? "1 second" : m + " seconds") + " ago";
			} else if (m < 3600L) {
				long n = m / 60L;
				return (n == 1L ? "1 minute" : n + " minutes") + " ago";
			} else if (m < 86400L) {
				long n = m / 3600L;
				return (n == 1L ? "1 hour" : n + " hours") + " ago";
			} else {
				long n = m / 86400L;
				return (n == 1L ? "1 day" : n + " days") + " ago";
			}
		}
	}

	public static String convertToAgePresentationFromInstant(Date date) {
		return convertToAgePresentation(System.currentTimeMillis() - date.getTime());
	}

	public static void renderPlayerFace(PoseStack poseStack, int i, int j, int k, String string) {
		GameProfile gameProfile = getGameProfile(string);
		ResourceLocation resourceLocation = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(gameProfile);
		RenderSystem.setShaderTexture(0, resourceLocation);
		PlayerFaceRenderer.draw(poseStack, i, j, k);
	}
}
