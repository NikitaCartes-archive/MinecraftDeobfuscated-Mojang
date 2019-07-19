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
public class CodModel<T extends Entity>
extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart topFin;
    private final ModelPart head;
    private final ModelPart nose;
    private final ModelPart sideFin0;
    private final ModelPart sideFin1;
    private final ModelPart tailFin;

    public CodModel() {
        this.texWidth = 32;
        this.texHeight = 32;
        int i = 22;
        this.body = new ModelPart(this, 0, 0);
        this.body.addBox(-1.0f, -2.0f, 0.0f, 2, 4, 7);
        this.body.setPos(0.0f, 22.0f, 0.0f);
        this.head = new ModelPart(this, 11, 0);
        this.head.addBox(-1.0f, -2.0f, -3.0f, 2, 4, 3);
        this.head.setPos(0.0f, 22.0f, 0.0f);
        this.nose = new ModelPart(this, 0, 0);
        this.nose.addBox(-1.0f, -2.0f, -1.0f, 2, 3, 1);
        this.nose.setPos(0.0f, 22.0f, -3.0f);
        this.sideFin0 = new ModelPart(this, 22, 1);
        this.sideFin0.addBox(-2.0f, 0.0f, -1.0f, 2, 0, 2);
        this.sideFin0.setPos(-1.0f, 23.0f, 0.0f);
        this.sideFin0.zRot = -0.7853982f;
        this.sideFin1 = new ModelPart(this, 22, 4);
        this.sideFin1.addBox(0.0f, 0.0f, -1.0f, 2, 0, 2);
        this.sideFin1.setPos(1.0f, 23.0f, 0.0f);
        this.sideFin1.zRot = 0.7853982f;
        this.tailFin = new ModelPart(this, 22, 3);
        this.tailFin.addBox(0.0f, -2.0f, 0.0f, 0, 4, 4);
        this.tailFin.setPos(0.0f, 22.0f, 7.0f);
        this.topFin = new ModelPart(this, 20, -6);
        this.topFin.addBox(0.0f, -1.0f, -1.0f, 0, 1, 6);
        this.topFin.setPos(0.0f, 20.0f, 0.0f);
    }

    @Override
    public void render(T entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim(entity, f, g, h, i, j, k);
        this.body.render(k);
        this.head.render(k);
        this.nose.render(k);
        this.sideFin0.render(k);
        this.sideFin1.render(k);
        this.tailFin.render(k);
        this.topFin.render(k);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        float l = 1.0f;
        if (!((Entity)entity).isInWater()) {
            l = 1.5f;
        }
        this.tailFin.yRot = -l * 0.45f * Mth.sin(0.6f * h);
    }
}

