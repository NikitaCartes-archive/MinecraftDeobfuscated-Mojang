/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Random;
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
    private List<ModelPart> cubes = Lists.newArrayList();
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
        this.ear.addBox(-3.0f, -6.0f, -1.0f, 6.0f, 6.0f, 1.0f, f);
        this.cloak = new ModelPart(this, 0, 0);
        this.cloak.setTexSize(64, 32);
        this.cloak.addBox(-5.0f, 0.0f, -1.0f, 10.0f, 16.0f, 1.0f, f);
        if (bl) {
            this.leftArm = new ModelPart(this, 32, 48);
            this.leftArm.addBox(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, f);
            this.leftArm.setPos(5.0f, 2.5f, 0.0f);
            this.rightArm = new ModelPart(this, 40, 16);
            this.rightArm.addBox(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, f);
            this.rightArm.setPos(-5.0f, 2.5f, 0.0f);
            this.leftSleeve = new ModelPart(this, 48, 48);
            this.leftSleeve.addBox(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, f + 0.25f);
            this.leftSleeve.setPos(5.0f, 2.5f, 0.0f);
            this.rightSleeve = new ModelPart(this, 40, 32);
            this.rightSleeve.addBox(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, f + 0.25f);
            this.rightSleeve.setPos(-5.0f, 2.5f, 10.0f);
        } else {
            this.leftArm = new ModelPart(this, 32, 48);
            this.leftArm.addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
            this.leftArm.setPos(5.0f, 2.0f, 0.0f);
            this.leftSleeve = new ModelPart(this, 48, 48);
            this.leftSleeve.addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, f + 0.25f);
            this.leftSleeve.setPos(5.0f, 2.0f, 0.0f);
            this.rightSleeve = new ModelPart(this, 40, 32);
            this.rightSleeve.addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, f + 0.25f);
            this.rightSleeve.setPos(-5.0f, 2.0f, 10.0f);
        }
        this.leftLeg = new ModelPart(this, 16, 48);
        this.leftLeg.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
        this.leftLeg.setPos(1.9f, 12.0f, 0.0f);
        this.leftPants = new ModelPart(this, 0, 48);
        this.leftPants.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, f + 0.25f);
        this.leftPants.setPos(1.9f, 12.0f, 0.0f);
        this.rightPants = new ModelPart(this, 0, 32);
        this.rightPants.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, f + 0.25f);
        this.rightPants.setPos(-1.9f, 12.0f, 0.0f);
        this.jacket = new ModelPart(this, 16, 32);
        this.jacket.addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, f + 0.25f);
        this.jacket.setPos(0.0f, 0.0f, 0.0f);
    }

    @Override
    public void render(T livingEntity, float f, float g, float h, float i, float j, float k) {
        super.render(livingEntity, f, g, h, i, j, k);
        RenderSystem.pushMatrix();
        if (this.young) {
            float l = 2.0f;
            RenderSystem.scalef(0.5f, 0.5f, 0.5f);
            RenderSystem.translatef(0.0f, 24.0f * k, 0.0f);
            this.leftPants.render(k);
            this.rightPants.render(k);
            this.leftSleeve.render(k);
            this.rightSleeve.render(k);
            this.jacket.render(k);
        } else {
            if (((Entity)livingEntity).isCrouching()) {
                RenderSystem.translatef(0.0f, 0.2f, 0.0f);
            }
            this.leftPants.render(k);
            this.rightPants.render(k);
            this.leftSleeve.render(k);
            this.rightSleeve.render(k);
            this.jacket.render(k);
        }
        RenderSystem.popMatrix();
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
        this.cloak.y = ((Entity)livingEntity).isCrouching() ? 2.0f : 0.0f;
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

    public ModelPart getRandomModelPart(Random random) {
        return this.cubes.get(random.nextInt(this.cubes.size()));
    }

    @Override
    public void accept(ModelPart modelPart) {
        if (this.cubes == null) {
            this.cubes = Lists.newArrayList();
        }
        this.cubes.add(modelPart);
    }

    @Override
    public /* synthetic */ void accept(Object object) {
        this.accept((ModelPart)object);
    }
}

