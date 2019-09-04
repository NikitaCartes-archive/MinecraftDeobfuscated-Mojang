/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;

@Environment(value=EnvType.CLIENT)
public class BossHealthOverlay
extends GuiComponent {
    private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("textures/gui/bars.png");
    private final Minecraft minecraft;
    private final Map<UUID, LerpingBossEvent> events = Maps.newLinkedHashMap();

    public BossHealthOverlay(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render() {
        if (this.events.isEmpty()) {
            return;
        }
        int i = this.minecraft.window.getGuiScaledWidth();
        int j = 12;
        for (LerpingBossEvent lerpingBossEvent : this.events.values()) {
            int k = i / 2 - 91;
            int l = j;
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            this.minecraft.getTextureManager().bind(GUI_BARS_LOCATION);
            this.drawBar(k, l, lerpingBossEvent);
            String string = lerpingBossEvent.getName().getColoredString();
            int m = this.minecraft.font.width(string);
            int n = i / 2 - m / 2;
            int o = l - 9;
            this.minecraft.font.drawShadow(string, n, o, 0xFFFFFF);
            if ((j += 10 + this.minecraft.font.lineHeight) < this.minecraft.window.getGuiScaledHeight() / 3) continue;
            break;
        }
    }

    private void drawBar(int i, int j, BossEvent bossEvent) {
        int k;
        this.blit(i, j, 0, bossEvent.getColor().ordinal() * 5 * 2, 182, 5);
        if (bossEvent.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
            this.blit(i, j, 0, 80 + (bossEvent.getOverlay().ordinal() - 1) * 5 * 2, 182, 5);
        }
        if ((k = (int)(bossEvent.getPercent() * 183.0f)) > 0) {
            this.blit(i, j, 0, bossEvent.getColor().ordinal() * 5 * 2 + 5, k, 5);
            if (bossEvent.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
                this.blit(i, j, 0, 80 + (bossEvent.getOverlay().ordinal() - 1) * 5 * 2 + 5, k, 5);
            }
        }
    }

    public void update(ClientboundBossEventPacket clientboundBossEventPacket) {
        if (clientboundBossEventPacket.getOperation() == ClientboundBossEventPacket.Operation.ADD) {
            this.events.put(clientboundBossEventPacket.getId(), new LerpingBossEvent(clientboundBossEventPacket));
        } else if (clientboundBossEventPacket.getOperation() == ClientboundBossEventPacket.Operation.REMOVE) {
            this.events.remove(clientboundBossEventPacket.getId());
        } else {
            this.events.get(clientboundBossEventPacket.getId()).update(clientboundBossEventPacket);
        }
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

