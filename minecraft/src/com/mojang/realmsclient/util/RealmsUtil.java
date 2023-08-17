package com.mojang.realmsclient.util;

import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.Date;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class RealmsUtil {
	private static final Component RIGHT_NOW = Component.translatable("mco.util.time.now");
	private static final int MINUTES = 60;
	private static final int HOURS = 3600;
	private static final int DAYS = 86400;

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

	public static void renderPlayerFace(GuiGraphics guiGraphics, int i, int j, int k, UUID uUID) {
		Minecraft minecraft = Minecraft.getInstance();
		ProfileResult profileResult = minecraft.getMinecraftSessionService().fetchProfile(uUID, false);
		PlayerSkin playerSkin = profileResult != null ? minecraft.getSkinManager().getInsecureSkin(profileResult.profile()) : DefaultPlayerSkin.get(uUID);
		PlayerFaceRenderer.draw(guiGraphics, playerSkin.texture(), i, j, k);
	}
}
