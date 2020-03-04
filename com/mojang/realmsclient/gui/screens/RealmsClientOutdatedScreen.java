/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public class RealmsClientOutdatedScreen
extends RealmsScreen {
    private final Screen lastScreen;
    private final boolean outdated;

    public RealmsClientOutdatedScreen(Screen screen, boolean bl) {
        this.lastScreen = screen;
        this.outdated = bl;
    }

    @Override
    public void init() {
        this.addButton(new Button(this.width / 2 - 100, RealmsClientOutdatedScreen.row(12), 200, 20, I18n.get("gui.back", new Object[0]), button -> this.minecraft.setScreen(this.lastScreen)));
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        String string = I18n.get(this.outdated ? "mco.client.outdated.title" : "mco.client.incompatible.title", new Object[0]);
        this.drawCenteredString(this.font, string, this.width / 2, RealmsClientOutdatedScreen.row(3), 0xFF0000);
        int k = this.outdated ? 2 : 3;
        for (int l = 0; l < k; ++l) {
            String string2 = (this.outdated ? "mco.client.outdated.msg.line" : "mco.client.incompatible.msg.line") + (l + 1);
            String string3 = I18n.get(string2, new Object[0]);
            this.drawCenteredString(this.font, string3, this.width / 2, RealmsClientOutdatedScreen.row(5) + l * 12, 0xFFFFFF);
        }
        super.render(i, j, f);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 257 || i == 335 || i == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        }
        return super.keyPressed(i, j, k);
    }
}

