/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.platform.GLX;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class GlUtil {
    public static String getVendor() {
        return "GLU.getVendor";
    }

    public static String getCpuInfo() {
        return GLX._getCpuInfo();
    }

    public static String getRenderer() {
        return "GLU.getRenderer";
    }

    public static String getOpenGLVersion() {
        return "GLU.getOpenGLVersion";
    }
}

