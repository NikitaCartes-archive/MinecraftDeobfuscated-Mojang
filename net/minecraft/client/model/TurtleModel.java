/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Turtle;

@Environment(value=EnvType.CLIENT)
public class TurtleModel<T extends Turtle>
extends QuadrupedModel<T> {
    private final ModelPart eggBelly;
    private float yOffset;

    public TurtleModel(float f) {
        super(12, f, true, 120.0f, 0.0f, 9.0f, 6.0f, 120);
        this.texWidth = 128;
        this.texHeight = 64;
        this.head = new ModelPart(this, 3, 0);
        this.head.addBox(-3.0f, -1.0f, -3.0f, 6.0f, 5.0f, 6.0f, 0.0f);
        this.head.setPos(0.0f, 19.0f, -10.0f);
        this.body = new ModelPart(this);
        this.body.texOffs(7, 37).addBox(-9.5f, 3.0f, -10.0f, 19.0f, 20.0f, 6.0f, 0.0f);
        this.body.texOffs(31, 1).addBox(-5.5f, 3.0f, -13.0f, 11.0f, 18.0f, 3.0f, 0.0f);
        this.body.setPos(0.0f, 11.0f, -10.0f);
        this.eggBelly = new ModelPart(this);
        this.eggBelly.texOffs(70, 33).addBox(-4.5f, 3.0f, -14.0f, 9.0f, 18.0f, 1.0f, 0.0f);
        this.eggBelly.setPos(0.0f, 11.0f, -10.0f);
        boolean i = true;
        this.leg0 = new ModelPart(this, 1, 23);
        this.leg0.addBox(-2.0f, 0.0f, 0.0f, 4.0f, 1.0f, 10.0f, 0.0f);
        this.leg0.setPos(-3.5f, 22.0f, 11.0f);
        this.leg1 = new ModelPart(this, 1, 12);
        this.leg1.addBox(-2.0f, 0.0f, 0.0f, 4.0f, 1.0f, 10.0f, 0.0f);
        this.leg1.setPos(3.5f, 22.0f, 11.0f);
        this.leg2 = new ModelPart(this, 27, 30);
        this.leg2.addBox(-13.0f, 0.0f, -2.0f, 13.0f, 1.0f, 5.0f, 0.0f);
        this.leg2.setPos(-5.0f, 21.0f, -4.0f);
        this.leg3 = new ModelPart(this, 27, 24);
        this.leg3.addBox(0.0f, 0.0f, -2.0f, 13.0f, 1.0f, 5.0f, 0.0f);
        this.leg3.setPos(5.0f, 21.0f, -4.0f);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.eggBelly));
    }

    @Override
    public void setupAnim(T turtle, float f, float g, float h, float i, float j, float k) {
        super.setupAnim(turtle, f, g, h, i, j, k);
        this.leg0.xRot = Mth.cos(f * 0.6662f * 0.6f) * 0.5f * g;
        this.leg1.xRot = Mth.cos(f * 0.6662f * 0.6f + (float)Math.PI) * 0.5f * g;
        this.leg2.zRot = Mth.cos(f * 0.6662f * 0.6f + (float)Math.PI) * 0.5f * g;
        this.leg3.zRot = Mth.cos(f * 0.6662f * 0.6f) * 0.5f * g;
        this.leg2.xRot = 0.0f;
        this.leg3.xRot = 0.0f;
        this.leg2.yRot = 0.0f;
        this.leg3.yRot = 0.0f;
        this.leg0.yRot = 0.0f;
        this.leg1.yRot = 0.0f;
        this.eggBelly.xRot = 1.5707964f;
        if (!((Entity)turtle).isInWater() && ((Turtle)turtle).onGround) {
            float l = ((Turtle)turtle).isLayingEgg() ? 4.0f : 1.0f;
            float m = ((Turtle)turtle).isLayingEgg() ? 2.0f : 1.0f;
            float n = 5.0f;
            this.leg2.yRot = Mth.cos(l * f * 5.0f + (float)Math.PI) * 8.0f * g * m;
            this.leg2.zRot = 0.0f;
            this.leg3.yRot = Mth.cos(l * f * 5.0f) * 8.0f * g * m;
            this.leg3.zRot = 0.0f;
            this.leg0.yRot = Mth.cos(f * 5.0f + (float)Math.PI) * 3.0f * g;
            this.leg0.xRot = 0.0f;
            this.leg1.yRot = Mth.cos(f * 5.0f) * 3.0f * g;
            this.leg1.xRot = 0.0f;
        }
        this.eggBelly.visible = !this.young && ((Turtle)turtle).hasEgg();
        this.yOffset = this.eggBelly.visible ? -0.08f : 0.0f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, float f, float g, float h) {
        poseStack.pushPose();
        poseStack.translate(0.0, this.yOffset, 0.0);
        super.renderToBuffer(poseStack, vertexConsumer, i, f, g, h);
        poseStack.popPose();
    }
}

