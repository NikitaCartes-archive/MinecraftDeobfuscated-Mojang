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
public class SalmonModel<T extends Entity>
extends EntityModel<T> {
    private final ModelPart bodyFront;
    private final ModelPart bodyBack;
    private final ModelPart head;
    private final ModelPart topFin0;
    private final ModelPart topFin1;
    private final ModelPart backFin;
    private final ModelPart sideFin0;
    private final ModelPart sideFin1;

    public SalmonModel() {
        this.texWidth = 32;
        this.texHeight = 32;
        int i = 20;
        this.bodyFront = new ModelPart(this, 0, 0);
        this.bodyFront.addBox(-1.5f, -2.5f, 0.0f, 3, 5, 8);
        this.bodyFront.setPos(0.0f, 20.0f, 0.0f);
        this.bodyBack = new ModelPart(this, 0, 13);
        this.bodyBack.addBox(-1.5f, -2.5f, 0.0f, 3, 5, 8);
        this.bodyBack.setPos(0.0f, 20.0f, 8.0f);
        this.head = new ModelPart(this, 22, 0);
        this.head.addBox(-1.0f, -2.0f, -3.0f, 2, 4, 3);
        this.head.setPos(0.0f, 20.0f, 0.0f);
        this.backFin = new ModelPart(this, 20, 10);
        this.backFin.addBox(0.0f, -2.5f, 0.0f, 0, 5, 6);
        this.backFin.setPos(0.0f, 0.0f, 8.0f);
        this.bodyBack.addChild(this.backFin);
        this.topFin0 = new ModelPart(this, 2, 1);
        this.topFin0.addBox(0.0f, 0.0f, 0.0f, 0, 2, 3);
        this.topFin0.setPos(0.0f, -4.5f, 5.0f);
        this.bodyFront.addChild(this.topFin0);
        this.topFin1 = new ModelPart(this, 0, 2);
        this.topFin1.addBox(0.0f, 0.0f, 0.0f, 0, 2, 4);
        this.topFin1.setPos(0.0f, -4.5f, -1.0f);
        this.bodyBack.addChild(this.topFin1);
        this.sideFin0 = new ModelPart(this, -4, 0);
        this.sideFin0.addBox(-2.0f, 0.0f, 0.0f, 2, 0, 2);
        this.sideFin0.setPos(-1.5f, 21.5f, 0.0f);
        this.sideFin0.zRot = -0.7853982f;
        this.sideFin1 = new ModelPart(this, 0, 0);
        this.sideFin1.addBox(0.0f, 0.0f, 0.0f, 2, 0, 2);
        this.sideFin1.setPos(1.5f, 21.5f, 0.0f);
        this.sideFin1.zRot = 0.7853982f;
    }

    @Override
    public void render(T entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim(entity, f, g, h, i, j, k);
        this.bodyFront.render(k);
        this.bodyBack.render(k);
        this.head.render(k);
        this.sideFin0.render(k);
        this.sideFin1.render(k);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        float l = 1.0f;
        float m = 1.0f;
        if (!((Entity)entity).isInWater()) {
            l = 1.3f;
            m = 1.7f;
        }
        this.bodyBack.yRot = -l * 0.25f * Mth.sin(m * 0.6f * h);
    }
}

