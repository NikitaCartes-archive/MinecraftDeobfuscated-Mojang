/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.PlayerInfo;

@Environment(value=EnvType.CLIENT)
public class PlayerSocialManager {
    private final Minecraft minecraft;
    private final Set<UUID> hiddenPlayers = Sets.newHashSet();
    private final UserApiService service;
    private final Map<String, UUID> discoveredNamesToUUID = Maps.newHashMap();
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
        }
        this.pendingBlockListRefresh.join();
        return this.service.isBlockedPlayer(uUID);
    }

    public Set<UUID> getHiddenPlayers() {
        return this.hiddenPlayers;
    }

    public UUID getDiscoveredUUID(String string) {
        return this.discoveredNamesToUUID.getOrDefault(string, Util.NIL_UUID);
    }

    public void addPlayer(PlayerInfo playerInfo) {
        Screen screen;
        GameProfile gameProfile = playerInfo.getProfile();
        if (gameProfile.isComplete()) {
            this.discoveredNamesToUUID.put(gameProfile.getName(), gameProfile.getId());
        }
        if ((screen = this.minecraft.screen) instanceof SocialInteractionsScreen) {
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

