/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public abstract class EntityModel<T extends Entity>
extends Model {
    public float attackTime;
    public boolean riding;
    public boolean young = true;

    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i) {
        this.renderToBuffer(poseStack, vertexConsumer, i, 1.0f, 1.0f, 1.0f);
    }

    public abstract void renderToBuffer(PoseStack var1, VertexConsumer var2, int var3, float var4, float var5, float var6);

    public abstract void setupAnim(T var1, float var2, float var3, float var4, float var5, float var6, float var7);

    public void prepareMobModel(T entity, float f, float g, float h) {
    }

    public void copyPropertiesTo(EntityModel<T> entityModel) {
        entityModel.attackTime = this.attackTime;
        entityModel.riding = this.riding;
        entityModel.young = this.young;
    }
}

