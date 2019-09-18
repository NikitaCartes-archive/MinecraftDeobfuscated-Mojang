/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Rabbit;

@Environment(value=EnvType.CLIENT)
public class RabbitModel<T extends Rabbit>
extends EntityModel<T> {
    private final ModelPart rearFootLeft = new ModelPart(this, 26, 24);
    private final ModelPart rearFootRight;
    private final ModelPart haunchLeft;
    private final ModelPart haunchRight;
    private final ModelPart body;
    private final ModelPart frontLegLeft;
    private final ModelPart frontLegRight;
    private final ModelPart head;
    private final ModelPart earRight;
    private final ModelPart earLeft;
    private final ModelPart tail;
    private final ModelPart nose;
    private float jumpRotation;

    public RabbitModel() {
        this.rearFootLeft.addBox(-1.0f, 5.5f, -3.7f, 2.0f, 1.0f, 7.0f);
        this.rearFootLeft.setPos(3.0f, 17.5f, 3.7f);
        this.rearFootLeft.mirror = true;
        this.setRotation(this.rearFootLeft, 0.0f, 0.0f, 0.0f);
        this.rearFootRight = new ModelPart(this, 8, 24);
        this.rearFootRight.addBox(-1.0f, 5.5f, -3.7f, 2.0f, 1.0f, 7.0f);
        this.rearFootRight.setPos(-3.0f, 17.5f, 3.7f);
        this.rearFootRight.mirror = true;
        this.setRotation(this.rearFootRight, 0.0f, 0.0f, 0.0f);
        this.haunchLeft = new ModelPart(this, 30, 15);
        this.haunchLeft.addBox(-1.0f, 0.0f, 0.0f, 2.0f, 4.0f, 5.0f);
        this.haunchLeft.setPos(3.0f, 17.5f, 3.7f);
        this.haunchLeft.mirror = true;
        this.setRotation(this.haunchLeft, -0.34906584f, 0.0f, 0.0f);
        this.haunchRight = new ModelPart(this, 16, 15);
        this.haunchRight.addBox(-1.0f, 0.0f, 0.0f, 2.0f, 4.0f, 5.0f);
        this.haunchRight.setPos(-3.0f, 17.5f, 3.7f);
        this.haunchRight.mirror = true;
        this.setRotation(this.haunchRight, -0.34906584f, 0.0f, 0.0f);
        this.body = new ModelPart(this, 0, 0);
        this.body.addBox(-3.0f, -2.0f, -10.0f, 6.0f, 5.0f, 10.0f);
        this.body.setPos(0.0f, 19.0f, 8.0f);
        this.body.mirror = true;
        this.setRotation(this.body, -0.34906584f, 0.0f, 0.0f);
        this.frontLegLeft = new ModelPart(this, 8, 15);
        this.frontLegLeft.addBox(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f);
        this.frontLegLeft.setPos(3.0f, 17.0f, -1.0f);
        this.frontLegLeft.mirror = true;
        this.setRotation(this.frontLegLeft, -0.17453292f, 0.0f, 0.0f);
        this.frontLegRight = new ModelPart(this, 0, 15);
        this.frontLegRight.addBox(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f);
        this.frontLegRight.setPos(-3.0f, 17.0f, -1.0f);
        this.frontLegRight.mirror = true;
        this.setRotation(this.frontLegRight, -0.17453292f, 0.0f, 0.0f);
        this.head = new ModelPart(this, 32, 0);
        this.head.addBox(-2.5f, -4.0f, -5.0f, 5.0f, 4.0f, 5.0f);
        this.head.setPos(0.0f, 16.0f, -1.0f);
        this.head.mirror = true;
        this.setRotation(this.head, 0.0f, 0.0f, 0.0f);
        this.earRight = new ModelPart(this, 52, 0);
        this.earRight.addBox(-2.5f, -9.0f, -1.0f, 2.0f, 5.0f, 1.0f);
        this.earRight.setPos(0.0f, 16.0f, -1.0f);
        this.earRight.mirror = true;
        this.setRotation(this.earRight, 0.0f, -0.2617994f, 0.0f);
        this.earLeft = new ModelPart(this, 58, 0);
        this.earLeft.addBox(0.5f, -9.0f, -1.0f, 2.0f, 5.0f, 1.0f);
        this.earLeft.setPos(0.0f, 16.0f, -1.0f);
        this.earLeft.mirror = true;
        this.setRotation(this.earLeft, 0.0f, 0.2617994f, 0.0f);
        this.tail = new ModelPart(this, 52, 6);
        this.tail.addBox(-1.5f, -1.5f, 0.0f, 3.0f, 3.0f, 2.0f);
        this.tail.setPos(0.0f, 20.0f, 7.0f);
        this.tail.mirror = true;
        this.setRotation(this.tail, -0.3490659f, 0.0f, 0.0f);
        this.nose = new ModelPart(this, 32, 9);
        this.nose.addBox(-0.5f, -2.5f, -5.5f, 1.0f, 1.0f, 1.0f);
        this.nose.setPos(0.0f, 16.0f, -1.0f);
        this.nose.mirror = true;
        this.setRotation(this.nose, 0.0f, 0.0f, 0.0f);
    }

    private void setRotation(ModelPart modelPart, float f, float g, float h) {
        modelPart.xRot = f;
        modelPart.yRot = g;
        modelPart.zRot = h;
    }

    @Override
    public void render(T rabbit, float f, float g, float h, float i, float j, float k) {
        this.setupAnim(rabbit, f, g, h, i, j, k);
        if (this.young) {
            float l = 1.5f;
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.56666666f, 0.56666666f, 0.56666666f);
            RenderSystem.translatef(0.0f, 22.0f * k, 2.0f * k);
            this.head.render(k);
            this.earLeft.render(k);
            this.earRight.render(k);
            this.nose.render(k);
            RenderSystem.popMatrix();
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.4f, 0.4f, 0.4f);
            RenderSystem.translatef(0.0f, 36.0f * k, 0.0f);
            this.rearFootLeft.render(k);
            this.rearFootRight.render(k);
            this.haunchLeft.render(k);
            this.haunchRight.render(k);
            this.body.render(k);
            this.frontLegLeft.render(k);
            this.frontLegRight.render(k);
            this.tail.render(k);
            RenderSystem.popMatrix();
        } else {
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.6f, 0.6f, 0.6f);
            RenderSystem.translatef(0.0f, 16.0f * k, 0.0f);
            this.rearFootLeft.render(k);
            this.rearFootRight.render(k);
            this.haunchLeft.render(k);
            this.haunchRight.render(k);
            this.body.render(k);
            this.frontLegLeft.render(k);
            this.frontLegRight.render(k);
            this.head.render(k);
            this.earRight.render(k);
            this.earLeft.render(k);
            this.tail.render(k);
            this.nose.render(k);
            RenderSystem.popMatrix();
        }
    }

    @Override
    public void setupAnim(T rabbit, float f, float g, float h, float i, float j, float k) {
        float l = h - (float)((Rabbit)rabbit).tickCount;
        this.nose.xRot = j * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
        this.earRight.xRot = j * ((float)Math.PI / 180);
        this.earLeft.xRot = j * ((float)Math.PI / 180);
        this.nose.yRot = i * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
        this.earRight.yRot = this.nose.yRot - 0.2617994f;
        this.earLeft.yRot = this.nose.yRot + 0.2617994f;
        this.jumpRotation = Mth.sin(((Rabbit)rabbit).getJumpCompletion(l) * (float)Math.PI);
        this.haunchLeft.xRot = (this.jumpRotation * 50.0f - 21.0f) * ((float)Math.PI / 180);
        this.haunchRight.xRot = (this.jumpRotation * 50.0f - 21.0f) * ((float)Math.PI / 180);
        this.rearFootLeft.xRot = this.jumpRotation * 50.0f * ((float)Math.PI / 180);
        this.rearFootRight.xRot = this.jumpRotation * 50.0f * ((float)Math.PI / 180);
        this.frontLegLeft.xRot = (this.jumpRotation * -40.0f - 11.0f) * ((float)Math.PI / 180);
        this.frontLegRight.xRot = (this.jumpRotation * -40.0f - 11.0f) * ((float)Math.PI / 180);
    }

    @Override
    public void prepareMobModel(T rabbit, float f, float g, float h) {
        super.prepareMobModel(rabbit, f, g, h);
        this.jumpRotation = Mth.sin(((Rabbit)rabbit).getJumpCompletion(h) * (float)Math.PI);
    }

    @Override
    public /* synthetic */ void setupAnim(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim((T)((Rabbit)entity), f, g, h, i, j, k);
    }

    @Override
    public /* synthetic */ void render(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.render((T)((Rabbit)entity), f, g, h, i, j, k);
    }
}

