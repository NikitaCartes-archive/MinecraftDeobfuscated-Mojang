/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.controls;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class ControlsScreen
extends OptionsSubScreen {
    private static final int ROW_SPACING = 24;

    public ControlsScreen(Screen screen, Options options) {
        super(screen, options, Component.translatable("controls.title"));
    }

    @Override
    protected void init() {
        super.init();
        int i = this.width / 2 - 155;
        int j = i + 160;
        int k = this.height / 6 - 12;
        this.addRenderableWidget(Button.builder(Component.translatable("options.mouse_settings"), button -> this.minecraft.setScreen(new MouseSettingsScreen(this, this.options))).bounds(i, k, 150, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("controls.keybinds"), button -> this.minecraft.setScreen(new KeyBindsScreen(this, this.options))).bounds(j, k, 150, 20).build());
        this.addRenderableWidget(this.options.toggleCrouch().createButton(this.options, i, k += 24, 150));
        this.addRenderableWidget(this.options.toggleSprint().createButton(this.options, j, k, 150));
        this.addRenderableWidget(this.options.autoJump().createButton(this.options, i, k += 24, 150));
        this.addRenderableWidget(this.options.operatorItemsTab().createButton(this.options, j, k, 150));
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 - 100, k += 24, 200, 20).build());
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        ControlsScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }
}

