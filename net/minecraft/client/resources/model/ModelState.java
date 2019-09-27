/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.model;

import com.mojang.math.Transformation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface ModelState {
    default public Transformation getRotation() {
        return Transformation.identity();
    }

    default public boolean isUvLocked() {
        return false;
    }
}

