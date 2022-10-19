/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
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
    private static final Comparator<PlayerInfo> PLAYER_COMPARATOR = Comparator.comparing(PlayerInfo::getGameMode, Comparator.comparing(gameType -> gameType != GameType.SPECTATOR)).thenComparing(playerInfo -> Util.mapNullable(playerInfo.getTeam(), PlayerTeam::getName, ""));
    public static final int MAX_ROWS_PER_COL = 20;
    public static final int HEART_EMPTY_CONTAINER = 16;
    public static final int HEART_EMPTY_CONTAINER_BLINKING = 25;
    public static final int HEART_FULL = 52;
    public static final int HEART_HALF_FULL = 61;
    public static final int HEART_GOLDEN_FULL = 160;
    public static final int HEART_GOLDEN_HALF_FULL = 169;
    public static final int HEART_GHOST_FULL = 70;
    public static final int HEART_GHOST_HALF_FULL = 79;
    private final Minecraft minecraft;
    private final Gui gui;
    @Nullable
    private Component footer;
    @Nullable
    private Component header;
    private boolean visible;
    private final Map<UUID, HealthState> healthStates = new Object2ObjectOpenHashMap<UUID, HealthState>();

    public PlayerTabOverlay(Minecraft minecraft, Gui gui) {
        this.minecraft = minecraft;
        this.gui = gui;
    }

    public Component getNameForDisplay(PlayerInfo playerInfo) {
        if (playerInfo.getTabListDisplayName() != null) {
            return this.decorateName(playerInfo, playerInfo.getTabListDisplayName().copy());
        }
        return this.decorateName(playerInfo, PlayerTeam.formatNameForTeam(playerInfo.getTeam(), Component.literal(playerInfo.getProfile().getName())));
    }

    private Component decorateName(PlayerInfo playerInfo, MutableComponent mutableComponent) {
        return playerInfo.getGameMode() == GameType.SPECTATOR ? mutableComponent.withStyle(ChatFormatting.ITALIC) : mutableComponent;
    }

    public void setVisible(boolean bl) {
        if (this.visible != bl) {
            this.healthStates.clear();
            this.visible = bl;
        }
    }

    public void render(PoseStack poseStack, int i, Scoreboard scoreboard, @Nullable Objective objective) {
        int w;
        int t;
        boolean bl;
        int m;
        int l;
        ClientPacketListener clientPacketListener = this.minecraft.player.connection;
        List<PlayerInfo> list = clientPacketListener.getListedOnlinePlayers().stream().sorted(PLAYER_COMPARATOR).limit(80L).toList();
        int j = 0;
        int k = 0;
        for (PlayerInfo playerInfo2 : list) {
            l = this.minecraft.font.width(this.getNameForDisplay(playerInfo2));
            j = Math.max(j, l);
            if (objective == null || objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) continue;
            l = this.minecraft.font.width(" " + scoreboard.getOrCreatePlayerScore(playerInfo2.getProfile().getName(), objective).getScore());
            k = Math.max(k, l);
        }
        if (!this.healthStates.isEmpty()) {
            Set set = list.stream().map(playerInfo -> playerInfo.getProfile().getId()).collect(Collectors.toSet());
            this.healthStates.keySet().removeIf(uUID -> !set.contains(uUID));
        }
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
        List<FormattedCharSequence> list2 = null;
        if (this.header != null) {
            list2 = this.minecraft.font.split(this.header, i - 50);
            for (FormattedCharSequence formattedCharSequence : list2) {
                s = Math.max(s, this.minecraft.font.width(formattedCharSequence));
            }
        }
        List<FormattedCharSequence> list3 = null;
        if (this.footer != null) {
            list3 = this.minecraft.font.split(this.footer, i - 50);
            for (FormattedCharSequence formattedCharSequence2 : list3) {
                s = Math.max(s, this.minecraft.font.width(formattedCharSequence2));
            }
        }
        if (list2 != null) {
            PlayerTabOverlay.fill(poseStack, i / 2 - s / 2 - 1, r - 1, i / 2 + s / 2 + 1, r + list2.size() * this.minecraft.font.lineHeight, Integer.MIN_VALUE);
            for (FormattedCharSequence formattedCharSequence2 : list2) {
                t = this.minecraft.font.width(formattedCharSequence2);
                this.minecraft.font.drawShadow(poseStack, formattedCharSequence2, (float)(i / 2 - t / 2), (float)r, -1);
                r += this.minecraft.font.lineHeight;
            }
            ++r;
        }
        PlayerTabOverlay.fill(poseStack, i / 2 - s / 2 - 1, r - 1, i / 2 + s / 2 + 1, r + n * 9, Integer.MIN_VALUE);
        int n2 = this.minecraft.options.getBackgroundColor(0x20FFFFFF);
        for (int v = 0; v < m; ++v) {
            int z;
            int aa;
            t = v / n;
            w = v % n;
            int x = q + t * p + t * 5;
            int y = r + w * 9;
            PlayerTabOverlay.fill(poseStack, x, y, x + p, y + 8, n2);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            if (v >= list.size()) continue;
            PlayerInfo playerInfo2 = list.get(v);
            GameProfile gameProfile = playerInfo2.getProfile();
            if (bl) {
                Player player = this.minecraft.level.getPlayerByUUID(gameProfile.getId());
                boolean bl22 = player != null && LivingEntityRenderer.isEntityUpsideDown(player);
                boolean bl3 = player != null && player.isModelPartShown(PlayerModelPart.HAT);
                RenderSystem.setShaderTexture(0, playerInfo2.getSkinLocation());
                PlayerFaceRenderer.draw(poseStack, x, y, 8, bl3, bl22);
                x += 9;
            }
            this.minecraft.font.drawShadow(poseStack, this.getNameForDisplay(playerInfo2), (float)x, (float)y, playerInfo2.getGameMode() == GameType.SPECTATOR ? -1862270977 : -1);
            if (objective != null && playerInfo2.getGameMode() != GameType.SPECTATOR && (aa = (z = x + j + 1) + o) - z > 5) {
                this.renderTablistScore(objective, y, gameProfile.getName(), z, aa, gameProfile.getId(), poseStack);
            }
            this.renderPingIcon(poseStack, p, x - (bl ? 9 : 0), y, playerInfo2);
        }
        if (list3 != null) {
            PlayerTabOverlay.fill(poseStack, i / 2 - s / 2 - 1, (r += n * 9 + 1) - 1, i / 2 + s / 2 + 1, r + list3.size() * this.minecraft.font.lineHeight, Integer.MIN_VALUE);
            for (FormattedCharSequence formattedCharSequence3 : list3) {
                w = this.minecraft.font.width(formattedCharSequence3);
                this.minecraft.font.drawShadow(poseStack, formattedCharSequence3, (float)(i / 2 - w / 2), (float)r, -1);
                r += this.minecraft.font.lineHeight;
            }
        }
    }

    protected void renderPingIcon(PoseStack poseStack, int i, int j, int k, PlayerInfo playerInfo) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
        boolean l = false;
        int m = playerInfo.getLatency() < 0 ? 5 : (playerInfo.getLatency() < 150 ? 0 : (playerInfo.getLatency() < 300 ? 1 : (playerInfo.getLatency() < 600 ? 2 : (playerInfo.getLatency() < 1000 ? 3 : 4))));
        this.setBlitOffset(this.getBlitOffset() + 100);
        this.blit(poseStack, j + i - 11, k, 0, 176 + m * 8, 10, 8);
        this.setBlitOffset(this.getBlitOffset() - 100);
    }

    private void renderTablistScore(Objective objective, int i, String string, int j, int k, UUID uUID, PoseStack poseStack) {
        int l = objective.getScoreboard().getOrCreatePlayerScore(string, objective).getScore();
        if (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
            this.renderTablistHearts(i, j, k, uUID, poseStack, l);
            return;
        }
        String string2 = "" + ChatFormatting.YELLOW + l;
        this.minecraft.font.drawShadow(poseStack, string2, (float)(k - this.minecraft.font.width(string2)), (float)i, 0xFFFFFF);
    }

    private void renderTablistHearts(int i, int j, int k, UUID uUID2, PoseStack poseStack, int l) {
        int q;
        HealthState healthState = this.healthStates.computeIfAbsent(uUID2, uUID -> new HealthState(l));
        healthState.update(l, this.gui.getGuiTicks());
        int m = Mth.positiveCeilDiv(Math.max(l, healthState.displayedValue()), 2);
        int n = Math.max(l, Math.max(healthState.displayedValue(), 20)) / 2;
        boolean bl = healthState.isBlinking(this.gui.getGuiTicks());
        if (m <= 0) {
            return;
        }
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
        int o = Mth.floor(Math.min((float)(k - j - 4) / (float)n, 9.0f));
        if (o <= 3) {
            float f = Mth.clamp((float)l / 20.0f, 0.0f, 1.0f);
            int p = (int)((1.0f - f) * 255.0f) << 16 | (int)(f * 255.0f) << 8;
            String string = "" + (float)l / 2.0f;
            if (k - this.minecraft.font.width(string + "hp") >= j) {
                string = string + "hp";
            }
            this.minecraft.font.drawShadow(poseStack, string, (float)((k + j - this.minecraft.font.width(string)) / 2), (float)i, p);
            return;
        }
        for (q = m; q < n; ++q) {
            this.blit(poseStack, j + q * o, i, bl ? 25 : 16, 0, 9, 9);
        }
        for (q = 0; q < m; ++q) {
            this.blit(poseStack, j + q * o, i, bl ? 25 : 16, 0, 9, 9);
            if (bl) {
                if (q * 2 + 1 < healthState.displayedValue()) {
                    this.blit(poseStack, j + q * o, i, 70, 0, 9, 9);
                }
                if (q * 2 + 1 == healthState.displayedValue()) {
                    this.blit(poseStack, j + q * o, i, 79, 0, 9, 9);
                }
            }
            if (q * 2 + 1 < l) {
                this.blit(poseStack, j + q * o, i, q >= 10 ? 160 : 52, 0, 9, 9);
            }
            if (q * 2 + 1 != l) continue;
            this.blit(poseStack, j + q * o, i, q >= 10 ? 169 : 61, 0, 9, 9);
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
    static class HealthState {
        private static final long DISPLAY_UPDATE_DELAY = 20L;
        private static final long DECREASE_BLINK_DURATION = 20L;
        private static final long INCREASE_BLINK_DURATION = 10L;
        private int lastValue;
        private int displayedValue;
        private long lastUpdateTick;
        private long blinkUntilTick;

        public HealthState(int i) {
            this.displayedValue = i;
            this.lastValue = i;
        }

        public void update(int i, long l) {
            if (i != this.lastValue) {
                long m = i < this.lastValue ? 20L : 10L;
                this.blinkUntilTick = l + m;
                this.lastValue = i;
                this.lastUpdateTick = l;
            }
            if (l - this.lastUpdateTick > 20L) {
                this.displayedValue = i;
            }
        }

        public int displayedValue() {
            return this.displayedValue;
        }

        public boolean isBlinking(long l) {
            return this.blinkUntilTick > l && (this.blinkUntilTick - l) % 6L >= 3L;
        }
    }
}

