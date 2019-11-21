/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;

@Environment(value=EnvType.CLIENT)
public class SkullModel
extends Model {
    protected final ModelPart head;

    public SkullModel() {
        this(0, 35, 64, 64);
    }

    public SkullModel(int i, int j, int k, int l) {
        super(RenderType::entityTranslucent);
        this.texWidth = k;
        this.texHeight = l;
        this.head = new ModelPart(this, i, j);
        this.head.addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, 0.0f);
        this.head.setPos(0.0f, 0.0f, 0.0f);
    }

    public void setupAnim(float f, float g, float h) {
        this.head.yRot = g * ((float)Math.PI / 180);
        this.head.xRot = h * ((float)Math.PI / 180);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        this.head.render(poseStack, vertexConsumer, i, j, f, g, h, k);
    }
}

