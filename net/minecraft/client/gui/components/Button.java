/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class Button
extends AbstractButton {
    protected final OnPress onPress;

    public Button(int i, int j, int k, int l, Component component, OnPress onPress) {
        super(i, j, k, l, component);
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface OnPress {
        public void onPress(Button var1);
    }
}

