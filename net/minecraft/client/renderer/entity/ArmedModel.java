/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.HumanoidArm;

@Environment(value=EnvType.CLIENT)
public interface ArmedModel {
    public void translateToHand(float var1, HumanoidArm var2);
}

