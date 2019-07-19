/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

@Environment(value=EnvType.CLIENT)
public class PlayerModel<T extends LivingEntity>
extends HumanoidModel<T> {
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart leftPants;
    public final ModelPart rightPants;
    public final ModelPart jacket;
    private final ModelPart cloak;
    private final ModelPart ear;
    private final boolean slim;

    public PlayerModel(float f, boolean bl) {
        super(f, 0.0f, 64, 64);
        this.slim = bl;
        this.ear = new ModelPart(this, 24, 0);
        this.ear.addBox(-3.0f, -6.0f, -1.0f, 6, 6, 1, f);
        this.cloak = new ModelPart(this, 0, 0);
        this.cloak.setTexSize(64, 32);
        this.cloak.addBox(-5.0f, 0.0f, -1.0f, 10, 16, 1, f);
        if (bl) {
            this.leftArm = new ModelPart(this, 32, 48);
            this.leftArm.addBox(-1.0f, -2.0f, -2.0f, 3, 12, 4, f);
            this.leftArm.setPos(5.0f, 2.5f, 0.0f);
            this.rightArm = new ModelPart(this, 40, 16);
            this.rightArm.addBox(-2.0f, -2.0f, -2.0f, 3, 12, 4, f);
            this.rightArm.setPos(-5.0f, 2.5f, 0.0f);
            this.leftSleeve = new ModelPart(this, 48, 48);
            this.leftSleeve.addBox(-1.0f, -2.0f, -2.0f, 3, 12, 4, f + 0.25f);
            this.leftSleeve.setPos(5.0f, 2.5f, 0.0f);
            this.rightSleeve = new ModelPart(this, 40, 32);
            this.rightSleeve.addBox(-2.0f, -2.0f, -2.0f, 3, 12, 4, f + 0.25f);
            this.rightSleeve.setPos(-5.0f, 2.5f, 10.0f);
        } else {
            this.leftArm = new ModelPart(this, 32, 48);
            this.leftArm.addBox(-1.0f, -2.0f, -2.0f, 4, 12, 4, f);
            this.leftArm.setPos(5.0f, 2.0f, 0.0f);
            this.leftSleeve = new ModelPart(this, 48, 48);
            this.leftSleeve.addBox(-1.0f, -2.0f, -2.0f, 4, 12, 4, f + 0.25f);
            this.leftSleeve.setPos(5.0f, 2.0f, 0.0f);
            this.rightSleeve = new ModelPart(this, 40, 32);
            this.rightSleeve.addBox(-3.0f, -2.0f, -2.0f, 4, 12, 4, f + 0.25f);
            this.rightSleeve.setPos(-5.0f, 2.0f, 10.0f);
        }
        this.leftLeg = new ModelPart(this, 16, 48);
        this.leftLeg.addBox(-2.0f, 0.0f, -2.0f, 4, 12, 4, f);
        this.leftLeg.setPos(1.9f, 12.0f, 0.0f);
        this.leftPants = new ModelPart(this, 0, 48);
        this.leftPants.addBox(-2.0f, 0.0f, -2.0f, 4, 12, 4, f + 0.25f);
        this.leftPants.setPos(1.9f, 12.0f, 0.0f);
        this.rightPants = new ModelPart(this, 0, 32);
        this.rightPants.addBox(-2.0f, 0.0f, -2.0f, 4, 12, 4, f + 0.25f);
        this.rightPants.setPos(-1.9f, 12.0f, 0.0f);
        this.jacket = new ModelPart(this, 16, 32);
        this.jacket.addBox(-4.0f, 0.0f, -2.0f, 8, 12, 4, f + 0.25f);
        this.jacket.setPos(0.0f, 0.0f, 0.0f);
    }

    @Override
    public void render(T livingEntity, float f, float g, float h, float i, float j, float k) {
        super.render(livingEntity, f, g, h, i, j, k);
        GlStateManager.pushMatrix();
        if (this.young) {
            float l = 2.0f;
            GlStateManager.scalef(0.5f, 0.5f, 0.5f);
            GlStateManager.translatef(0.0f, 24.0f * k, 0.0f);
            this.leftPants.render(k);
            this.rightPants.render(k);
            this.leftSleeve.render(k);
            this.rightSleeve.render(k);
            this.jacket.render(k);
        } else {
            if (((Entity)livingEntity).isVisuallySneaking()) {
                GlStateManager.translatef(0.0f, 0.2f, 0.0f);
            }
            this.leftPants.render(k);
            this.rightPants.render(k);
            this.leftSleeve.render(k);
            this.rightSleeve.render(k);
            this.jacket.render(k);
        }
        GlStateManager.popMatrix();
    }

    public void renderEars(float f) {
        this.ear.copyFrom(this.head);
        this.ear.x = 0.0f;
        this.ear.y = 0.0f;
        this.ear.render(f);
    }

    public void renderCloak(float f) {
        this.cloak.render(f);
    }

    @Override
    public void setupAnim(T livingEntity, float f, float g, float h, float i, float j, float k) {
        super.setupAnim(livingEntity, f, g, h, i, j, k);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);
        this.cloak.y = ((Entity)livingEntity).isVisuallySneaking() ? 2.0f : 0.0f;
    }

    @Override
    public void setAllVisible(boolean bl) {
        super.setAllVisible(bl);
        this.leftSleeve.visible = bl;
        this.rightSleeve.visible = bl;
        this.leftPants.visible = bl;
        this.rightPants.visible = bl;
        this.jacket.visible = bl;
        this.cloak.visible = bl;
        this.ear.visible = bl;
    }

    @Override
    public void translateToHand(float f, HumanoidArm humanoidArm) {
        ModelPart modelPart = this.getArm(humanoidArm);
        if (this.slim) {
            float g = 0.5f * (float)(humanoidArm == HumanoidArm.RIGHT ? 1 : -1);
            modelPart.x += g;
            modelPart.translateTo(f);
            modelPart.x -= g;
        } else {
            modelPart.translateTo(f);
        }
    }
}

