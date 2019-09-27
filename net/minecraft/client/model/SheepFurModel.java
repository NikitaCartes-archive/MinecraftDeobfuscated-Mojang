/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Sheep;

@Environment(value=EnvType.CLIENT)
public class SheepFurModel<T extends Sheep>
extends QuadrupedModel<T> {
    private float headXRot;

    public SheepFurModel() {
        super(12, 0.0f, false, 8.0f, 4.0f, 2.0f, 2.0f, 24);
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-3.0f, -4.0f, -4.0f, 6.0f, 6.0f, 6.0f, 0.6f);
        this.head.setPos(0.0f, 6.0f, -8.0f);
        this.body = new ModelPart(this, 28, 8);
        this.body.addBox(-4.0f, -10.0f, -7.0f, 8.0f, 16.0f, 6.0f, 1.75f);
        this.body.setPos(0.0f, 5.0f, 2.0f);
        float f = 0.5f;
        this.leg0 = new ModelPart(this, 0, 16);
        this.leg0.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 6.0f, 4.0f, 0.5f);
        this.leg0.setPos(-3.0f, 12.0f, 7.0f);
        this.leg1 = new ModelPart(this, 0, 16);
        this.leg1.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 6.0f, 4.0f, 0.5f);
        this.leg1.setPos(3.0f, 12.0f, 7.0f);
        this.leg2 = new ModelPart(this, 0, 16);
        this.leg2.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 6.0f, 4.0f, 0.5f);
        this.leg2.setPos(-3.0f, 12.0f, -5.0f);
        this.leg3 = new ModelPart(this, 0, 16);
        this.leg3.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 6.0f, 4.0f, 0.5f);
        this.leg3.setPos(3.0f, 12.0f, -5.0f);
    }

    @Override
    public void prepareMobModel(T sheep, float f, float g, float h) {
        super.prepareMobModel(sheep, f, g, h);
        this.head.y = 6.0f + ((Sheep)sheep).getHeadEatPositionScale(h) * 9.0f;
        this.headXRot = ((Sheep)sheep).getHeadEatAngleScale(h);
    }

    @Override
    public void setupAnim(T sheep, float f, float g, float h, float i, float j, float k) {
        super.setupAnim(sheep, f, g, h, i, j, k);
        this.head.xRot = this.headXRot;
    }
}

