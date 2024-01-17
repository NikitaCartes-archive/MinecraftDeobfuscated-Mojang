package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ClientInformation;

public record CommonListenerCookie(GameProfile gameProfile, int latency, ClientInformation clientInformation, boolean transferred) {
	public static CommonListenerCookie createInitial(GameProfile gameProfile, boolean bl) {
		return new CommonListenerCookie(gameProfile, 0, ClientInformation.createDefault(), bl);
	}
}
