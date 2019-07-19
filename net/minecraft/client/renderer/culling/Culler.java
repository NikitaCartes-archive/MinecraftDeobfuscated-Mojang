/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.culling;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.AABB;

@Environment(value=EnvType.CLIENT)
public interface Culler {
    public boolean isVisible(AABB var1);

    public void prepare(double var1, double var3, double var5);
}

