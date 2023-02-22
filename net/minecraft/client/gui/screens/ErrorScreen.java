/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class ErrorScreen
extends Screen {
    private final Component message;

    public ErrorScreen(Component component, Component component2) {
        super(component);
        this.message = component2;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(null)).bounds(this.width / 2 - 100, 140, 200, 20).build());
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        ErrorScreen.fillGradient(poseStack, 0, 0, this.width, this.height, -12574688, -11530224);
        ErrorScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 90, 0xFFFFFF);
        ErrorScreen.drawCenteredString(poseStack, this.font, this.message, this.width / 2, 110, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}

