/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public abstract class ColorableHierarchicalModel<E extends Entity>
extends HierarchicalModel<E> {
    private float r = 1.0f;
    private float g = 1.0f;
    private float b = 1.0f;

    public void setColor(float f, float g, float h) {
        this.r = f;
        this.g = g;
        this.b = h;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        super.renderToBuffer(poseStack, vertexConsumer, i, j, this.r * f, this.g * g, this.b * h, k);
    }
}

