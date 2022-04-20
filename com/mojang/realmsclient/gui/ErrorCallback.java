/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public interface ErrorCallback {
    public void error(Component var1);

    default public void error(String string) {
        this.error(Component.literal(string));
    }
}

