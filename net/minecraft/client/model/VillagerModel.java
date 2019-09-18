/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.HeadedModel;
import net.minecraft.client.renderer.entity.VillagerHeadModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;

@Environment(value=EnvType.CLIENT)
public class VillagerModel<T extends Entity>
extends EntityModel<T>
implements HeadedModel,
VillagerHeadModel {
    protected ModelPart head;
    protected ModelPart hat;
    protected final ModelPart hatRim;
    protected final ModelPart body;
    protected final ModelPart jacket;
    protected final ModelPart arms;
    protected final ModelPart leg0;
    protected final ModelPart leg1;
    protected final ModelPart nose;

    public VillagerModel(float f) {
        this(f, 64, 64);
    }

    public VillagerModel(float f, int i, int j) {
        float g = 0.5f;
        this.head = new ModelPart(this).setTexSize(i, j);
        this.head.setPos(0.0f, 0.0f, 0.0f);
        this.head.texOffs(0, 0).addBox(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f, f);
        this.hat = new ModelPart(this).setTexSize(i, j);
        this.hat.setPos(0.0f, 0.0f, 0.0f);
        this.hat.texOffs(32, 0).addBox(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f, f + 0.5f);
        this.head.addChild(this.hat);
        this.hatRim = new ModelPart(this).setTexSize(i, j);
        this.hatRim.setPos(0.0f, 0.0f, 0.0f);
        this.hatRim.texOffs(30, 47).addBox(-8.0f, -8.0f, -6.0f, 16.0f, 16.0f, 1.0f, f);
        this.hatRim.xRot = -1.5707964f;
        this.hat.addChild(this.hatRim);
        this.nose = new ModelPart(this).setTexSize(i, j);
        this.nose.setPos(0.0f, -2.0f, 0.0f);
        this.nose.texOffs(24, 0).addBox(-1.0f, -1.0f, -6.0f, 2.0f, 4.0f, 2.0f, f);
        this.head.addChild(this.nose);
        this.body = new ModelPart(this).setTexSize(i, j);
        this.body.setPos(0.0f, 0.0f, 0.0f);
        this.body.texOffs(16, 20).addBox(-4.0f, 0.0f, -3.0f, 8.0f, 12.0f, 6.0f, f);
        this.jacket = new ModelPart(this).setTexSize(i, j);
        this.jacket.setPos(0.0f, 0.0f, 0.0f);
        this.jacket.texOffs(0, 38).addBox(-4.0f, 0.0f, -3.0f, 8.0f, 18.0f, 6.0f, f + 0.5f);
        this.body.addChild(this.jacket);
        this.arms = new ModelPart(this).setTexSize(i, j);
        this.arms.setPos(0.0f, 2.0f, 0.0f);
        this.arms.texOffs(44, 22).addBox(-8.0f, -2.0f, -2.0f, 4.0f, 8.0f, 4.0f, f);
        this.arms.texOffs(44, 22).addBox(4.0f, -2.0f, -2.0f, 4.0f, 8.0f, 4.0f, f, true);
        this.arms.texOffs(40, 38).addBox(-4.0f, 2.0f, -2.0f, 8.0f, 4.0f, 4.0f, f);
        this.leg0 = new ModelPart(this, 0, 22).setTexSize(i, j);
        this.leg0.setPos(-2.0f, 12.0f, 0.0f);
        this.leg0.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
        this.leg1 = new ModelPart(this, 0, 22).setTexSize(i, j);
        this.leg1.mirror = true;
        this.leg1.setPos(2.0f, 12.0f, 0.0f);
        this.leg1.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
    }

    @Override
    public void render(T entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim(entity, f, g, h, i, j, k);
        this.head.render(k);
        this.body.render(k);
        this.leg0.render(k);
        this.leg1.render(k);
        this.arms.render(k);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        boolean bl = false;
        if (entity instanceof AbstractVillager) {
            bl = ((AbstractVillager)entity).getUnhappyCounter() > 0;
        }
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
        if (bl) {
            this.head.zRot = 0.3f * Mth.sin(0.45f * h);
            this.head.xRot = 0.4f;
        } else {
            this.head.zRot = 0.0f;
        }
        this.arms.y = 3.0f;
        this.arms.z = -1.0f;
        this.arms.xRot = -0.75f;
        this.leg0.xRot = Mth.cos(f * 0.6662f) * 1.4f * g * 0.5f;
        this.leg1.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g * 0.5f;
        this.leg0.yRot = 0.0f;
        this.leg1.yRot = 0.0f;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void hatVisible(boolean bl) {
        this.head.visible = bl;
        this.hat.visible = bl;
        this.hatRim.visible = bl;
    }
}

