/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public class DisconnectedRealmsScreen
extends RealmsScreen {
    private final String title;
    private final Component reason;
    private List<String> lines;
    private final Screen parent;
    private int textHeight;

    public DisconnectedRealmsScreen(Screen screen, String string, Component component) {
        this.parent = screen;
        this.title = I18n.get(string, new Object[0]);
        this.reason = component;
    }

    @Override
    public void init() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setConnectedToRealms(false);
        minecraft.getClientPackSource().clearServerPack();
        NarrationHelper.now(this.title + ": " + this.reason.getString());
        this.lines = this.font.split(this.reason.getColoredString(), this.width - 50);
        this.textHeight = this.lines.size() * this.font.lineHeight;
        this.addButton(new Button(this.width / 2 - 100, this.height / 2 + this.textHeight / 2 + this.font.lineHeight, 200, 20, I18n.get("gui.back", new Object[0]), button -> Minecraft.getInstance().setScreen(this.parent)));
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - this.textHeight / 2 - this.font.lineHeight * 2, 0xAAAAAA);
        int k = this.height / 2 - this.textHeight / 2;
        if (this.lines != null) {
            for (String string : this.lines) {
                this.drawCenteredString(this.font, string, this.width / 2, k, 0xFFFFFF);
                k += this.font.lineHeight;
            }
        }
        super.render(i, j, f);
    }
}

