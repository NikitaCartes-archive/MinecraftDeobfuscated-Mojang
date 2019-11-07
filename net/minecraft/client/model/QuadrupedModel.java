/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class QuadrupedModel<T extends Entity>
extends AgeableListModel<T> {
    protected ModelPart head = new ModelPart(this, 0, 0);
    protected ModelPart body;
    protected ModelPart leg0;
    protected ModelPart leg1;
    protected ModelPart leg2;
    protected ModelPart leg3;

    public QuadrupedModel(int i, float f, boolean bl, float g, float h, float j, float k, int l) {
        super(bl, g, h, j, k, l);
        this.head.addBox(-4.0f, -4.0f, -8.0f, 8.0f, 8.0f, 8.0f, f);
        this.head.setPos(0.0f, 18 - i, -6.0f);
        this.body = new ModelPart(this, 28, 8);
        this.body.addBox(-5.0f, -10.0f, -7.0f, 10.0f, 16.0f, 8.0f, f);
        this.body.setPos(0.0f, 17 - i, 2.0f);
        this.leg0 = new ModelPart(this, 0, 16);
        this.leg0.addBox(-2.0f, 0.0f, -2.0f, 4.0f, (float)i, 4.0f, f);
        this.leg0.setPos(-3.0f, 24 - i, 7.0f);
        this.leg1 = new ModelPart(this, 0, 16);
        this.leg1.addBox(-2.0f, 0.0f, -2.0f, 4.0f, (float)i, 4.0f, f);
        this.leg1.setPos(3.0f, 24 - i, 7.0f);
        this.leg2 = new ModelPart(this, 0, 16);
        this.leg2.addBox(-2.0f, 0.0f, -2.0f, 4.0f, (float)i, 4.0f, f);
        this.leg2.setPos(-3.0f, 24 - i, -5.0f);
        this.leg3 = new ModelPart(this, 0, 16);
        this.leg3.addBox(-2.0f, 0.0f, -2.0f, 4.0f, (float)i, 4.0f, f);
        this.leg3.setPos(3.0f, 24 - i, -5.0f);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.leg0, this.leg1, this.leg2, this.leg3);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
        this.body.xRot = 1.5707964f;
        this.leg0.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
        this.leg1.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.leg2.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.leg3.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
    }
}

