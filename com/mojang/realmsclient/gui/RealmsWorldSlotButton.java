/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.util.RealmsTextureManager;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsButtonProxy;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsWorldSlotButton
extends RealmsButton {
    private final Supplier<RealmsServer> serverDataProvider;
    private final Consumer<String> toolTipSetter;
    private final Listener listener;
    private final int slotIndex;
    private int animTick;
    private State state;

    public RealmsWorldSlotButton(int i, int j, int k, int l, Supplier<RealmsServer> supplier, Consumer<String> consumer, int m, int n, Listener listener) {
        super(m, i, j, k, l, "");
        this.serverDataProvider = supplier;
        this.slotIndex = n;
        this.toolTipSetter = consumer;
        this.listener = listener;
    }

    @Override
    public void render(int i, int j, float f) {
        super.render(i, j, f);
    }

    @Override
    public void tick() {
        Action action;
        boolean bl3;
        String string2;
        long l;
        String string;
        boolean bl2;
        boolean bl;
        ++this.animTick;
        RealmsServer realmsServer = this.serverDataProvider.get();
        if (realmsServer == null) {
            return;
        }
        RealmsWorldOptions realmsWorldOptions = realmsServer.slots.get(this.slotIndex);
        boolean bl4 = bl = this.slotIndex == 4;
        if (bl) {
            bl2 = realmsServer.worldType.equals((Object)RealmsServer.WorldType.MINIGAME);
            string = "Minigame";
            l = realmsServer.minigameId;
            string2 = realmsServer.minigameImage;
            bl3 = realmsServer.minigameId == -1;
        } else {
            bl2 = realmsServer.activeSlot == this.slotIndex && !realmsServer.worldType.equals((Object)RealmsServer.WorldType.MINIGAME);
            string = realmsWorldOptions.getSlotName(this.slotIndex);
            l = realmsWorldOptions.templateId;
            string2 = realmsWorldOptions.templateImage;
            bl3 = realmsWorldOptions.empty;
        }
        String string3 = null;
        if (bl2) {
            boolean bl42;
            boolean bl5 = bl42 = realmsServer.state == RealmsServer.State.OPEN || realmsServer.state == RealmsServer.State.CLOSED;
            if (realmsServer.expired || !bl42) {
                action = Action.NOTHING;
            } else {
                action = Action.JOIN;
                string3 = Realms.getLocalizedString("mco.configure.world.slot.tooltip.active", new Object[0]);
            }
        } else if (bl) {
            if (realmsServer.expired) {
                action = Action.NOTHING;
            } else {
                action = Action.SWITCH_SLOT;
                string3 = Realms.getLocalizedString("mco.configure.world.slot.tooltip.minigame", new Object[0]);
            }
        } else {
            action = Action.SWITCH_SLOT;
            string3 = Realms.getLocalizedString("mco.configure.world.slot.tooltip", new Object[0]);
        }
        this.state = new State(bl2, string, l, string2, bl3, bl, action, string3);
        String string4 = action == Action.NOTHING ? string : (bl ? (bl3 ? string3 : string3 + " " + string + " " + realmsServer.minigameName) : string3 + " " + string);
        this.setMessage(string4);
    }

    @Override
    public void renderButton(int i, int j, float f) {
        if (this.state == null) {
            return;
        }
        RealmsButtonProxy realmsButtonProxy = this.getProxy();
        this.drawSlotFrame(realmsButtonProxy.x, realmsButtonProxy.y, i, j, this.state.isCurrentlyActiveSlot, this.state.slotName, this.slotIndex, this.state.imageId, this.state.image, this.state.empty, this.state.minigame, this.state.action, this.state.actionPrompt);
    }

    private void drawSlotFrame(int i, int j, int k, int l, boolean bl, String string, int m, long n, @Nullable String string2, boolean bl2, boolean bl3, Action action, @Nullable String string3) {
        boolean bl5;
        boolean bl4 = this.getProxy().isHovered();
        if (this.getProxy().isMouseOver(k, l) && string3 != null) {
            this.toolTipSetter.accept(string3);
        }
        if (bl3) {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(n), string2);
        } else if (bl2) {
            Realms.bind("realms:textures/gui/realms/empty_frame.png");
        } else if (string2 != null && n != -1L) {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(n), string2);
        } else if (m == 1) {
            Realms.bind("textures/gui/title/background/panorama_0.png");
        } else if (m == 2) {
            Realms.bind("textures/gui/title/background/panorama_2.png");
        } else if (m == 3) {
            Realms.bind("textures/gui/title/background/panorama_3.png");
        }
        if (bl) {
            float f = 0.85f + 0.15f * RealmsMth.cos((float)this.animTick * 0.2f);
            GlStateManager.color4f(f, f, f, 1.0f);
        } else {
            GlStateManager.color4f(0.56f, 0.56f, 0.56f, 1.0f);
        }
        RealmsScreen.blit(i + 3, j + 3, 0.0f, 0.0f, 74, 74, 74, 74);
        Realms.bind("realms:textures/gui/realms/slot_frame.png");
        boolean bl6 = bl5 = bl4 && action != Action.NOTHING;
        if (bl5) {
            GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        } else if (bl) {
            GlStateManager.color4f(0.8f, 0.8f, 0.8f, 1.0f);
        } else {
            GlStateManager.color4f(0.56f, 0.56f, 0.56f, 1.0f);
        }
        RealmsScreen.blit(i, j, 0.0f, 0.0f, 80, 80, 80, 80);
        this.drawCenteredString(string, i + 40, j + 66, 0xFFFFFF);
    }

    @Override
    public void onPress() {
        this.listener.onSlotClick(this.slotIndex, this.state.action, this.state.minigame, this.state.empty);
    }

    @Environment(value=EnvType.CLIENT)
    public static class State {
        final boolean isCurrentlyActiveSlot;
        final String slotName;
        final long imageId;
        public final String image;
        public final boolean empty;
        final boolean minigame;
        public final Action action;
        final String actionPrompt;

        State(boolean bl, String string, long l, @Nullable String string2, boolean bl2, boolean bl3, @NotNull Action action, @Nullable String string3) {
            this.isCurrentlyActiveSlot = bl;
            this.slotName = string;
            this.imageId = l;
            this.image = string2;
            this.empty = bl2;
            this.minigame = bl3;
            this.action = action;
            this.actionPrompt = string3;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Action {
        NOTHING,
        SWITCH_SLOT,
        JOIN;

    }

    @Environment(value=EnvType.CLIENT)
    public static interface Listener {
        public void onSlotClick(int var1, @NotNull Action var2, boolean var3, boolean var4);
    }
}

