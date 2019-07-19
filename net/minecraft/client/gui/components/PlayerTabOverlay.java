/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PlayerTabOverlay
extends GuiComponent {
    private static final Ordering<PlayerInfo> PLAYER_ORDERING = Ordering.from(new PlayerInfoComparator());
    private final Minecraft minecraft;
    private final Gui gui;
    private Component footer;
    private Component header;
    private long visibilityId;
    private boolean visible;

    public PlayerTabOverlay(Minecraft minecraft, Gui gui) {
        this.minecraft = minecraft;
        this.gui = gui;
    }

    public Component getNameForDisplay(PlayerInfo playerInfo) {
        if (playerInfo.getTabListDisplayName() != null) {
            return playerInfo.getTabListDisplayName();
        }
        return PlayerTeam.formatNameForTeam(playerInfo.getTeam(), new TextComponent(playerInfo.getProfile().getName()));
    }

    public void setVisible(boolean bl) {
        if (bl && !this.visible) {
            this.visibilityId = Util.getMillis();
        }
        this.visible = bl;
    }

    public void render(int i, Scoreboard scoreboard, @Nullable Objective objective) {
        int w;
        int t;
        boolean bl;
        int m;
        int l;
        ClientPacketListener clientPacketListener = this.minecraft.player.connection;
        List<PlayerInfo> list = PLAYER_ORDERING.sortedCopy(clientPacketListener.getOnlinePlayers());
        int j = 0;
        int k = 0;
        for (PlayerInfo playerInfo : list) {
            l = this.minecraft.font.width(this.getNameForDisplay(playerInfo).getColoredString());
            j = Math.max(j, l);
            if (objective == null || objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) continue;
            l = this.minecraft.font.width(" " + scoreboard.getOrCreatePlayerScore(playerInfo.getProfile().getName(), objective).getScore());
            k = Math.max(k, l);
        }
        list = list.subList(0, Math.min(list.size(), 80));
        int n = m = list.size();
        l = 1;
        while (n > 20) {
            n = (m + ++l - 1) / l;
        }
        boolean bl2 = bl = this.minecraft.isLocalServer() || this.minecraft.getConnection().getConnection().isEncrypted();
        int o = objective != null ? (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS ? 90 : k) : 0;
        int p = Math.min(l * ((bl ? 9 : 0) + j + o + 13), i - 50) / l;
        int q = i / 2 - (p * l + (l - 1) * 5) / 2;
        int r = 10;
        int s = p * l + (l - 1) * 5;
        List<String> list2 = null;
        if (this.header != null) {
            list2 = this.minecraft.font.split(this.header.getColoredString(), i - 50);
            for (String string : list2) {
                s = Math.max(s, this.minecraft.font.width(string));
            }
        }
        List<String> list3 = null;
        if (this.footer != null) {
            list3 = this.minecraft.font.split(this.footer.getColoredString(), i - 50);
            for (String string2 : list3) {
                s = Math.max(s, this.minecraft.font.width(string2));
            }
        }
        if (list2 != null) {
            PlayerTabOverlay.fill(i / 2 - s / 2 - 1, r - 1, i / 2 + s / 2 + 1, r + list2.size() * this.minecraft.font.lineHeight, Integer.MIN_VALUE);
            for (String string2 : list2) {
                t = this.minecraft.font.width(string2);
                this.minecraft.font.drawShadow(string2, i / 2 - t / 2, r, -1);
                r += this.minecraft.font.lineHeight;
            }
            ++r;
        }
        PlayerTabOverlay.fill(i / 2 - s / 2 - 1, r - 1, i / 2 + s / 2 + 1, r + n * 9, Integer.MIN_VALUE);
        int n2 = this.minecraft.options.getBackgroundColor(0x20FFFFFF);
        for (int v = 0; v < m; ++v) {
            int ad;
            int z;
            t = v / n;
            w = v % n;
            int x = q + t * p + t * 5;
            int y = r + w * 9;
            PlayerTabOverlay.fill(x, y, x + p, y + 8, n2);
            GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableAlphaTest();
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            if (v >= list.size()) continue;
            PlayerInfo playerInfo2 = list.get(v);
            GameProfile gameProfile = playerInfo2.getProfile();
            if (bl) {
                Player player = this.minecraft.level.getPlayerByUUID(gameProfile.getId());
                boolean bl22 = player != null && player.isModelPartShown(PlayerModelPart.CAPE) && ("Dinnerbone".equals(gameProfile.getName()) || "Grumm".equals(gameProfile.getName()));
                this.minecraft.getTextureManager().bind(playerInfo2.getSkinLocation());
                z = 8 + (bl22 ? 8 : 0);
                int aa = 8 * (bl22 ? -1 : 1);
                GuiComponent.blit(x, y, 8, 8, 8.0f, z, 8, aa, 64, 64);
                if (player != null && player.isModelPartShown(PlayerModelPart.HAT)) {
                    int ab = 8 + (bl22 ? 8 : 0);
                    int ac = 8 * (bl22 ? -1 : 1);
                    GuiComponent.blit(x, y, 8, 8, 40.0f, ab, 8, ac, 64, 64);
                }
                x += 9;
            }
            String string3 = this.getNameForDisplay(playerInfo2).getColoredString();
            if (playerInfo2.getGameMode() == GameType.SPECTATOR) {
                this.minecraft.font.drawShadow((Object)((Object)ChatFormatting.ITALIC) + string3, x, y, -1862270977);
            } else {
                this.minecraft.font.drawShadow(string3, x, y, -1);
            }
            if (objective != null && playerInfo2.getGameMode() != GameType.SPECTATOR && (z = (ad = x + j + 1) + o) - ad > 5) {
                this.renderTablistScore(objective, y, gameProfile.getName(), ad, z, playerInfo2);
            }
            this.renderPingIcon(p, x - (bl ? 9 : 0), y, playerInfo2);
        }
        if (list3 != null) {
            PlayerTabOverlay.fill(i / 2 - s / 2 - 1, (r += n * 9 + 1) - 1, i / 2 + s / 2 + 1, r + list3.size() * this.minecraft.font.lineHeight, Integer.MIN_VALUE);
            for (String string4 : list3) {
                w = this.minecraft.font.width(string4);
                this.minecraft.font.drawShadow(string4, i / 2 - w / 2, r, -1);
                r += this.minecraft.font.lineHeight;
            }
        }
    }

    protected void renderPingIcon(int i, int j, int k, PlayerInfo playerInfo) {
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bind(GUI_ICONS_LOCATION);
        boolean l = false;
        int m = playerInfo.getLatency() < 0 ? 5 : (playerInfo.getLatency() < 150 ? 0 : (playerInfo.getLatency() < 300 ? 1 : (playerInfo.getLatency() < 600 ? 2 : (playerInfo.getLatency() < 1000 ? 3 : 4))));
        this.blitOffset += 100;
        this.blit(j + i - 11, k, 0, 176 + m * 8, 10, 8);
        this.blitOffset -= 100;
    }

    private void renderTablistScore(Objective objective, int i, String string, int j, int k, PlayerInfo playerInfo) {
        int l = objective.getScoreboard().getOrCreatePlayerScore(string, objective).getScore();
        if (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
            boolean bl;
            this.minecraft.getTextureManager().bind(GUI_ICONS_LOCATION);
            long m = Util.getMillis();
            if (this.visibilityId == playerInfo.getRenderVisibilityId()) {
                if (l < playerInfo.getLastHealth()) {
                    playerInfo.setLastHealthTime(m);
                    playerInfo.setHealthBlinkTime(this.gui.getGuiTicks() + 20);
                } else if (l > playerInfo.getLastHealth()) {
                    playerInfo.setLastHealthTime(m);
                    playerInfo.setHealthBlinkTime(this.gui.getGuiTicks() + 10);
                }
            }
            if (m - playerInfo.getLastHealthTime() > 1000L || this.visibilityId != playerInfo.getRenderVisibilityId()) {
                playerInfo.setLastHealth(l);
                playerInfo.setDisplayHealth(l);
                playerInfo.setLastHealthTime(m);
            }
            playerInfo.setRenderVisibilityId(this.visibilityId);
            playerInfo.setLastHealth(l);
            int n = Mth.ceil((float)Math.max(l, playerInfo.getDisplayHealth()) / 2.0f);
            int o = Math.max(Mth.ceil(l / 2), Math.max(Mth.ceil(playerInfo.getDisplayHealth() / 2), 10));
            boolean bl2 = bl = playerInfo.getHealthBlinkTime() > (long)this.gui.getGuiTicks() && (playerInfo.getHealthBlinkTime() - (long)this.gui.getGuiTicks()) / 3L % 2L == 1L;
            if (n > 0) {
                int p = Mth.floor(Math.min((float)(k - j - 4) / (float)o, 9.0f));
                if (p > 3) {
                    int q;
                    for (q = n; q < o; ++q) {
                        this.blit(j + q * p, i, bl ? 25 : 16, 0, 9, 9);
                    }
                    for (q = 0; q < n; ++q) {
                        this.blit(j + q * p, i, bl ? 25 : 16, 0, 9, 9);
                        if (bl) {
                            if (q * 2 + 1 < playerInfo.getDisplayHealth()) {
                                this.blit(j + q * p, i, 70, 0, 9, 9);
                            }
                            if (q * 2 + 1 == playerInfo.getDisplayHealth()) {
                                this.blit(j + q * p, i, 79, 0, 9, 9);
                            }
                        }
                        if (q * 2 + 1 < l) {
                            this.blit(j + q * p, i, q >= 10 ? 160 : 52, 0, 9, 9);
                        }
                        if (q * 2 + 1 != l) continue;
                        this.blit(j + q * p, i, q >= 10 ? 169 : 61, 0, 9, 9);
                    }
                } else {
                    float f = Mth.clamp((float)l / 20.0f, 0.0f, 1.0f);
                    int r = (int)((1.0f - f) * 255.0f) << 16 | (int)(f * 255.0f) << 8;
                    String string2 = "" + (float)l / 2.0f;
                    if (k - this.minecraft.font.width(string2 + "hp") >= j) {
                        string2 = string2 + "hp";
                    }
                    this.minecraft.font.drawShadow(string2, (k + j) / 2 - this.minecraft.font.width(string2) / 2, i, r);
                }
            }
        } else {
            String string3 = (Object)((Object)ChatFormatting.YELLOW) + "" + l;
            this.minecraft.font.drawShadow(string3, k - this.minecraft.font.width(string3), i, 0xFFFFFF);
        }
    }

    public void setFooter(@Nullable Component component) {
        this.footer = component;
    }

    public void setHeader(@Nullable Component component) {
        this.header = component;
    }

    public void reset() {
        this.header = null;
        this.footer = null;
    }

    @Environment(value=EnvType.CLIENT)
    static class PlayerInfoComparator
    implements Comparator<PlayerInfo> {
        private PlayerInfoComparator() {
        }

        @Override
        public int compare(PlayerInfo playerInfo, PlayerInfo playerInfo2) {
            PlayerTeam playerTeam = playerInfo.getTeam();
            PlayerTeam playerTeam2 = playerInfo2.getTeam();
            return ComparisonChain.start().compareTrueFirst(playerInfo.getGameMode() != GameType.SPECTATOR, playerInfo2.getGameMode() != GameType.SPECTATOR).compare((Comparable<?>)((Object)(playerTeam != null ? playerTeam.getName() : "")), (Comparable<?>)((Object)(playerTeam2 != null ? playerTeam2.getName() : ""))).compare(playerInfo.getProfile().getName(), playerInfo2.getProfile().getName(), String::compareToIgnoreCase).result();
        }

        @Override
        public /* synthetic */ int compare(Object object, Object object2) {
            return this.compare((PlayerInfo)object, (PlayerInfo)object2);
        }
    }
}

