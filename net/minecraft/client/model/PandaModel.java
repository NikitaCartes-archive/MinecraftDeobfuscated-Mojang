/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelUtils;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.animal.Panda;

@Environment(value=EnvType.CLIENT)
public class PandaModel<T extends Panda>
extends QuadrupedModel<T> {
    private float sitAmount;
    private float lieOnBackAmount;
    private float rollAmount;

    public PandaModel(int i, float f) {
        super(i, f, true, 23.0f, 0.3f, 1.2f, 3.0f, 49);
        this.texWidth = 64;
        this.texHeight = 64;
        this.head = new ModelPart(this, 0, 6);
        this.head.addBox(-6.5f, -5.0f, -4.0f, 13.0f, 10.0f, 9.0f);
        this.head.setPos(0.0f, 11.5f, -17.0f);
        this.head.texOffs(45, 16).addBox(-3.5f, 0.0f, -6.0f, 7.0f, 5.0f, 2.0f);
        this.head.texOffs(52, 25).addBox(-8.5f, -8.0f, -1.0f, 5.0f, 4.0f, 1.0f);
        this.head.texOffs(52, 25).addBox(3.5f, -8.0f, -1.0f, 5.0f, 4.0f, 1.0f);
        this.body = new ModelPart(this, 0, 25);
        this.body.addBox(-9.5f, -13.0f, -6.5f, 19.0f, 26.0f, 13.0f);
        this.body.setPos(0.0f, 10.0f, 0.0f);
        int j = 9;
        int k = 6;
        this.leg0 = new ModelPart(this, 40, 0);
        this.leg0.addBox(-3.0f, 0.0f, -3.0f, 6.0f, 9.0f, 6.0f);
        this.leg0.setPos(-5.5f, 15.0f, 9.0f);
        this.leg1 = new ModelPart(this, 40, 0);
        this.leg1.addBox(-3.0f, 0.0f, -3.0f, 6.0f, 9.0f, 6.0f);
        this.leg1.setPos(5.5f, 15.0f, 9.0f);
        this.leg2 = new ModelPart(this, 40, 0);
        this.leg2.addBox(-3.0f, 0.0f, -3.0f, 6.0f, 9.0f, 6.0f);
        this.leg2.setPos(-5.5f, 15.0f, -9.0f);
        this.leg3 = new ModelPart(this, 40, 0);
        this.leg3.addBox(-3.0f, 0.0f, -3.0f, 6.0f, 9.0f, 6.0f);
        this.leg3.setPos(5.5f, 15.0f, -9.0f);
    }

    @Override
    public void prepareMobModel(T panda, float f, float g, float h) {
        super.prepareMobModel(panda, f, g, h);
        this.sitAmount = ((Panda)panda).getSitAmount(h);
        this.lieOnBackAmount = ((Panda)panda).getLieOnBackAmount(h);
        this.rollAmount = ((AgableMob)panda).isBaby() ? 0.0f : ((Panda)panda).getRollAmount(h);
    }

    @Override
    public void setupAnim(T panda, float f, float g, float h, float i, float j, float k) {
        super.setupAnim(panda, f, g, h, i, j, k);
        boolean bl = ((Panda)panda).getUnhappyCounter() > 0;
        boolean bl2 = ((Panda)panda).isSneezing();
        int l = ((Panda)panda).getSneezeCounter();
        boolean bl3 = ((Panda)panda).isEating();
        boolean bl4 = ((Panda)panda).isScared();
        if (bl) {
            this.head.yRot = 0.35f * Mth.sin(0.6f * h);
            this.head.zRot = 0.35f * Mth.sin(0.6f * h);
            this.leg2.xRot = -0.75f * Mth.sin(0.3f * h);
            this.leg3.xRot = 0.75f * Mth.sin(0.3f * h);
        } else {
            this.head.zRot = 0.0f;
        }
        if (bl2) {
            if (l < 15) {
                this.head.xRot = -0.7853982f * (float)l / 14.0f;
            } else if (l < 20) {
                float m = (l - 15) / 5;
                this.head.xRot = -0.7853982f + 0.7853982f * m;
            }
        }
        if (this.sitAmount > 0.0f) {
            this.body.xRot = ModelUtils.rotlerpRad(this.body.xRot, 1.7407963f, this.sitAmount);
            this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, 1.5707964f, this.sitAmount);
            this.leg2.zRot = -0.27079642f;
            this.leg3.zRot = 0.27079642f;
            this.leg0.zRot = 0.5707964f;
            this.leg1.zRot = -0.5707964f;
            if (bl3) {
                this.head.xRot = 1.5707964f + 0.2f * Mth.sin(h * 0.6f);
                this.leg2.xRot = -0.4f - 0.2f * Mth.sin(h * 0.6f);
                this.leg3.xRot = -0.4f - 0.2f * Mth.sin(h * 0.6f);
            }
            if (bl4) {
                this.head.xRot = 2.1707964f;
                this.leg2.xRot = -0.9f;
                this.leg3.xRot = -0.9f;
            }
        } else {
            this.leg0.zRot = 0.0f;
            this.leg1.zRot = 0.0f;
            this.leg2.zRot = 0.0f;
            this.leg3.zRot = 0.0f;
        }
        if (this.lieOnBackAmount > 0.0f) {
            this.leg0.xRot = -0.6f * Mth.sin(h * 0.15f);
            this.leg1.xRot = 0.6f * Mth.sin(h * 0.15f);
            this.leg2.xRot = 0.3f * Mth.sin(h * 0.25f);
            this.leg3.xRot = -0.3f * Mth.sin(h * 0.25f);
            this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, 1.5707964f, this.lieOnBackAmount);
        }
        if (this.rollAmount > 0.0f) {
            this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, 2.0561945f, this.rollAmount);
            this.leg0.xRot = -0.5f * Mth.sin(h * 0.5f);
            this.leg1.xRot = 0.5f * Mth.sin(h * 0.5f);
            this.leg2.xRot = 0.5f * Mth.sin(h * 0.5f);
            this.leg3.xRot = -0.5f * Mth.sin(h * 0.5f);
        }
    }
}

