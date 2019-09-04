/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelUtils;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;

@Environment(value=EnvType.CLIENT)
public class CatModel<T extends Cat>
extends OcelotModel<T> {
    private float lieDownAmount;
    private float lieDownAmountTail;
    private float relaxStateOneAmount;

    public CatModel(float f) {
        super(f);
    }

    @Override
    public void prepareMobModel(T cat, float f, float g, float h) {
        this.lieDownAmount = ((Cat)cat).getLieDownAmount(h);
        this.lieDownAmountTail = ((Cat)cat).getLieDownAmountTail(h);
        this.relaxStateOneAmount = ((Cat)cat).getRelaxStateOneAmount(h);
        if (this.lieDownAmount <= 0.0f) {
            this.head.xRot = 0.0f;
            this.head.zRot = 0.0f;
            this.frontLegL.xRot = 0.0f;
            this.frontLegL.zRot = 0.0f;
            this.frontLegR.xRot = 0.0f;
            this.frontLegR.zRot = 0.0f;
            this.frontLegR.x = -1.2f;
            this.backLegL.xRot = 0.0f;
            this.backLegR.xRot = 0.0f;
            this.backLegR.zRot = 0.0f;
            this.backLegR.x = -1.1f;
            this.backLegR.y = 18.0f;
        }
        super.prepareMobModel(cat, f, g, h);
        if (((TamableAnimal)cat).isSitting()) {
            this.body.xRot = 0.7853982f;
            this.body.y += -4.0f;
            this.body.z += 5.0f;
            this.head.y += -3.3f;
            this.head.z += 1.0f;
            this.tail1.y += 8.0f;
            this.tail1.z += -2.0f;
            this.tail2.y += 2.0f;
            this.tail2.z += -0.8f;
            this.tail1.xRot = 1.7278761f;
            this.tail2.xRot = 2.670354f;
            this.frontLegL.xRot = -0.15707964f;
            this.frontLegL.y = 16.1f;
            this.frontLegL.z = -7.0f;
            this.frontLegR.xRot = -0.15707964f;
            this.frontLegR.y = 16.1f;
            this.frontLegR.z = -7.0f;
            this.backLegL.xRot = -1.5707964f;
            this.backLegL.y = 21.0f;
            this.backLegL.z = 1.0f;
            this.backLegR.xRot = -1.5707964f;
            this.backLegR.y = 21.0f;
            this.backLegR.z = 1.0f;
            this.state = 3;
        }
    }

    @Override
    public void setupAnim(T cat, float f, float g, float h, float i, float j, float k) {
        super.setupAnim(cat, f, g, h, i, j, k);
        if (this.lieDownAmount > 0.0f) {
            this.head.zRot = ModelUtils.rotlerpRad(this.head.zRot, -1.2707963f, this.lieDownAmount);
            this.head.yRot = ModelUtils.rotlerpRad(this.head.yRot, 1.2707963f, this.lieDownAmount);
            this.frontLegL.xRot = -1.2707963f;
            this.frontLegR.xRot = -0.47079635f;
            this.frontLegR.zRot = -0.2f;
            this.frontLegR.x = -0.2f;
            this.backLegL.xRot = -0.4f;
            this.backLegR.xRot = 0.5f;
            this.backLegR.zRot = -0.5f;
            this.backLegR.x = -0.3f;
            this.backLegR.y = 20.0f;
            this.tail1.xRot = ModelUtils.rotlerpRad(this.tail1.xRot, 0.8f, this.lieDownAmountTail);
            this.tail2.xRot = ModelUtils.rotlerpRad(this.tail2.xRot, -0.4f, this.lieDownAmountTail);
        }
        if (this.relaxStateOneAmount > 0.0f) {
            this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, -0.58177644f, this.relaxStateOneAmount);
        }
    }
}

