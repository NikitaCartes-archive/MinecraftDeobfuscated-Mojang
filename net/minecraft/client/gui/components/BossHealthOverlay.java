/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;

@Environment(value=EnvType.CLIENT)
public class BossHealthOverlay
extends GuiComponent {
    private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("textures/gui/bars.png");
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int OVERLAY_OFFSET = 80;
    private final Minecraft minecraft;
    private final Map<UUID, LerpingBossEvent> events = Maps.newLinkedHashMap();

    public BossHealthOverlay(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(PoseStack poseStack) {
        if (this.events.isEmpty()) {
            return;
        }
        int i = this.minecraft.getWindow().getGuiScaledWidth();
        int j = 12;
        for (LerpingBossEvent lerpingBossEvent : this.events.values()) {
            int k = i / 2 - 91;
            int l = j;
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, GUI_BARS_LOCATION);
            this.drawBar(poseStack, k, l, lerpingBossEvent);
            Component component = lerpingBossEvent.getName();
            int m = this.minecraft.font.width(component);
            int n = i / 2 - m / 2;
            int o = l - 9;
            this.minecraft.font.drawShadow(poseStack, component, (float)n, (float)o, 0xFFFFFF);
            if ((j += 10 + this.minecraft.font.lineHeight) < this.minecraft.getWindow().getGuiScaledHeight() / 3) continue;
            break;
        }
    }

    private void drawBar(PoseStack poseStack, int i, int j, BossEvent bossEvent) {
        int k;
        this.blit(poseStack, i, j, 0, bossEvent.getColor().ordinal() * 5 * 2, 182, 5);
        if (bossEvent.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
            this.blit(poseStack, i, j, 0, 80 + (bossEvent.getOverlay().ordinal() - 1) * 5 * 2, 182, 5);
        }
        if ((k = (int)(bossEvent.getProgress() * 183.0f)) > 0) {
            this.blit(poseStack, i, j, 0, bossEvent.getColor().ordinal() * 5 * 2 + 5, k, 5);
            if (bossEvent.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
                this.blit(poseStack, i, j, 0, 80 + (bossEvent.getOverlay().ordinal() - 1) * 5 * 2 + 5, k, 5);
            }
        }
    }

    public void update(ClientboundBossEventPacket clientboundBossEventPacket) {
        clientboundBossEventPacket.dispatch(new ClientboundBossEventPacket.Handler(){

            @Override
            public void add(UUID uUID, Component component, float f, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay, boolean bl, boolean bl2, boolean bl3) {
                BossHealthOverlay.this.events.put(uUID, new LerpingBossEvent(uUID, component, f, bossBarColor, bossBarOverlay, bl, bl2, bl3));
            }

            @Override
            public void remove(UUID uUID) {
                BossHealthOverlay.this.events.remove(uUID);
            }

            @Override
            public void updateProgress(UUID uUID, float f) {
                ((LerpingBossEvent)BossHealthOverlay.this.events.get(uUID)).setProgress(f);
            }

            @Override
            public void updateName(UUID uUID, Component component) {
                ((LerpingBossEvent)BossHealthOverlay.this.events.get(uUID)).setName(component);
            }

            @Override
            public void updateStyle(UUID uUID, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay) {
                LerpingBossEvent lerpingBossEvent = (LerpingBossEvent)BossHealthOverlay.this.events.get(uUID);
                lerpingBossEvent.setColor(bossBarColor);
                lerpingBossEvent.setOverlay(bossBarOverlay);
            }

            @Override
            public void updateProperties(UUID uUID, boolean bl, boolean bl2, boolean bl3) {
                LerpingBossEvent lerpingBossEvent = (LerpingBossEvent)BossHealthOverlay.this.events.get(uUID);
                lerpingBossEvent.setDarkenScreen(bl);
                lerpingBossEvent.setPlayBossMusic(bl2);
                lerpingBossEvent.setCreateWorldFog(bl3);
            }
        });
    }

    public void reset() {
        this.events.clear();
    }

    public boolean shouldPlayMusic() {
        if (!this.events.isEmpty()) {
            for (BossEvent bossEvent : this.events.values()) {
                if (!bossEvent.shouldPlayBossMusic()) continue;
                return true;
            }
        }
        return false;
    }

    public boolean shouldDarkenScreen() {
        if (!this.events.isEmpty()) {
            for (BossEvent bossEvent : this.events.values()) {
                if (!bossEvent.shouldDarkenScreen()) continue;
                return true;
            }
        }
        return false;
    }

    public boolean shouldCreateWorldFog() {
        if (!this.events.isEmpty()) {
            for (BossEvent bossEvent : this.events.values()) {
                if (!bossEvent.shouldCreateWorldFog()) continue;
                return true;
            }
        }
        return false;
    }
}

