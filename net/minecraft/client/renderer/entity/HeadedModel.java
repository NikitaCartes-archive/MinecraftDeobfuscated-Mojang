/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;

@Environment(value=EnvType.CLIENT)
public interface HeadedModel {
    public ModelPart getHead();

    default public void translateToHead(float f) {
        this.getHead().translateTo(f);
    }
}

