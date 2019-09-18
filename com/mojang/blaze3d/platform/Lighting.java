/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class Lighting {
    public static void turnOff() {
        RenderSystem.disableDiffuseLighting();
    }

    public static void turnOn() {
        RenderSystem.enableUsualDiffuseLighting();
    }

    public static void turnOnGui() {
        RenderSystem.enableGuiDiffuseLighting();
    }
}

