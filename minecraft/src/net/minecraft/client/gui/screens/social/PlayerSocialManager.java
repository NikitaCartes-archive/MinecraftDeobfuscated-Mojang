package net.minecraft.client.gui.screens.social;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.SocialInteractionsService;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;

@Environment(EnvType.CLIENT)
public class PlayerSocialManager {
	private final Minecraft minecraft;
	private final Set<UUID> hiddenPlayers = Sets.<UUID>newHashSet();
	private final SocialInteractionsService service;
	private final Map<String, UUID> discoveredNamesToUUID = Maps.<String, UUID>newHashMap();

	public PlayerSocialManager(Minecraft minecraft, SocialInteractionsService socialInteractionsService) {
		this.minecraft = minecraft;
		this.service = socialInteractionsService;
	}

	public void hidePlayer(UUID uUID) {
		this.hiddenPlayers.add(uUID);
	}

	public void showPlayer(UUID uUID) {
		this.hiddenPlayers.remove(uUID);
	}

	public boolean shouldHideMessageFrom(UUID uUID) {
		return this.isHidden(uUID) || this.isBlocked(uUID);
	}

	public boolean isHidden(UUID uUID) {
		return this.hiddenPlayers.contains(uUID);
	}

	public boolean isBlocked(UUID uUID) {
		return this.service.isBlockedPlayer(uUID);
	}

	public Set<UUID> getHiddenPlayers() {
		return this.hiddenPlayers;
	}

	public UUID getDiscoveredUUID(String string) {
		return (UUID)this.discoveredNamesToUUID.getOrDefault(string, Util.NIL_UUID);
	}

	public void addPlayer(PlayerInfo playerInfo) {
		GameProfile gameProfile = playerInfo.getProfile();
		if (gameProfile.isComplete()) {
			this.discoveredNamesToUUID.put(gameProfile.getName(), gameProfile.getId());
		}

		Screen screen = this.minecraft.screen;
		if (screen instanceof SocialInteractionsScreen) {
			SocialInteractionsScreen socialInteractionsScreen = (SocialInteractionsScreen)screen;
			socialInteractionsScreen.onAddPlayer(playerInfo);
		}
	}

	public void removePlayer(UUID uUID) {
		Screen screen = this.minecraft.screen;
		if (screen instanceof SocialInteractionsScreen) {
			SocialInteractionsScreen socialInteractionsScreen = (SocialInteractionsScreen)screen;
			socialInteractionsScreen.onRemovePlayer(uUID);
		}
	}
}
