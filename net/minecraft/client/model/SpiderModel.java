/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class SpiderModel<T extends Entity>
extends ListModel<T> {
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
        this.head.addBox(-4.0f, -4.0f, -8.0f, 8.0f, 8.0f, 8.0f, 0.0f);
        this.head.setPos(0.0f, 15.0f, -3.0f);
        this.body0 = new ModelPart(this, 0, 0);
        this.body0.addBox(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f, 0.0f);
        this.body0.setPos(0.0f, 15.0f, 0.0f);
        this.body1 = new ModelPart(this, 0, 12);
        this.body1.addBox(-5.0f, -4.0f, -6.0f, 10.0f, 8.0f, 12.0f, 0.0f);
        this.body1.setPos(0.0f, 15.0f, 9.0f);
        this.leg0 = new ModelPart(this, 18, 0);
        this.leg0.addBox(-15.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f, 0.0f);
        this.leg0.setPos(-4.0f, 15.0f, 2.0f);
        this.leg1 = new ModelPart(this, 18, 0);
        this.leg1.addBox(-1.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f, 0.0f);
        this.leg1.setPos(4.0f, 15.0f, 2.0f);
        this.leg2 = new ModelPart(this, 18, 0);
        this.leg2.addBox(-15.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f, 0.0f);
        this.leg2.setPos(-4.0f, 15.0f, 1.0f);
        this.leg3 = new ModelPart(this, 18, 0);
        this.leg3.addBox(-1.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f, 0.0f);
        this.leg3.setPos(4.0f, 15.0f, 1.0f);
        this.leg4 = new ModelPart(this, 18, 0);
        this.leg4.addBox(-15.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f, 0.0f);
        this.leg4.setPos(-4.0f, 15.0f, 0.0f);
        this.leg5 = new ModelPart(this, 18, 0);
        this.leg5.addBox(-1.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f, 0.0f);
        this.leg5.setPos(4.0f, 15.0f, 0.0f);
        this.leg6 = new ModelPart(this, 18, 0);
        this.leg6.addBox(-15.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f, 0.0f);
        this.leg6.setPos(-4.0f, 15.0f, -1.0f);
        this.leg7 = new ModelPart(this, 18, 0);
        this.leg7.addBox(-1.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f, 0.0f);
        this.leg7.setPos(4.0f, 15.0f, -1.0f);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.head, this.body0, this.body1, this.leg0, this.leg1, this.leg2, this.leg3, this.leg4, this.leg5, this.leg6, this.leg7);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
        float k = 0.7853982f;
        this.leg0.zRot = -0.7853982f;
        this.leg1.zRot = 0.7853982f;
        this.leg2.zRot = -0.58119464f;
        this.leg3.zRot = 0.58119464f;
        this.leg4.zRot = -0.58119464f;
        this.leg5.zRot = 0.58119464f;
        this.leg6.zRot = -0.7853982f;
        this.leg7.zRot = 0.7853982f;
        float l = -0.0f;
        float m = 0.3926991f;
        this.leg0.yRot = 0.7853982f;
        this.leg1.yRot = -0.7853982f;
        this.leg2.yRot = 0.3926991f;
        this.leg3.yRot = -0.3926991f;
        this.leg4.yRot = -0.3926991f;
        this.leg5.yRot = 0.3926991f;
        this.leg6.yRot = -0.7853982f;
        this.leg7.yRot = 0.7853982f;
        float n = -(Mth.cos(f * 0.6662f * 2.0f + 0.0f) * 0.4f) * g;
        float o = -(Mth.cos(f * 0.6662f * 2.0f + (float)Math.PI) * 0.4f) * g;
        float p = -(Mth.cos(f * 0.6662f * 2.0f + 1.5707964f) * 0.4f) * g;
        float q = -(Mth.cos(f * 0.6662f * 2.0f + 4.712389f) * 0.4f) * g;
        float r = Math.abs(Mth.sin(f * 0.6662f + 0.0f) * 0.4f) * g;
        float s = Math.abs(Mth.sin(f * 0.6662f + (float)Math.PI) * 0.4f) * g;
        float t = Math.abs(Mth.sin(f * 0.6662f + 1.5707964f) * 0.4f) * g;
        float u = Math.abs(Mth.sin(f * 0.6662f + 4.712389f) * 0.4f) * g;
        this.leg0.yRot += n;
        this.leg1.yRot += -n;
        this.leg2.yRot += o;
        this.leg3.yRot += -o;
        this.leg4.yRot += p;
        this.leg5.yRot += -p;
        this.leg6.yRot += q;
        this.leg7.yRot += -q;
        this.leg0.zRot += r;
        this.leg1.zRot += -r;
        this.leg2.zRot += s;
        this.leg3.zRot += -s;
        this.leg4.zRot += t;
        this.leg5.zRot += -t;
        this.leg6.zRot += u;
        this.leg7.zRot += -u;
    }
}

