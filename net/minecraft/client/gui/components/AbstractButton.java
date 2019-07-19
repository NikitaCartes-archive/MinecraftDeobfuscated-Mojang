/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractButton
extends AbstractWidget {
    public AbstractButton(int i, int j, int k, int l, String string) {
        super(i, j, k, l, string);
    }

    public abstract void onPress();

    @Override
    public void onClick(double d, double e) {
        this.onPress();
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (!this.active || !this.visible) {
            return false;
        }
        if (i == 257 || i == 32 || i == 335) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
            return true;
        }
        return false;
    }
}

