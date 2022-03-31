/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public interface OptionEnum {
    public int getId();

    public String getKey();

    default public Component getCaption() {
        return new TranslatableComponent(this.getKey());
    }
}

