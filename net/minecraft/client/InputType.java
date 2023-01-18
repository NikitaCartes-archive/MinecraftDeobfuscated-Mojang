/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public enum InputType {
    NONE,
    MOUSE,
    KEYBOARD_OTHER,
    KEYBOARD_TAB;


    public boolean isMouse() {
        return this == MOUSE;
    }

    public boolean isKeyboard() {
        return this == KEYBOARD_OTHER || this == KEYBOARD_TAB;
    }
}

