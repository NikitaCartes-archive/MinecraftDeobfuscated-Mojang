/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.math.Vector3f;
import java.nio.FloatBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class Lighting {
    private static final FloatBuffer BUFFER = MemoryTracker.createFloatBuffer(4);
    private static final Vector3f LIGHT_0 = Lighting.createVector(0.2f, 1.0f, -0.7f);
    private static final Vector3f LIGHT_1 = Lighting.createVector(-0.2f, 1.0f, 0.7f);

    private static Vector3f createVector(float f, float g, float h) {
        Vector3f vector3f = new Vector3f(f, g, h);
        vector3f.normalize();
        return vector3f;
    }

    public static void turnOff() {
        GlStateManager.disableLighting();
        GlStateManager.disableLight(0);
        GlStateManager.disableLight(1);
        GlStateManager.disableColorMaterial();
    }

    public static void turnOn() {
        GlStateManager.enableLighting();
        GlStateManager.enableLight(0);
        GlStateManager.enableLight(1);
        GlStateManager.enableColorMaterial();
        GlStateManager.colorMaterial(1032, 5634);
        GlStateManager.light(16384, 4611, Lighting.getBuffer(LIGHT_0.x(), LIGHT_0.y(), LIGHT_0.z(), 0.0f));
        float f = 0.6f;
        GlStateManager.light(16384, 4609, Lighting.getBuffer(0.6f, 0.6f, 0.6f, 1.0f));
        GlStateManager.light(16384, 4608, Lighting.getBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        GlStateManager.light(16384, 4610, Lighting.getBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        GlStateManager.light(16385, 4611, Lighting.getBuffer(LIGHT_1.x(), LIGHT_1.y(), LIGHT_1.z(), 0.0f));
        GlStateManager.light(16385, 4609, Lighting.getBuffer(0.6f, 0.6f, 0.6f, 1.0f));
        GlStateManager.light(16385, 4608, Lighting.getBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        GlStateManager.light(16385, 4610, Lighting.getBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        GlStateManager.shadeModel(7424);
        float g = 0.4f;
        GlStateManager.lightModel(2899, Lighting.getBuffer(0.4f, 0.4f, 0.4f, 1.0f));
    }

    public static FloatBuffer getBuffer(float f, float g, float h, float i) {
        BUFFER.clear();
        BUFFER.put(f).put(g).put(h).put(i);
        BUFFER.flip();
        return BUFFER;
    }

    public static void turnOnGui() {
        GlStateManager.pushMatrix();
        GlStateManager.rotatef(-30.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotatef(165.0f, 1.0f, 0.0f, 0.0f);
        Lighting.turnOn();
        GlStateManager.popMatrix();
    }
}

