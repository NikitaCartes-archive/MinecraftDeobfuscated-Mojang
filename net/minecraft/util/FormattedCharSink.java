/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import net.minecraft.network.chat.Style;

@FunctionalInterface
public interface FormattedCharSink {
    public boolean accept(int var1, Style var2, int var3);
}

