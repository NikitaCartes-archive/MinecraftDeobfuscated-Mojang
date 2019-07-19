/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Option;
import net.minecraft.client.gui.components.Button;

@Environment(value=EnvType.CLIENT)
public class OptionButton
extends Button {
    private final Option option;

    public OptionButton(int i, int j, int k, int l, Option option, String string, Button.OnPress onPress) {
        super(i, j, k, l, string, onPress);
        this.option = option;
    }
}

