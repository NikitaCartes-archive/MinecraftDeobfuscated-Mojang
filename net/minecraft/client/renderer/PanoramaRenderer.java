/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class PanoramaRenderer {
    private final Minecraft minecraft;
    private final CubeMap cubeMap;
    private float spin;
    private float bob;

    public PanoramaRenderer(CubeMap cubeMap) {
        this.cubeMap = cubeMap;
        this.minecraft = Minecraft.getInstance();
    }

    public void render(float f, float g) {
        float h = (float)((double)f * this.minecraft.options.panoramaSpeed().get());
        this.spin = PanoramaRenderer.wrap(this.spin + h * 0.1f, 360.0f);
        this.bob = PanoramaRenderer.wrap(this.bob + h * 0.001f, (float)Math.PI * 2);
        this.cubeMap.render(this.minecraft, Mth.sin(this.bob) * 5.0f + 25.0f, -this.spin, g);
    }

    private static float wrap(float f, float g) {
        return f > g ? f - g : f;
    }
}

