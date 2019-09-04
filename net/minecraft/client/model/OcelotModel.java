/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class OcelotModel<T extends Entity>
extends EntityModel<T> {
    protected final ModelPart backLegL;
    protected final ModelPart backLegR;
    protected final ModelPart frontLegL;
    protected final ModelPart frontLegR;
    protected final ModelPart tail1;
    protected final ModelPart tail2;
    protected final ModelPart head = new ModelPart(this, "head");
    protected final ModelPart body;
    protected int state = 1;

    public OcelotModel(float f) {
        this.head.addBox("main", -2.5f, -2.0f, -3.0f, 5, 4, 5, f, 0, 0);
        this.head.addBox("nose", -1.5f, 0.0f, -4.0f, 3, 2, 2, f, 0, 24);
        this.head.addBox("ear1", -2.0f, -3.0f, 0.0f, 1, 1, 2, f, 0, 10);
        this.head.addBox("ear2", 1.0f, -3.0f, 0.0f, 1, 1, 2, f, 6, 10);
        this.head.setPos(0.0f, 15.0f, -9.0f);
        this.body = new ModelPart(this, 20, 0);
        this.body.addBox(-2.0f, 3.0f, -8.0f, 4, 16, 6, f);
        this.body.setPos(0.0f, 12.0f, -10.0f);
        this.tail1 = new ModelPart(this, 0, 15);
        this.tail1.addBox(-0.5f, 0.0f, 0.0f, 1, 8, 1, f);
        this.tail1.xRot = 0.9f;
        this.tail1.setPos(0.0f, 15.0f, 8.0f);
        this.tail2 = new ModelPart(this, 4, 15);
        this.tail2.addBox(-0.5f, 0.0f, 0.0f, 1, 8, 1, f);
        this.tail2.setPos(0.0f, 20.0f, 14.0f);
        this.backLegL = new ModelPart(this, 8, 13);
        this.backLegL.addBox(-1.0f, 0.0f, 1.0f, 2, 6, 2, f);
        this.backLegL.setPos(1.1f, 18.0f, 5.0f);
        this.backLegR = new ModelPart(this, 8, 13);
        this.backLegR.addBox(-1.0f, 0.0f, 1.0f, 2, 6, 2, f);
        this.backLegR.setPos(-1.1f, 18.0f, 5.0f);
        this.frontLegL = new ModelPart(this, 40, 0);
        this.frontLegL.addBox(-1.0f, 0.0f, 0.0f, 2, 10, 2, f);
        this.frontLegL.setPos(1.2f, 14.1f, -5.0f);
        this.frontLegR = new ModelPart(this, 40, 0);
        this.frontLegR.addBox(-1.0f, 0.0f, 0.0f, 2, 10, 2, f);
        this.frontLegR.setPos(-1.2f, 14.1f, -5.0f);
    }

    @Override
    public void render(T entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim(entity, f, g, h, i, j, k);
        if (this.young) {
            float l = 2.0f;
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.75f, 0.75f, 0.75f);
            RenderSystem.translatef(0.0f, 10.0f * k, 4.0f * k);
            this.head.render(k);
            RenderSystem.popMatrix();
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.5f, 0.5f, 0.5f);
            RenderSystem.translatef(0.0f, 24.0f * k, 0.0f);
            this.body.render(k);
            this.backLegL.render(k);
            this.backLegR.render(k);
            this.frontLegL.render(k);
            this.frontLegR.render(k);
            this.tail1.render(k);
            this.tail2.render(k);
            RenderSystem.popMatrix();
        } else {
            this.head.render(k);
            this.body.render(k);
            this.tail1.render(k);
            this.tail2.render(k);
            this.backLegL.render(k);
            this.backLegR.render(k);
            this.frontLegL.render(k);
            this.frontLegR.render(k);
        }
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
        if (this.state != 3) {
            this.body.xRot = 1.5707964f;
            if (this.state == 2) {
                this.backLegL.xRot = Mth.cos(f * 0.6662f) * g;
                this.backLegR.xRot = Mth.cos(f * 0.6662f + 0.3f) * g;
                this.frontLegL.xRot = Mth.cos(f * 0.6662f + (float)Math.PI + 0.3f) * g;
                this.frontLegR.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * g;
                this.tail2.xRot = 1.7278761f + 0.31415927f * Mth.cos(f) * g;
            } else {
                this.backLegL.xRot = Mth.cos(f * 0.6662f) * g;
                this.backLegR.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * g;
                this.frontLegL.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * g;
                this.frontLegR.xRot = Mth.cos(f * 0.6662f) * g;
                this.tail2.xRot = this.state == 1 ? 1.7278761f + 0.7853982f * Mth.cos(f) * g : 1.7278761f + 0.47123894f * Mth.cos(f) * g;
            }
        }
    }

    @Override
    public void prepareMobModel(T entity, float f, float g, float h) {
        this.body.y = 12.0f;
        this.body.z = -10.0f;
        this.head.y = 15.0f;
        this.head.z = -9.0f;
        this.tail1.y = 15.0f;
        this.tail1.z = 8.0f;
        this.tail2.y = 20.0f;
        this.tail2.z = 14.0f;
        this.frontLegL.y = 14.1f;
        this.frontLegL.z = -5.0f;
        this.frontLegR.y = 14.1f;
        this.frontLegR.z = -5.0f;
        this.backLegL.y = 18.0f;
        this.backLegL.z = 5.0f;
        this.backLegR.y = 18.0f;
        this.backLegR.z = 5.0f;
        this.tail1.xRot = 0.9f;
        if (((Entity)entity).isCrouching()) {
            this.body.y += 1.0f;
            this.head.y += 2.0f;
            this.tail1.y += 1.0f;
            this.tail2.y += -4.0f;
            this.tail2.z += 2.0f;
            this.tail1.xRot = 1.5707964f;
            this.tail2.xRot = 1.5707964f;
            this.state = 0;
        } else if (((Entity)entity).isSprinting()) {
            this.tail2.y = this.tail1.y;
            this.tail2.z += 2.0f;
            this.tail1.xRot = 1.5707964f;
            this.tail2.xRot = 1.5707964f;
            this.state = 2;
        } else {
            this.state = 1;
        }
    }
}

