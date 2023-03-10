/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public class RealmsGenericErrorScreen
extends RealmsScreen {
    private final Screen nextScreen;
    private final Pair<Component, Component> lines;
    private MultiLineLabel line2Split = MultiLineLabel.EMPTY;

    public RealmsGenericErrorScreen(RealmsServiceException realmsServiceException, Screen screen) {
        super(GameNarrator.NO_TITLE);
        this.nextScreen = screen;
        this.lines = RealmsGenericErrorScreen.errorMessage(realmsServiceException);
    }

    public RealmsGenericErrorScreen(Component component, Screen screen) {
        super(GameNarrator.NO_TITLE);
        this.nextScreen = screen;
        this.lines = RealmsGenericErrorScreen.errorMessage(component);
    }

    public RealmsGenericErrorScreen(Component component, Component component2, Screen screen) {
        super(GameNarrator.NO_TITLE);
        this.nextScreen = screen;
        this.lines = RealmsGenericErrorScreen.errorMessage(component, component2);
    }

    private static Pair<Component, Component> errorMessage(RealmsServiceException realmsServiceException) {
        if (realmsServiceException.realmsError == null) {
            return Pair.of(Component.literal("An error occurred (" + realmsServiceException.httpResultCode + "):"), Component.literal(realmsServiceException.rawResponse));
        }
        String string = "mco.errorMessage." + realmsServiceException.realmsError.getErrorCode();
        return Pair.of(Component.literal("Realms (" + realmsServiceException.realmsError + "):"), I18n.exists(string) ? Component.translatable(string) : Component.nullToEmpty(realmsServiceException.realmsError.getErrorMessage()));
    }

    private static Pair<Component, Component> errorMessage(Component component) {
        return Pair.of(Component.literal("An error occurred: "), component);
    }

    private static Pair<Component, Component> errorMessage(Component component, Component component2) {
        return Pair.of(component, component2);
    }

    @Override
    public void init() {
        this.addRenderableWidget(Button.builder(Component.literal("Ok"), button -> this.minecraft.setScreen(this.nextScreen)).bounds(this.width / 2 - 100, this.height - 52, 200, 20).build());
        this.line2Split = MultiLineLabel.create(this.font, (FormattedText)this.lines.getSecond(), this.width * 3 / 4);
    }

    @Override
    public Component getNarrationMessage() {
        return Component.empty().append(this.lines.getFirst()).append(": ").append(this.lines.getSecond());
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        RealmsGenericErrorScreen.drawCenteredString(poseStack, this.font, this.lines.getFirst(), this.width / 2, 80, 0xFFFFFF);
        this.line2Split.renderCentered(poseStack, this.width / 2, 100, this.minecraft.font.lineHeight, 0xFF0000);
        super.render(poseStack, i, j, f);
    }
}

