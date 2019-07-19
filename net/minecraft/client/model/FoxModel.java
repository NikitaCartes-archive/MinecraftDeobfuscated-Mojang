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
import net.minecraft.world.entity.animal.Fox;

@Environment(value=EnvType.CLIENT)
public class FoxModel<T extends Fox>
extends EntityModel<T> {
    public final ModelPart head;
    private final ModelPart earL;
    private final ModelPart earR;
    private final ModelPart nose;
    private final ModelPart body;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart tail;
    private float legMotionPos;

    public FoxModel() {
        this.texWidth = 48;
        this.texHeight = 32;
        this.head = new ModelPart(this, 1, 5);
        this.head.addBox(-3.0f, -2.0f, -5.0f, 8, 6, 6);
        this.head.setPos(-1.0f, 16.5f, -3.0f);
        this.earL = new ModelPart(this, 8, 1);
        this.earL.addBox(-3.0f, -4.0f, -4.0f, 2, 2, 1);
        this.earR = new ModelPart(this, 15, 1);
        this.earR.addBox(3.0f, -4.0f, -4.0f, 2, 2, 1);
        this.nose = new ModelPart(this, 6, 18);
        this.nose.addBox(-1.0f, 2.01f, -8.0f, 4, 2, 3);
        this.head.addChild(this.earL);
        this.head.addChild(this.earR);
        this.head.addChild(this.nose);
        this.body = new ModelPart(this, 24, 15);
        this.body.addBox(-3.0f, 3.999f, -3.5f, 6, 11, 6);
        this.body.setPos(0.0f, 16.0f, -6.0f);
        float f = 0.001f;
        this.leg0 = new ModelPart(this, 13, 24);
        this.leg0.addBox(2.0f, 0.5f, -1.0f, 2, 6, 2, 0.001f);
        this.leg0.setPos(-5.0f, 17.5f, 7.0f);
        this.leg1 = new ModelPart(this, 4, 24);
        this.leg1.addBox(2.0f, 0.5f, -1.0f, 2, 6, 2, 0.001f);
        this.leg1.setPos(-1.0f, 17.5f, 7.0f);
        this.leg2 = new ModelPart(this, 13, 24);
        this.leg2.addBox(2.0f, 0.5f, -1.0f, 2, 6, 2, 0.001f);
        this.leg2.setPos(-5.0f, 17.5f, 0.0f);
        this.leg3 = new ModelPart(this, 4, 24);
        this.leg3.addBox(2.0f, 0.5f, -1.0f, 2, 6, 2, 0.001f);
        this.leg3.setPos(-1.0f, 17.5f, 0.0f);
        this.tail = new ModelPart(this, 30, 0);
        this.tail.addBox(2.0f, 0.0f, -1.0f, 4, 9, 5);
        this.tail.setPos(-4.0f, 15.0f, -1.0f);
        this.body.addChild(this.tail);
    }

    @Override
    public void prepareMobModel(T fox, float f, float g, float h) {
        this.body.xRot = 1.5707964f;
        this.tail.xRot = -0.05235988f;
        this.leg0.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
        this.leg1.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.leg2.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.leg3.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
        this.head.setPos(-1.0f, 16.5f, -3.0f);
        this.head.yRot = 0.0f;
        this.head.zRot = ((Fox)fox).getHeadRollAngle(h);
        this.leg0.visible = true;
        this.leg1.visible = true;
        this.leg2.visible = true;
        this.leg3.visible = true;
        this.body.setPos(0.0f, 16.0f, -6.0f);
        this.body.zRot = 0.0f;
        this.leg0.setPos(-5.0f, 17.5f, 7.0f);
        this.leg1.setPos(-1.0f, 17.5f, 7.0f);
        if (((Fox)fox).isCrouching()) {
            this.body.xRot = 1.6755161f;
            float i = ((Fox)fox).getCrouchAmount(h);
            this.body.setPos(0.0f, 16.0f + ((Fox)fox).getCrouchAmount(h), -6.0f);
            this.head.setPos(-1.0f, 16.5f + i, -3.0f);
            this.head.yRot = 0.0f;
        } else if (((Fox)fox).isSleeping()) {
            this.body.zRot = -1.5707964f;
            this.body.setPos(0.0f, 21.0f, -6.0f);
            this.tail.xRot = -2.6179938f;
            if (this.young) {
                this.tail.xRot = -2.1816616f;
                this.body.setPos(0.0f, 21.0f, -2.0f);
            }
            this.head.setPos(1.0f, 19.49f, -3.0f);
            this.head.xRot = 0.0f;
            this.head.yRot = -2.0943952f;
            this.head.zRot = 0.0f;
            this.leg0.visible = false;
            this.leg1.visible = false;
            this.leg2.visible = false;
            this.leg3.visible = false;
        } else if (((Fox)fox).isSitting()) {
            this.body.xRot = 0.5235988f;
            this.body.setPos(0.0f, 9.0f, -3.0f);
            this.tail.xRot = 0.7853982f;
            this.tail.setPos(-4.0f, 15.0f, -2.0f);
            this.head.setPos(-1.0f, 10.0f, -0.25f);
            this.head.xRot = 0.0f;
            this.head.yRot = 0.0f;
            if (this.young) {
                this.head.setPos(-1.0f, 13.0f, -3.75f);
            }
            this.leg0.xRot = -1.3089969f;
            this.leg0.setPos(-5.0f, 21.5f, 6.75f);
            this.leg1.xRot = -1.3089969f;
            this.leg1.setPos(-1.0f, 21.5f, 6.75f);
            this.leg2.xRot = -0.2617994f;
            this.leg3.xRot = -0.2617994f;
        }
    }

    @Override
    public void render(T fox, float f, float g, float h, float i, float j, float k) {
        super.render(fox, f, g, h, i, j, k);
        this.setupAnim(fox, f, g, h, i, j, k);
        if (this.young) {
            GlStateManager.pushMatrix();
            float l = 0.75f;
            GlStateManager.scalef(0.75f, 0.75f, 0.75f);
            GlStateManager.translatef(0.0f, 8.0f * k, 3.35f * k);
            this.head.render(k);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            float m = 0.5f;
            GlStateManager.scalef(0.5f, 0.5f, 0.5f);
            GlStateManager.translatef(0.0f, 24.0f * k, 0.0f);
            this.body.render(k);
            this.leg0.render(k);
            this.leg1.render(k);
            this.leg2.render(k);
            this.leg3.render(k);
            GlStateManager.popMatrix();
        } else {
            GlStateManager.pushMatrix();
            this.head.render(k);
            this.body.render(k);
            this.leg0.render(k);
            this.leg1.render(k);
            this.leg2.render(k);
            this.leg3.render(k);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void setupAnim(T fox, float f, float g, float h, float i, float j, float k) {
        float l;
        super.setupAnim(fox, f, g, h, i, j, k);
        if (!(((Fox)fox).isSleeping() || ((Fox)fox).isFaceplanted() || ((Fox)fox).isCrouching())) {
            this.head.xRot = j * ((float)Math.PI / 180);
            this.head.yRot = i * ((float)Math.PI / 180);
        }
        if (((Fox)fox).isSleeping()) {
            this.head.xRot = 0.0f;
            this.head.yRot = -2.0943952f;
            this.head.zRot = Mth.cos(h * 0.027f) / 22.0f;
        }
        if (((Fox)fox).isCrouching()) {
            this.body.yRot = l = Mth.cos(h) * 0.01f;
            this.leg0.zRot = l;
            this.leg1.zRot = l;
            this.leg2.zRot = l / 2.0f;
            this.leg3.zRot = l / 2.0f;
        }
        if (((Fox)fox).isFaceplanted()) {
            l = 0.1f;
            this.legMotionPos += 0.67f;
            this.leg0.xRot = Mth.cos(this.legMotionPos * 0.4662f) * 0.1f;
            this.leg1.xRot = Mth.cos(this.legMotionPos * 0.4662f + (float)Math.PI) * 0.1f;
            this.leg2.xRot = Mth.cos(this.legMotionPos * 0.4662f + (float)Math.PI) * 0.1f;
            this.leg3.xRot = Mth.cos(this.legMotionPos * 0.4662f) * 0.1f;
        }
    }

    @Override
    public /* synthetic */ void setupAnim(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim((T)((Fox)entity), f, g, h, i, j, k);
    }

    @Override
    public /* synthetic */ void render(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.render((T)((Fox)entity), f, g, h, i, j, k);
    }
}

