/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface WindowEventHandler {
    public void setWindowActive(boolean var1);

    public void updateDisplay(boolean var1);

    public void resizeDisplay();
}

