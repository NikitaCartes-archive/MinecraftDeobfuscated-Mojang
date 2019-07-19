/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.culling;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.FrustumData;
import net.minecraft.world.phys.AABB;

@Environment(value=EnvType.CLIENT)
public class FrustumCuller
implements Culler {
    private final FrustumData frustum;
    private double xOff;
    private double yOff;
    private double zOff;

    public FrustumCuller() {
        this(Frustum.getFrustum());
    }

    public FrustumCuller(FrustumData frustumData) {
        this.frustum = frustumData;
    }

    @Override
    public void prepare(double d, double e, double f) {
        this.xOff = d;
        this.yOff = e;
        this.zOff = f;
    }

    public boolean cubeInFrustum(double d, double e, double f, double g, double h, double i) {
        return this.frustum.cubeInFrustum(d - this.xOff, e - this.yOff, f - this.zOff, g - this.xOff, h - this.yOff, i - this.zOff);
    }

    @Override
    public boolean isVisible(AABB aABB) {
        return this.cubeInFrustum(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ);
    }
}

