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
public class SpiderModel<T extends Entity>
extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body0;
    private final ModelPart body1;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart leg5;
    private final ModelPart leg6;
    private final ModelPart leg7;

    public SpiderModel() {
        float f = 0.0f;
        int i = 15;
        this.head = new ModelPart(this, 32, 4);
        this.head.addBox(-4.0f, -4.0f, -8.0f, 8, 8, 8, 0.0f);
        this.head.setPos(0.0f, 15.0f, -3.0f);
        this.body0 = new ModelPart(this, 0, 0);
        this.body0.addBox(-3.0f, -3.0f, -3.0f, 6, 6, 6, 0.0f);
        this.body0.setPos(0.0f, 15.0f, 0.0f);
        this.body1 = new ModelPart(this, 0, 12);
        this.body1.addBox(-5.0f, -4.0f, -6.0f, 10, 8, 12, 0.0f);
        this.body1.setPos(0.0f, 15.0f, 9.0f);
        this.leg0 = new ModelPart(this, 18, 0);
        this.leg0.addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f);
        this.leg0.setPos(-4.0f, 15.0f, 2.0f);
        this.leg1 = new ModelPart(this, 18, 0);
        this.leg1.addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f);
        this.leg1.setPos(4.0f, 15.0f, 2.0f);
        this.leg2 = new ModelPart(this, 18, 0);
        this.leg2.addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f);
        this.leg2.setPos(-4.0f, 15.0f, 1.0f);
        this.leg3 = new ModelPart(this, 18, 0);
        this.leg3.addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f);
        this.leg3.setPos(4.0f, 15.0f, 1.0f);
        this.leg4 = new ModelPart(this, 18, 0);
        this.leg4.addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f);
        this.leg4.setPos(-4.0f, 15.0f, 0.0f);
        this.leg5 = new ModelPart(this, 18, 0);
        this.leg5.addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f);
        this.leg5.setPos(4.0f, 15.0f, 0.0f);
        this.leg6 = new ModelPart(this, 18, 0);
        this.leg6.addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f);
        this.leg6.setPos(-4.0f, 15.0f, -1.0f);
        this.leg7 = new ModelPart(this, 18, 0);
        this.leg7.addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, 0.0f);
        this.leg7.setPos(4.0f, 15.0f, -1.0f);
    }

    @Override
    public void render(T entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim(entity, f, g, h, i, j, k);
        this.head.render(k);
        this.body0.render(k);
        this.body1.render(k);
        this.leg0.render(k);
        this.leg1.render(k);
        this.leg2.render(k);
        this.leg3.render(k);
        this.leg4.render(k);
        this.leg5.render(k);
        this.leg6.render(k);
        this.leg7.render(k);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
        float l = 0.7853982f;
        this.leg0.zRot = -0.7853982f;
        this.leg1.zRot = 0.7853982f;
        this.leg2.zRot = -0.58119464f;
        this.leg3.zRot = 0.58119464f;
        this.leg4.zRot = -0.58119464f;
        this.leg5.zRot = 0.58119464f;
        this.leg6.zRot = -0.7853982f;
        this.leg7.zRot = 0.7853982f;
        float m = -0.0f;
        float n = 0.3926991f;
        this.leg0.yRot = 0.7853982f;
        this.leg1.yRot = -0.7853982f;
        this.leg2.yRot = 0.3926991f;
        this.leg3.yRot = -0.3926991f;
        this.leg4.yRot = -0.3926991f;
        this.leg5.yRot = 0.3926991f;
        this.leg6.yRot = -0.7853982f;
        this.leg7.yRot = 0.7853982f;
        float o = -(Mth.cos(f * 0.6662f * 2.0f + 0.0f) * 0.4f) * g;
        float p = -(Mth.cos(f * 0.6662f * 2.0f + (float)Math.PI) * 0.4f) * g;
        float q = -(Mth.cos(f * 0.6662f * 2.0f + 1.5707964f) * 0.4f) * g;
        float r = -(Mth.cos(f * 0.6662f * 2.0f + 4.712389f) * 0.4f) * g;
        float s = Math.abs(Mth.sin(f * 0.6662f + 0.0f) * 0.4f) * g;
        float t = Math.abs(Mth.sin(f * 0.6662f + (float)Math.PI) * 0.4f) * g;
        float u = Math.abs(Mth.sin(f * 0.6662f + 1.5707964f) * 0.4f) * g;
        float v = Math.abs(Mth.sin(f * 0.6662f + 4.712389f) * 0.4f) * g;
        this.leg0.yRot += o;
        this.leg1.yRot += -o;
        this.leg2.yRot += p;
        this.leg3.yRot += -p;
        this.leg4.yRot += q;
        this.leg5.yRot += -q;
        this.leg6.yRot += r;
        this.leg7.yRot += -r;
        this.leg0.zRot += s;
        this.leg1.zRot += -s;
        this.leg2.zRot += t;
        this.leg3.zRot += -t;
        this.leg4.zRot += u;
        this.leg5.zRot += -u;
        this.leg6.zRot += v;
        this.leg7.zRot += -v;
    }
}

