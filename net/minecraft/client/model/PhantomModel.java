/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class PhantomModel<T extends Entity>
extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart leftWingBase;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingBase;
    private final ModelPart rightWingTip;
    private final ModelPart head;
    private final ModelPart tailBase;
    private final ModelPart tailTip;

    public PhantomModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        this.body = new ModelPart(this, 0, 8);
        this.body.addBox(-3.0f, -2.0f, -8.0f, 5, 3, 9);
        this.tailBase = new ModelPart(this, 3, 20);
        this.tailBase.addBox(-2.0f, 0.0f, 0.0f, 3, 2, 6);
        this.tailBase.setPos(0.0f, -2.0f, 1.0f);
        this.body.addChild(this.tailBase);
        this.tailTip = new ModelPart(this, 4, 29);
        this.tailTip.addBox(-1.0f, 0.0f, 0.0f, 1, 1, 6);
        this.tailTip.setPos(0.0f, 0.5f, 6.0f);
        this.tailBase.addChild(this.tailTip);
        this.leftWingBase = new ModelPart(this, 23, 12);
        this.leftWingBase.addBox(0.0f, 0.0f, 0.0f, 6, 2, 9);
        this.leftWingBase.setPos(2.0f, -2.0f, -8.0f);
        this.leftWingTip = new ModelPart(this, 16, 24);
        this.leftWingTip.addBox(0.0f, 0.0f, 0.0f, 13, 1, 9);
        this.leftWingTip.setPos(6.0f, 0.0f, 0.0f);
        this.leftWingBase.addChild(this.leftWingTip);
        this.rightWingBase = new ModelPart(this, 23, 12);
        this.rightWingBase.mirror = true;
        this.rightWingBase.addBox(-6.0f, 0.0f, 0.0f, 6, 2, 9);
        this.rightWingBase.setPos(-3.0f, -2.0f, -8.0f);
        this.rightWingTip = new ModelPart(this, 16, 24);
        this.rightWingTip.mirror = true;
        this.rightWingTip.addBox(-13.0f, 0.0f, 0.0f, 13, 1, 9);
        this.rightWingTip.setPos(-6.0f, 0.0f, 0.0f);
        this.rightWingBase.addChild(this.rightWingTip);
        this.leftWingBase.zRot = 0.1f;
        this.leftWingTip.zRot = 0.1f;
        this.rightWingBase.zRot = -0.1f;
        this.rightWingTip.zRot = -0.1f;
        this.body.xRot = -0.1f;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-4.0f, -2.0f, -5.0f, 7, 3, 5);
        this.head.setPos(0.0f, 1.0f, -7.0f);
        this.head.xRot = 0.2f;
        this.body.addChild(this.head);
        this.body.addChild(this.leftWingBase);
        this.body.addChild(this.rightWingBase);
    }

    @Override
    public void render(T entity, float f, float g, float h, float i, float j, float k) {
        this.body.render(k);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        float l = ((float)(((Entity)entity).getId() * 3) + h) * 0.13f;
        float m = 16.0f;
        this.leftWingBase.zRot = Mth.cos(l) * 16.0f * ((float)Math.PI / 180);
        this.leftWingTip.zRot = Mth.cos(l) * 16.0f * ((float)Math.PI / 180);
        this.rightWingBase.zRot = -this.leftWingBase.zRot;
        this.rightWingTip.zRot = -this.leftWingTip.zRot;
        this.tailBase.xRot = -(5.0f + Mth.cos(l * 2.0f) * 5.0f) * ((float)Math.PI / 180);
        this.tailTip.xRot = -(5.0f + Mth.cos(l * 2.0f) * 5.0f) * ((float)Math.PI / 180);
    }
}

