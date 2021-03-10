/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;

@Environment(value=EnvType.CLIENT)
public class Lighting {
    private static final Vector3f DIFFUSE_LIGHT_0 = Util.make(new Vector3f(0.2f, 1.0f, -0.7f), Vector3f::normalize);
    private static final Vector3f DIFFUSE_LIGHT_1 = Util.make(new Vector3f(-0.2f, 1.0f, 0.7f), Vector3f::normalize);
    private static final Vector3f NETHER_DIFFUSE_LIGHT_0 = Util.make(new Vector3f(0.2f, 1.0f, -0.7f), Vector3f::normalize);
    private static final Vector3f NETHER_DIFFUSE_LIGHT_1 = Util.make(new Vector3f(-0.2f, -1.0f, 0.7f), Vector3f::normalize);

    public static void setupNetherLevel(Matrix4f matrix4f) {
        RenderSystem.setupLevelDiffuseLighting(NETHER_DIFFUSE_LIGHT_0, NETHER_DIFFUSE_LIGHT_1, matrix4f);
    }

    public static void setupLevel(Matrix4f matrix4f) {
        RenderSystem.setupLevelDiffuseLighting(DIFFUSE_LIGHT_0, DIFFUSE_LIGHT_1, matrix4f);
    }

    public static void setupForFlatItems() {
        RenderSystem.setupGuiFlatDiffuseLighting(DIFFUSE_LIGHT_0, DIFFUSE_LIGHT_1);
    }

    public static void setupFor3DItems() {
        RenderSystem.setupGui3DDiffuseLighting(DIFFUSE_LIGHT_0, DIFFUSE_LIGHT_1);
    }
}

