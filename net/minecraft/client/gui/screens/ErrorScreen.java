/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class ErrorScreen
extends Screen {
    private final String message;

    public ErrorScreen(Component component, String string) {
        super(component);
        this.message = string;
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new Button(this.width / 2 - 100, 140, 200, 20, I18n.get("gui.cancel", new Object[0]), button -> this.minecraft.setScreen(null)));
    }

    @Override
    public void render(int i, int j, float f) {
        this.fillGradient(0, 0, this.width, this.height, -12574688, -11530224);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 90, 0xFFFFFF);
        this.drawCenteredString(this.font, this.message, this.width / 2, 110, 0xFFFFFF);
        super.render(i, j, f);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}

