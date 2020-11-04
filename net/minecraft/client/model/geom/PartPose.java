/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model.geom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class PartPose {
    public static final PartPose ZERO = PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    public final float x;
    public final float y;
    public final float z;
    public final float xRot;
    public final float yRot;
    public final float zRot;

    private PartPose(float f, float g, float h, float i, float j, float k) {
        this.x = f;
        this.y = g;
        this.z = h;
        this.xRot = i;
        this.yRot = j;
        this.zRot = k;
    }

    public static PartPose offset(float f, float g, float h) {
        return PartPose.offsetAndRotation(f, g, h, 0.0f, 0.0f, 0.0f);
    }

    public static PartPose rotation(float f, float g, float h) {
        return PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, f, g, h);
    }

    public static PartPose offsetAndRotation(float f, float g, float h, float i, float j, float k) {
        return new PartPose(f, g, h, i, j, k);
    }
}

