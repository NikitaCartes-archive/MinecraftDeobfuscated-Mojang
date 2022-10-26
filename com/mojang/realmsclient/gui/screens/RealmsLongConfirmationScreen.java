/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public class RealmsLongConfirmationScreen
extends RealmsScreen {
    private final Type type;
    private final Component line2;
    private final Component line3;
    protected final BooleanConsumer callback;
    private final boolean yesNoQuestion;

    public RealmsLongConfirmationScreen(BooleanConsumer booleanConsumer, Type type, Component component, Component component2, boolean bl) {
        super(GameNarrator.NO_TITLE);
        this.callback = booleanConsumer;
        this.type = type;
        this.line2 = component;
        this.line3 = component2;
        this.yesNoQuestion = bl;
    }

    @Override
    public void init() {
        if (this.yesNoQuestion) {
            this.addRenderableWidget(Button.builder(CommonComponents.GUI_YES, button -> this.callback.accept(true)).bounds(this.width / 2 - 105, RealmsLongConfirmationScreen.row(8), 100, 20).build());
            this.addRenderableWidget(Button.builder(CommonComponents.GUI_NO, button -> this.callback.accept(false)).bounds(this.width / 2 + 5, RealmsLongConfirmationScreen.row(8), 100, 20).build());
        } else {
            this.addRenderableWidget(Button.builder(Component.translatable("mco.gui.ok"), button -> this.callback.accept(true)).bounds(this.width / 2 - 50, RealmsLongConfirmationScreen.row(8), 100, 20).build());
        }
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinLines(this.type.text, this.line2, this.line3);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.callback.accept(false);
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        RealmsLongConfirmationScreen.drawCenteredString(poseStack, this.font, this.type.text, this.width / 2, RealmsLongConfirmationScreen.row(2), this.type.colorCode);
        RealmsLongConfirmationScreen.drawCenteredString(poseStack, this.font, this.line2, this.width / 2, RealmsLongConfirmationScreen.row(4), 0xFFFFFF);
        RealmsLongConfirmationScreen.drawCenteredString(poseStack, this.font, this.line3, this.width / 2, RealmsLongConfirmationScreen.row(6), 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        Warning("Warning!", 0xFF0000),
        Info("Info!", 8226750);

        public final int colorCode;
        public final Component text;

        private Type(String string2, int j) {
            this.text = Component.literal(string2);
            this.colorCode = j;
        }
    }
}

