/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.social;

import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SocialInteractionsPlayerList
extends ContainerObjectSelectionList<PlayerEntry> {
    private final SocialInteractionsScreen socialInteractionsScreen;
    private final List<PlayerEntry> players = Lists.newArrayList();
    @Nullable
    private String filter;

    public SocialInteractionsPlayerList(SocialInteractionsScreen socialInteractionsScreen, Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);
        this.socialInteractionsScreen = socialInteractionsScreen;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        double d = this.minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor((int)((double)this.getRowLeft() * d), (int)((double)(this.height - this.y1) * d), (int)((double)(this.getScrollbarPosition() + 6) * d), (int)((double)(this.height - (this.height - this.y1) - this.y0 - 4) * d));
        super.render(poseStack, i, j, f);
        RenderSystem.disableScissor();
    }

    public void updatePlayerList(Collection<UUID> collection, double d, boolean bl) {
        HashMap<UUID, PlayerEntry> map = new HashMap<UUID, PlayerEntry>();
        this.addOnlinePlayers(collection, map);
        this.updatePlayersFromChatLog(map, bl);
        this.updateFiltersAndScroll(map.values(), d);
    }

    private void addOnlinePlayers(Collection<UUID> collection, Map<UUID, PlayerEntry> map) {
        ClientPacketListener clientPacketListener = this.minecraft.player.connection;
        for (UUID uUID : collection) {
            PlayerInfo playerInfo = clientPacketListener.getPlayerInfo(uUID);
            if (playerInfo == null) continue;
            boolean bl = playerInfo.hasVerifiableChat();
            map.put(uUID, new PlayerEntry(this.minecraft, this.socialInteractionsScreen, uUID, playerInfo.getProfile().getName(), playerInfo::getSkinLocation, bl));
        }
    }

    private void updatePlayersFromChatLog(Map<UUID, PlayerEntry> map, boolean bl) {
        Collection<GameProfile> collection = SocialInteractionsPlayerList.collectProfilesFromChatLog(this.minecraft.getReportingContext().chatLog());
        for (GameProfile gameProfile : collection) {
            PlayerEntry playerEntry;
            if (bl) {
                playerEntry = map.computeIfAbsent(gameProfile.getId(), uUID -> {
                    PlayerEntry playerEntry = new PlayerEntry(this.minecraft, this.socialInteractionsScreen, gameProfile.getId(), gameProfile.getName(), Suppliers.memoize(() -> this.minecraft.getSkinManager().getInsecureSkinLocation(gameProfile)), true);
                    playerEntry.setRemoved(true);
                    return playerEntry;
                });
            } else {
                playerEntry = map.get(gameProfile.getId());
                if (playerEntry == null) continue;
            }
            playerEntry.setHasRecentMessages(true);
        }
    }

    private static Collection<GameProfile> collectProfilesFromChatLog(ChatLog chatLog) {
        ObjectLinkedOpenHashSet<GameProfile> set = new ObjectLinkedOpenHashSet<GameProfile>();
        for (int i = chatLog.end(); i >= chatLog.start(); --i) {
            LoggedChatMessage.Player player;
            LoggedChatEvent loggedChatEvent = chatLog.lookup(i);
            if (!(loggedChatEvent instanceof LoggedChatMessage.Player) || !(player = (LoggedChatMessage.Player)loggedChatEvent).message().hasSignature()) continue;
            set.add(player.profile());
        }
        return set;
    }

    private void sortPlayerEntries() {
        this.players.sort(Comparator.comparing(playerEntry -> {
            if (playerEntry.getPlayerId().equals(this.minecraft.getUser().getProfileId())) {
                return 0;
            }
            if (playerEntry.getPlayerId().version() == 2) {
                return 3;
            }
            if (playerEntry.hasRecentMessages()) {
                return 1;
            }
            return 2;
        }).thenComparing(playerEntry -> {
            int i;
            if (!playerEntry.getPlayerName().isBlank() && ((i = playerEntry.getPlayerName().codePointAt(0)) == 95 || i >= 97 && i <= 122 || i >= 65 && i <= 90 || i >= 48 && i <= 57)) {
                return 0;
            }
            return 1;
        }).thenComparing(PlayerEntry::getPlayerName, String::compareToIgnoreCase));
    }

    private void updateFiltersAndScroll(Collection<PlayerEntry> collection, double d) {
        this.players.clear();
        this.players.addAll(collection);
        this.sortPlayerEntries();
        this.updateFilteredPlayers();
        this.replaceEntries(this.players);
        this.setScrollAmount(d);
    }

    private void updateFilteredPlayers() {
        if (this.filter != null) {
            this.players.removeIf(playerEntry -> !playerEntry.getPlayerName().toLowerCase(Locale.ROOT).contains(this.filter));
            this.replaceEntries(this.players);
        }
    }

    public void setFilter(String string) {
        this.filter = string;
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    public void addPlayer(PlayerInfo playerInfo, SocialInteractionsScreen.Page page) {
        UUID uUID = playerInfo.getProfile().getId();
        for (PlayerEntry playerEntry : this.players) {
            if (!playerEntry.getPlayerId().equals(uUID)) continue;
            playerEntry.setRemoved(false);
            return;
        }
        if ((page == SocialInteractionsScreen.Page.ALL || this.minecraft.getPlayerSocialManager().shouldHideMessageFrom(uUID)) && (Strings.isNullOrEmpty(this.filter) || playerInfo.getProfile().getName().toLowerCase(Locale.ROOT).contains(this.filter))) {
            PlayerEntry playerEntry;
            boolean bl = playerInfo.hasVerifiableChat();
            playerEntry = new PlayerEntry(this.minecraft, this.socialInteractionsScreen, playerInfo.getProfile().getId(), playerInfo.getProfile().getName(), playerInfo::getSkinLocation, bl);
            this.addEntry(playerEntry);
            this.players.add(playerEntry);
        }
    }

    public void removePlayer(UUID uUID) {
        for (PlayerEntry playerEntry : this.players) {
            if (!playerEntry.getPlayerId().equals(uUID)) continue;
            playerEntry.setRemoved(true);
            return;
        }
    }
}

