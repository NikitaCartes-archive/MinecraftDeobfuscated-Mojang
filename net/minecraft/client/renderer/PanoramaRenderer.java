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
    private float time;

    public PanoramaRenderer(CubeMap cubeMap) {
        this.cubeMap = cubeMap;
        this.minecraft = Minecraft.getInstance();
    }

    public void render(float f, float g) {
        this.time += f;
        this.cubeMap.render(this.minecraft, Mth.sin(this.time * 0.001f) * 5.0f + 25.0f, -this.time * 0.1f, g);
        this.minecraft.getWindow().setupGuiState(Minecraft.ON_OSX);
    }
}

