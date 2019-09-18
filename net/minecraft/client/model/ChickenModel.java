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

@Environment(value=EnvType.CLIENT)
public class ChickenModel<T extends Entity>
extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart wing0;
    private final ModelPart wing1;
    private final ModelPart beak;
    private final ModelPart redThing;

    public ChickenModel() {
        int i = 16;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-2.0f, -6.0f, -2.0f, 4.0f, 6.0f, 3.0f, 0.0f);
        this.head.setPos(0.0f, 15.0f, -4.0f);
        this.beak = new ModelPart(this, 14, 0);
        this.beak.addBox(-2.0f, -4.0f, -4.0f, 4.0f, 2.0f, 2.0f, 0.0f);
        this.beak.setPos(0.0f, 15.0f, -4.0f);
        this.redThing = new ModelPart(this, 14, 4);
        this.redThing.addBox(-1.0f, -2.0f, -3.0f, 2.0f, 2.0f, 2.0f, 0.0f);
        this.redThing.setPos(0.0f, 15.0f, -4.0f);
        this.body = new ModelPart(this, 0, 9);
        this.body.addBox(-3.0f, -4.0f, -3.0f, 6.0f, 8.0f, 6.0f, 0.0f);
        this.body.setPos(0.0f, 16.0f, 0.0f);
        this.leg0 = new ModelPart(this, 26, 0);
        this.leg0.addBox(-1.0f, 0.0f, -3.0f, 3.0f, 5.0f, 3.0f);
        this.leg0.setPos(-2.0f, 19.0f, 1.0f);
        this.leg1 = new ModelPart(this, 26, 0);
        this.leg1.addBox(-1.0f, 0.0f, -3.0f, 3.0f, 5.0f, 3.0f);
        this.leg1.setPos(1.0f, 19.0f, 1.0f);
        this.wing0 = new ModelPart(this, 24, 13);
        this.wing0.addBox(0.0f, 0.0f, -3.0f, 1.0f, 4.0f, 6.0f);
        this.wing0.setPos(-4.0f, 13.0f, 0.0f);
        this.wing1 = new ModelPart(this, 24, 13);
        this.wing1.addBox(-1.0f, 0.0f, -3.0f, 1.0f, 4.0f, 6.0f);
        this.wing1.setPos(4.0f, 13.0f, 0.0f);
    }

    @Override
    public void render(T entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim(entity, f, g, h, i, j, k);
        if (this.young) {
            float l = 2.0f;
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0f, 5.0f * k, 2.0f * k);
            this.head.render(k);
            this.beak.render(k);
            this.redThing.render(k);
            RenderSystem.popMatrix();
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.5f, 0.5f, 0.5f);
            RenderSystem.translatef(0.0f, 24.0f * k, 0.0f);
            this.body.render(k);
            this.leg0.render(k);
            this.leg1.render(k);
            this.wing0.render(k);
            this.wing1.render(k);
            RenderSystem.popMatrix();
        } else {
            this.head.render(k);
            this.beak.render(k);
            this.redThing.render(k);
            this.body.render(k);
            this.leg0.render(k);
            this.leg1.render(k);
            this.wing0.render(k);
            this.wing1.render(k);
        }
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
        this.beak.xRot = this.head.xRot;
        this.beak.yRot = this.head.yRot;
        this.redThing.xRot = this.head.xRot;
        this.redThing.yRot = this.head.yRot;
        this.body.xRot = 1.5707964f;
        this.leg0.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
        this.leg1.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.wing0.zRot = h;
        this.wing1.zRot = -h;
    }
}

