/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.IronGolem;

@Environment(value=EnvType.CLIENT)
public class IronGolemModel<T extends IronGolem>
extends ListModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    public final ModelPart arm0;
    private final ModelPart arm1;
    private final ModelPart leg0;
    private final ModelPart leg1;

    public IronGolemModel() {
        this(0.0f);
    }

    public IronGolemModel(float f) {
        this(f, -7.0f);
    }

    public IronGolemModel(float f, float g) {
        int i = 128;
        int j = 128;
        this.head = new ModelPart(this).setTexSize(128, 128);
        this.head.setPos(0.0f, 0.0f + g, -2.0f);
        this.head.texOffs(0, 0).addBox(-4.0f, -12.0f, -5.5f, 8.0f, 10.0f, 8.0f, f);
        this.head.texOffs(24, 0).addBox(-1.0f, -5.0f, -7.5f, 2.0f, 4.0f, 2.0f, f);
        this.body = new ModelPart(this).setTexSize(128, 128);
        this.body.setPos(0.0f, 0.0f + g, 0.0f);
        this.body.texOffs(0, 40).addBox(-9.0f, -2.0f, -6.0f, 18.0f, 12.0f, 11.0f, f);
        this.body.texOffs(0, 70).addBox(-4.5f, 10.0f, -3.0f, 9.0f, 5.0f, 6.0f, f + 0.5f);
        this.arm0 = new ModelPart(this).setTexSize(128, 128);
        this.arm0.setPos(0.0f, -7.0f, 0.0f);
        this.arm0.texOffs(60, 21).addBox(-13.0f, -2.5f, -3.0f, 4.0f, 30.0f, 6.0f, f);
        this.arm1 = new ModelPart(this).setTexSize(128, 128);
        this.arm1.setPos(0.0f, -7.0f, 0.0f);
        this.arm1.texOffs(60, 58).addBox(9.0f, -2.5f, -3.0f, 4.0f, 30.0f, 6.0f, f);
        this.leg0 = new ModelPart(this, 0, 22).setTexSize(128, 128);
        this.leg0.setPos(-4.0f, 18.0f + g, 0.0f);
        this.leg0.texOffs(37, 0).addBox(-3.5f, -3.0f, -3.0f, 6.0f, 16.0f, 5.0f, f);
        this.leg1 = new ModelPart(this, 0, 22).setTexSize(128, 128);
        this.leg1.mirror = true;
        this.leg1.texOffs(60, 0).setPos(5.0f, 18.0f + g, 0.0f);
        this.leg1.addBox(-3.5f, -3.0f, -3.0f, 6.0f, 16.0f, 5.0f, f);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.head, this.body, this.leg0, this.leg1, this.arm0, this.arm1);
    }

    @Override
    public void setupAnim(T ironGolem, float f, float g, float h, float i, float j, float k) {
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
        this.leg0.xRot = -1.5f * this.triangleWave(f, 13.0f) * g;
        this.leg1.xRot = 1.5f * this.triangleWave(f, 13.0f) * g;
        this.leg0.yRot = 0.0f;
        this.leg1.yRot = 0.0f;
    }

    @Override
    public void prepareMobModel(T ironGolem, float f, float g, float h) {
        int i = ((IronGolem)ironGolem).getAttackAnimationTick();
        if (i > 0) {
            this.arm0.xRot = -2.0f + 1.5f * this.triangleWave((float)i - h, 10.0f);
            this.arm1.xRot = -2.0f + 1.5f * this.triangleWave((float)i - h, 10.0f);
        } else {
            int j = ((IronGolem)ironGolem).getOfferFlowerTick();
            if (j > 0) {
                this.arm0.xRot = -0.8f + 0.025f * this.triangleWave(j, 70.0f);
                this.arm1.xRot = 0.0f;
            } else {
                this.arm0.xRot = (-0.2f + 1.5f * this.triangleWave(f, 13.0f)) * g;
                this.arm1.xRot = (-0.2f - 1.5f * this.triangleWave(f, 13.0f)) * g;
            }
        }
    }

    private float triangleWave(float f, float g) {
        return (Math.abs(f % g - g * 0.5f) - g * 0.25f) / (g * 0.25f);
    }

    public ModelPart getFlowerHoldingArm() {
        return this.arm1;
    }
}

