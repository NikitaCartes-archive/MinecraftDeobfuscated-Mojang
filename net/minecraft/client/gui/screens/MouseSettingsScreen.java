/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class MouseSettingsScreen
extends OptionsSubScreen {
    private OptionsList list;

    private static OptionInstance<?>[] options(Options options) {
        return new OptionInstance[]{options.sensitivity(), options.invertYMouse(), options.mouseWheelSensitivity(), options.discreteMouseScroll(), options.touchscreen()};
    }

    public MouseSettingsScreen(Screen screen, Options options) {
        super(screen, options, Component.translatable("options.mouse_settings.title"));
    }

    @Override
    protected void init() {
        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        if (InputConstants.isRawMouseInputSupported()) {
            this.list.addSmall((OptionInstance[])Stream.concat(Arrays.stream(MouseSettingsScreen.options(this.options)), Stream.of(this.options.rawMouseInput())).toArray(OptionInstance[]::new));
        } else {
            this.list.addSmall(MouseSettingsScreen.options(this.options));
        }
        this.addWidget(this.list);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.options.save();
            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        this.list.render(poseStack, i, j, f);
        MouseSettingsScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 5, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }
}

