/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public class Blaze3D {
    public static void youJustLostTheGame() {
        MemoryUtil.memSet(0L, 0, 1L);
    }

    public static double getTime() {
        return GLFW.glfwGetTime();
    }
}

