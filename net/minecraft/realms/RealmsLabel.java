/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.realms.RealmsGuiEventListener;
import net.minecraft.realms.RealmsLabelProxy;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public class RealmsLabel
extends RealmsGuiEventListener {
    private final RealmsLabelProxy proxy = new RealmsLabelProxy(this);
    private final String text;
    private final int x;
    private final int y;
    private final int color;

    public RealmsLabel(String string, int i, int j, int k) {
        this.text = string;
        this.x = i;
        this.y = j;
        this.color = k;
    }

    public void render(RealmsScreen realmsScreen) {
        realmsScreen.drawCenteredString(this.text, this.x, this.y, this.color);
    }

    @Override
    public GuiEventListener getProxy() {
        return this.proxy;
    }

    public String getText() {
        return this.text;
    }
}

