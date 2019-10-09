/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class OptionsSubScreen
extends Screen {
    protected final Screen lastScreen;
    protected final Options options;

    public OptionsSubScreen(Screen screen, Options options, Component component) {
        super(component);
        this.lastScreen = screen;
        this.options = options;
    }

    @Override
    public void removed() {
        this.minecraft.options.save();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }
}

