/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.BlockModelRotation;

@Environment(value=EnvType.CLIENT)
public interface ModelState {
    default public BlockModelRotation getRotation() {
        return BlockModelRotation.X0_Y0;
    }

    default public boolean isUvLocked() {
        return false;
    }
}

