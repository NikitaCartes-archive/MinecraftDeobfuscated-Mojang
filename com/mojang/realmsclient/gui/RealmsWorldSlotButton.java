/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.util.RealmsTextureManager;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsWorldSlotButton
extends Button {
    public static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
    public static final ResourceLocation EMPTY_SLOT_LOCATION = new ResourceLocation("realms", "textures/gui/realms/empty_frame.png");
    public static final ResourceLocation DEFAULT_WORLD_SLOT_1 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_0.png");
    public static final ResourceLocation DEFAULT_WORLD_SLOT_2 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_2.png");
    public static final ResourceLocation DEFAULT_WORLD_SLOT_3 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_3.png");
    private static final Component SLOT_ACTIVE_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip.active");
    private static final Component SWITCH_TO_MINIGAME_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip.minigame");
    private static final Component SWITCH_TO_WORLD_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip");
    private final Supplier<RealmsServer> serverDataProvider;
    private final Consumer<Component> toolTipSetter;
    private final int slotIndex;
    private int animTick;
    @Nullable
    private State state;

    public RealmsWorldSlotButton(int i, int j, int k, int l, Supplier<RealmsServer> supplier, Consumer<Component> consumer, int m, Button.OnPress onPress) {
        super(i, j, k, l, CommonComponents.EMPTY, onPress);
        this.serverDataProvider = supplier;
        this.slotIndex = m;
        this.toolTipSetter = consumer;
    }

    @Nullable
    public State getState() {
        return this.state;
    }

    public void tick() {
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
            bl2 = realmsServer.worldType == RealmsServer.WorldType.MINIGAME;
            string = "Minigame";
            l = realmsServer.minigameId;
            string2 = realmsServer.minigameImage;
            bl3 = realmsServer.minigameId == -1;
        } else {
            bl2 = realmsServer.activeSlot == this.slotIndex && realmsServer.worldType != RealmsServer.WorldType.MINIGAME;
            string = realmsWorldOptions.getSlotName(this.slotIndex);
            l = realmsWorldOptions.templateId;
            string2 = realmsWorldOptions.templateImage;
            bl3 = realmsWorldOptions.empty;
        }
        Action action = RealmsWorldSlotButton.getAction(realmsServer, bl2, bl);
        Pair<Component, Component> pair = this.getTooltipAndNarration(realmsServer, string, bl3, bl, action);
        this.state = new State(bl2, string, l, string2, bl3, bl, action, pair.getFirst());
        this.setMessage(pair.getSecond());
    }

    private static Action getAction(RealmsServer realmsServer, boolean bl, boolean bl2) {
        if (bl) {
            if (!realmsServer.expired && realmsServer.state != RealmsServer.State.UNINITIALIZED) {
                return Action.JOIN;
            }
        } else if (bl2) {
            if (!realmsServer.expired) {
                return Action.SWITCH_SLOT;
            }
        } else {
            return Action.SWITCH_SLOT;
        }
        return Action.NOTHING;
    }

    private Pair<Component, Component> getTooltipAndNarration(RealmsServer realmsServer, String string, boolean bl, boolean bl2, Action action) {
        if (action == Action.NOTHING) {
            return Pair.of(null, Component.literal(string));
        }
        Component component = bl2 ? (bl ? CommonComponents.EMPTY : Component.literal(" ").append(string).append(" ").append(realmsServer.minigameName)) : Component.literal(" ").append(string);
        Component component2 = action == Action.JOIN ? SLOT_ACTIVE_TOOLTIP : (bl2 ? SWITCH_TO_MINIGAME_SLOT_TOOLTIP : SWITCH_TO_WORLD_SLOT_TOOLTIP);
        MutableComponent component3 = component2.copy().append(component);
        return Pair.of(component2, component3);
    }

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        if (this.state == null) {
            return;
        }
        this.drawSlotFrame(poseStack, this.x, this.y, i, j, this.state.isCurrentlyActiveSlot, this.state.slotName, this.slotIndex, this.state.imageId, this.state.image, this.state.empty, this.state.minigame, this.state.action, this.state.actionPrompt);
    }

    private void drawSlotFrame(PoseStack poseStack, int i, int j, int k, int l, boolean bl, String string, int m, long n, @Nullable String string2, boolean bl2, boolean bl3, Action action, @Nullable Component component) {
        boolean bl5;
        boolean bl4 = this.isHoveredOrFocused();
        if (this.isMouseOver(k, l) && component != null) {
            this.toolTipSetter.accept(component);
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (bl3) {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(n), string2);
        } else if (bl2) {
            RenderSystem.setShaderTexture(0, EMPTY_SLOT_LOCATION);
        } else if (string2 != null && n != -1L) {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(n), string2);
        } else if (m == 1) {
            RenderSystem.setShaderTexture(0, DEFAULT_WORLD_SLOT_1);
        } else if (m == 2) {
            RenderSystem.setShaderTexture(0, DEFAULT_WORLD_SLOT_2);
        } else if (m == 3) {
            RenderSystem.setShaderTexture(0, DEFAULT_WORLD_SLOT_3);
        }
        if (bl) {
            float f = 0.85f + 0.15f * Mth.cos((float)this.animTick * 0.2f);
            RenderSystem.setShaderColor(f, f, f, 1.0f);
        } else {
            RenderSystem.setShaderColor(0.56f, 0.56f, 0.56f, 1.0f);
        }
        RealmsWorldSlotButton.blit(poseStack, i + 3, j + 3, 0.0f, 0.0f, 74, 74, 74, 74);
        RenderSystem.setShaderTexture(0, SLOT_FRAME_LOCATION);
        boolean bl6 = bl5 = bl4 && action != Action.NOTHING;
        if (bl5) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        } else if (bl) {
            RenderSystem.setShaderColor(0.8f, 0.8f, 0.8f, 1.0f);
        } else {
            RenderSystem.setShaderColor(0.56f, 0.56f, 0.56f, 1.0f);
        }
        RealmsWorldSlotButton.blit(poseStack, i, j, 0.0f, 0.0f, 80, 80, 80, 80);
        RealmsWorldSlotButton.drawCenteredString(poseStack, minecraft.font, string, i + 40, j + 66, 0xFFFFFF);
    }

    @Environment(value=EnvType.CLIENT)
    public static class State {
        final boolean isCurrentlyActiveSlot;
        final String slotName;
        final long imageId;
        @Nullable
        final String image;
        public final boolean empty;
        public final boolean minigame;
        public final Action action;
        @Nullable
        final Component actionPrompt;

        State(boolean bl, String string, long l, @Nullable String string2, boolean bl2, boolean bl3, Action action, @Nullable Component component) {
            this.isCurrentlyActiveSlot = bl;
            this.slotName = string;
            this.imageId = l;
            this.image = string2;
            this.empty = bl2;
            this.minigame = bl3;
            this.action = action;
            this.actionPrompt = component;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Action {
        NOTHING,
        SWITCH_SLOT,
        JOIN;

    }
}

