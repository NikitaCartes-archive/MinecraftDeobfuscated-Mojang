/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.VillagerHeadModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;

@Environment(value=EnvType.CLIENT)
public class ZombieVillagerModel<T extends Zombie>
extends HumanoidModel<T>
implements VillagerHeadModel {
    private ModelPart hatRim;

    public ZombieVillagerModel() {
        this(0.0f, false);
    }

    public ZombieVillagerModel(float f, boolean bl) {
        super(f, 0.0f, 64, bl ? 32 : 64);
        if (bl) {
            this.head = new ModelPart(this, 0, 0);
            this.head.addBox(-4.0f, -10.0f, -4.0f, 8.0f, 8.0f, 8.0f, f);
            this.body = new ModelPart(this, 16, 16);
            this.body.addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, f + 0.1f);
            this.rightLeg = new ModelPart(this, 0, 16);
            this.rightLeg.setPos(-2.0f, 12.0f, 0.0f);
            this.rightLeg.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, f + 0.1f);
            this.leftLeg = new ModelPart(this, 0, 16);
            this.leftLeg.mirror = true;
            this.leftLeg.setPos(2.0f, 12.0f, 0.0f);
            this.leftLeg.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, f + 0.1f);
        } else {
            this.head = new ModelPart(this, 0, 0);
            this.head.texOffs(0, 0).addBox(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f, f);
            this.head.texOffs(24, 0).addBox(-1.0f, -3.0f, -6.0f, 2.0f, 4.0f, 2.0f, f);
            this.hat = new ModelPart(this, 32, 0);
            this.hat.addBox(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f, f + 0.5f);
            this.hatRim = new ModelPart(this);
            this.hatRim.texOffs(30, 47).addBox(-8.0f, -8.0f, -6.0f, 16.0f, 16.0f, 1.0f, f);
            this.hatRim.xRot = -1.5707964f;
            this.hat.addChild(this.hatRim);
            this.body = new ModelPart(this, 16, 20);
            this.body.addBox(-4.0f, 0.0f, -3.0f, 8.0f, 12.0f, 6.0f, f);
            this.body.texOffs(0, 38).addBox(-4.0f, 0.0f, -3.0f, 8.0f, 18.0f, 6.0f, f + 0.05f);
            this.rightArm = new ModelPart(this, 44, 22);
            this.rightArm.addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
            this.rightArm.setPos(-5.0f, 2.0f, 0.0f);
            this.leftArm = new ModelPart(this, 44, 22);
            this.leftArm.mirror = true;
            this.leftArm.addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
            this.leftArm.setPos(5.0f, 2.0f, 0.0f);
            this.rightLeg = new ModelPart(this, 0, 22);
            this.rightLeg.setPos(-2.0f, 12.0f, 0.0f);
            this.rightLeg.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
            this.leftLeg = new ModelPart(this, 0, 22);
            this.leftLeg.mirror = true;
            this.leftLeg.setPos(2.0f, 12.0f, 0.0f);
            this.leftLeg.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
        }
    }

    @Override
    public void setupAnim(T zombie, float f, float g, float h, float i, float j, float k) {
        float n;
        super.setupAnim(zombie, f, g, h, i, j, k);
        float l = Mth.sin(this.attackTime * (float)Math.PI);
        float m = Mth.sin((1.0f - (1.0f - this.attackTime) * (1.0f - this.attackTime)) * (float)Math.PI);
        this.rightArm.zRot = 0.0f;
        this.leftArm.zRot = 0.0f;
        this.rightArm.yRot = -(0.1f - l * 0.6f);
        this.leftArm.yRot = 0.1f - l * 0.6f;
        this.rightArm.xRot = n = (float)(-Math.PI) / (((Mob)zombie).isAggressive() ? 1.5f : 2.25f);
        this.leftArm.xRot = n;
        this.rightArm.xRot += l * 1.2f - m * 0.4f;
        this.leftArm.xRot += l * 1.2f - m * 0.4f;
        this.rightArm.zRot += Mth.cos(h * 0.09f) * 0.05f + 0.05f;
        this.leftArm.zRot -= Mth.cos(h * 0.09f) * 0.05f + 0.05f;
        this.rightArm.xRot += Mth.sin(h * 0.067f) * 0.05f;
        this.leftArm.xRot -= Mth.sin(h * 0.067f) * 0.05f;
    }

    @Override
    public void hatVisible(boolean bl) {
        this.head.visible = bl;
        this.hat.visible = bl;
        this.hatRim.visible = bl;
    }
}

