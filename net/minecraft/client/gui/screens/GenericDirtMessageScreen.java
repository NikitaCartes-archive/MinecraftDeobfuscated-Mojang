/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class GenericDirtMessageScreen
extends Screen {
    public GenericDirtMessageScreen(Component component) {
        super(component);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(poseStack);
        GenericDirtMessageScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 70, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }
}

