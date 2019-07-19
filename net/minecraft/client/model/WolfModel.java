/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;

@Environment(value=EnvType.CLIENT)
public class WolfModel<T extends Wolf>
extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart tail;
    private final ModelPart upperBody;

    public WolfModel() {
        float f = 0.0f;
        float g = 13.5f;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-2.0f, -3.0f, -2.0f, 6, 6, 4, 0.0f);
        this.head.setPos(-1.0f, 13.5f, -7.0f);
        this.body = new ModelPart(this, 18, 14);
        this.body.addBox(-3.0f, -2.0f, -3.0f, 6, 9, 6, 0.0f);
        this.body.setPos(0.0f, 14.0f, 2.0f);
        this.upperBody = new ModelPart(this, 21, 0);
        this.upperBody.addBox(-3.0f, -3.0f, -3.0f, 8, 6, 7, 0.0f);
        this.upperBody.setPos(-1.0f, 14.0f, 2.0f);
        this.leg0 = new ModelPart(this, 0, 18);
        this.leg0.addBox(0.0f, 0.0f, -1.0f, 2, 8, 2, 0.0f);
        this.leg0.setPos(-2.5f, 16.0f, 7.0f);
        this.leg1 = new ModelPart(this, 0, 18);
        this.leg1.addBox(0.0f, 0.0f, -1.0f, 2, 8, 2, 0.0f);
        this.leg1.setPos(0.5f, 16.0f, 7.0f);
        this.leg2 = new ModelPart(this, 0, 18);
        this.leg2.addBox(0.0f, 0.0f, -1.0f, 2, 8, 2, 0.0f);
        this.leg2.setPos(-2.5f, 16.0f, -4.0f);
        this.leg3 = new ModelPart(this, 0, 18);
        this.leg3.addBox(0.0f, 0.0f, -1.0f, 2, 8, 2, 0.0f);
        this.leg3.setPos(0.5f, 16.0f, -4.0f);
        this.tail = new ModelPart(this, 9, 18);
        this.tail.addBox(0.0f, 0.0f, -1.0f, 2, 8, 2, 0.0f);
        this.tail.setPos(-1.0f, 12.0f, 8.0f);
        this.head.texOffs(16, 14).addBox(-2.0f, -5.0f, 0.0f, 2, 2, 1, 0.0f);
        this.head.texOffs(16, 14).addBox(2.0f, -5.0f, 0.0f, 2, 2, 1, 0.0f);
        this.head.texOffs(0, 10).addBox(-0.5f, 0.0f, -5.0f, 3, 3, 4, 0.0f);
    }

    @Override
    public void render(T wolf, float f, float g, float h, float i, float j, float k) {
        super.render(wolf, f, g, h, i, j, k);
        this.setupAnim(wolf, f, g, h, i, j, k);
        if (this.young) {
            float l = 2.0f;
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.0f, 5.0f * k, 2.0f * k);
            this.head.renderRollable(k);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scalef(0.5f, 0.5f, 0.5f);
            GlStateManager.translatef(0.0f, 24.0f * k, 0.0f);
            this.body.render(k);
            this.leg0.render(k);
            this.leg1.render(k);
            this.leg2.render(k);
            this.leg3.render(k);
            this.tail.renderRollable(k);
            this.upperBody.render(k);
            GlStateManager.popMatrix();
        } else {
            this.head.renderRollable(k);
            this.body.render(k);
            this.leg0.render(k);
            this.leg1.render(k);
            this.leg2.render(k);
            this.leg3.render(k);
            this.tail.renderRollable(k);
            this.upperBody.render(k);
        }
    }

    @Override
    public void prepareMobModel(T wolf, float f, float g, float h) {
        this.tail.yRot = ((Wolf)wolf).isAngry() ? 0.0f : Mth.cos(f * 0.6662f) * 1.4f * g;
        if (((TamableAnimal)wolf).isSitting()) {
            this.upperBody.setPos(-1.0f, 16.0f, -3.0f);
            this.upperBody.xRot = 1.2566371f;
            this.upperBody.yRot = 0.0f;
            this.body.setPos(0.0f, 18.0f, 0.0f);
            this.body.xRot = 0.7853982f;
            this.tail.setPos(-1.0f, 21.0f, 6.0f);
            this.leg0.setPos(-2.5f, 22.0f, 2.0f);
            this.leg0.xRot = 4.712389f;
            this.leg1.setPos(0.5f, 22.0f, 2.0f);
            this.leg1.xRot = 4.712389f;
            this.leg2.xRot = 5.811947f;
            this.leg2.setPos(-2.49f, 17.0f, -4.0f);
            this.leg3.xRot = 5.811947f;
            this.leg3.setPos(0.51f, 17.0f, -4.0f);
        } else {
            this.body.setPos(0.0f, 14.0f, 2.0f);
            this.body.xRot = 1.5707964f;
            this.upperBody.setPos(-1.0f, 14.0f, -3.0f);
            this.upperBody.xRot = this.body.xRot;
            this.tail.setPos(-1.0f, 12.0f, 8.0f);
            this.leg0.setPos(-2.5f, 16.0f, 7.0f);
            this.leg1.setPos(0.5f, 16.0f, 7.0f);
            this.leg2.setPos(-2.5f, 16.0f, -4.0f);
            this.leg3.setPos(0.5f, 16.0f, -4.0f);
            this.leg0.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
            this.leg1.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
            this.leg2.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
            this.leg3.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
        }
        this.head.zRot = ((Wolf)wolf).getHeadRollAngle(h) + ((Wolf)wolf).getBodyRollAngle(h, 0.0f);
        this.upperBody.zRot = ((Wolf)wolf).getBodyRollAngle(h, -0.08f);
        this.body.zRot = ((Wolf)wolf).getBodyRollAngle(h, -0.16f);
        this.tail.zRot = ((Wolf)wolf).getBodyRollAngle(h, -0.2f);
    }

    @Override
    public void setupAnim(T wolf, float f, float g, float h, float i, float j, float k) {
        super.setupAnim(wolf, f, g, h, i, j, k);
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
        this.tail.xRot = h;
    }

    @Override
    public /* synthetic */ void setupAnim(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim((T)((Wolf)entity), f, g, h, i, j, k);
    }

    @Override
    public /* synthetic */ void render(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.render((T)((Wolf)entity), f, g, h, i, j, k);
    }
}

