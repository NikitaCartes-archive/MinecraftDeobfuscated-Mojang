package net.minecraft.client.gui.screens.social;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.UserApiService;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

@Environment(EnvType.CLIENT)
public class PlayerSocialManager {
	private final Minecraft minecraft;
	private final Set<UUID> hiddenPlayers = Sets.<UUID>newHashSet();
	private final UserApiService service;
	private final Map<String, UUID> discoveredNamesToUUID = Maps.<String, UUID>newHashMap();
	private boolean onlineMode;
	private CompletableFuture<?> pendingBlockListRefresh = CompletableFuture.completedFuture(null);

	public PlayerSocialManager(Minecraft minecraft, UserApiService userApiService) {
		this.minecraft = minecraft;
		this.service = userApiService;
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

	public void startOnlineMode() {
		this.onlineMode = true;
		this.pendingBlockListRefresh = this.pendingBlockListRefresh.thenRunAsync(this.service::refreshBlockList, Util.ioPool());
	}

	public void stopOnlineMode() {
		this.onlineMode = false;
	}

	public boolean isBlocked(UUID uUID) {
		if (!this.onlineMode) {
			return false;
		} else {
			this.pendingBlockListRefresh.join();
			return this.service.isBlockedPlayer(uUID);
		}
	}

	public Set<UUID> getHiddenPlayers() {
		return this.hiddenPlayers;
	}

	public UUID getDiscoveredUUID(String string) {
		return (UUID)this.discoveredNamesToUUID.getOrDefault(string, Util.NIL_UUID);
	}

	public void addPlayer(PlayerInfo playerInfo) {
		GameProfile gameProfile = playerInfo.getProfile();
		this.discoveredNamesToUUID.put(gameProfile.getName(), gameProfile.getId());
		if (this.minecraft.screen instanceof SocialInteractionsScreen socialInteractionsScreen) {
			socialInteractionsScreen.onAddPlayer(playerInfo);
		}
	}

	public void removePlayer(UUID uUID) {
		if (this.minecraft.screen instanceof SocialInteractionsScreen socialInteractionsScreen) {
			socialInteractionsScreen.onRemovePlayer(uUID);
		}
	}
}
