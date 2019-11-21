/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelPart;

@Environment(value=EnvType.CLIENT)
public class HumanoidHeadModel
extends SkullModel {
    private final ModelPart hat = new ModelPart(this, 32, 0);

    public HumanoidHeadModel() {
        super(0, 0, 64, 64);
        this.hat.addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, 0.25f);
        this.hat.setPos(0.0f, 0.0f, 0.0f);
    }

    @Override
    public void setupAnim(float f, float g, float h) {
        super.setupAnim(f, g, h);
        this.hat.yRot = this.head.yRot;
        this.hat.xRot = this.head.xRot;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        super.renderToBuffer(poseStack, vertexConsumer, i, j, f, g, h, k);
        this.hat.render(poseStack, vertexConsumer, i, j, f, g, h, k);
    }
}

