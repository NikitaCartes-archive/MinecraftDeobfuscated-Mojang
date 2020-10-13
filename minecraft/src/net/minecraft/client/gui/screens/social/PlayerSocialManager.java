package net.minecraft.client.gui.screens.social;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;

@Environment(EnvType.CLIENT)
public class PlayerSocialManager {
	private final Minecraft minecraft;
	private final Set<UUID> hiddenPlayers = Sets.<UUID>newHashSet();

	public PlayerSocialManager(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void hidePlayer(UUID uUID) {
		this.hiddenPlayers.add(uUID);
	}

	public void showPlayer(UUID uUID) {
		this.hiddenPlayers.remove(uUID);
	}

	public boolean isHidden(UUID uUID) {
		return this.hiddenPlayers.contains(uUID);
	}

	public Set<UUID> getHiddenPlayers() {
		return this.hiddenPlayers;
	}

	public void addPlayer(PlayerInfo playerInfo) {
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
