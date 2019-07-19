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

@Environment(value=EnvType.CLIENT)
public class QuadrupedModel<T extends Entity>
extends EntityModel<T> {
    protected ModelPart head = new ModelPart(this, 0, 0);
    protected ModelPart body;
    protected ModelPart leg0;
    protected ModelPart leg1;
    protected ModelPart leg2;
    protected ModelPart leg3;
    protected float yHeadOffs = 8.0f;
    protected float zHeadOffs = 4.0f;

    public QuadrupedModel(int i, float f) {
        this.head.addBox(-4.0f, -4.0f, -8.0f, 8, 8, 8, f);
        this.head.setPos(0.0f, 18 - i, -6.0f);
        this.body = new ModelPart(this, 28, 8);
        this.body.addBox(-5.0f, -10.0f, -7.0f, 10, 16, 8, f);
        this.body.setPos(0.0f, 17 - i, 2.0f);
        this.leg0 = new ModelPart(this, 0, 16);
        this.leg0.addBox(-2.0f, 0.0f, -2.0f, 4, i, 4, f);
        this.leg0.setPos(-3.0f, 24 - i, 7.0f);
        this.leg1 = new ModelPart(this, 0, 16);
        this.leg1.addBox(-2.0f, 0.0f, -2.0f, 4, i, 4, f);
        this.leg1.setPos(3.0f, 24 - i, 7.0f);
        this.leg2 = new ModelPart(this, 0, 16);
        this.leg2.addBox(-2.0f, 0.0f, -2.0f, 4, i, 4, f);
        this.leg2.setPos(-3.0f, 24 - i, -5.0f);
        this.leg3 = new ModelPart(this, 0, 16);
        this.leg3.addBox(-2.0f, 0.0f, -2.0f, 4, i, 4, f);
        this.leg3.setPos(3.0f, 24 - i, -5.0f);
    }

    @Override
    public void render(T entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim(entity, f, g, h, i, j, k);
        if (this.young) {
            float l = 2.0f;
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.0f, this.yHeadOffs * k, this.zHeadOffs * k);
            this.head.render(k);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scalef(0.5f, 0.5f, 0.5f);
            GlStateManager.translatef(0.0f, 24.0f * k, 0.0f);
            this.body.render(k);
            this.leg0.render(k);
            this.leg1.render(k);
            this.leg2.render(k);
            this.leg3.render(k);
            GlStateManager.popMatrix();
        } else {
            this.head.render(k);
            this.body.render(k);
            this.leg0.render(k);
            this.leg1.render(k);
            this.leg2.render(k);
            this.leg3.render(k);
        }
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
        this.body.xRot = 1.5707964f;
        this.leg0.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
        this.leg1.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.leg2.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.leg3.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
    }
}

