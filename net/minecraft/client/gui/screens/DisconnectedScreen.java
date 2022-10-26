/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

@Environment(value=EnvType.CLIENT)
public class DisconnectedScreen
extends Screen {
    private final Component reason;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private final Screen parent;
    private int textHeight;

    public DisconnectedScreen(Screen screen, Component component, Component component2) {
        super(component);
        this.parent = screen;
        this.reason = component2;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        this.message = MultiLineLabel.create(this.font, (FormattedText)this.reason, this.width - 50);
        this.textHeight = this.message.getLineCount() * this.font.lineHeight;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.toMenu"), button -> this.minecraft.setScreen(this.parent)).bounds(this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + this.font.lineHeight, this.height - 30), 200, 20).build());
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        DisconnectedScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.height / 2 - this.textHeight / 2 - this.font.lineHeight * 2, 0xAAAAAA);
        this.message.renderCentered(poseStack, this.width / 2, this.height / 2 - this.textHeight / 2);
        super.render(poseStack, i, j, f);
    }
}

