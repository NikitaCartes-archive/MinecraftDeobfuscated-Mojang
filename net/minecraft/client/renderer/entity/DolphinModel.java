/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class DolphinModel<T extends Entity>
extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart tailFin;

    public DolphinModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        float f = 18.0f;
        float g = -8.0f;
        this.body = new ModelPart(this, 22, 0);
        this.body.addBox(-4.0f, -7.0f, 0.0f, 8.0f, 7.0f, 13.0f);
        this.body.setPos(0.0f, 22.0f, -5.0f);
        ModelPart modelPart = new ModelPart(this, 51, 0);
        modelPart.addBox(-0.5f, 0.0f, 8.0f, 1.0f, 4.0f, 5.0f);
        modelPart.xRot = 1.0471976f;
        this.body.addChild(modelPart);
        ModelPart modelPart2 = new ModelPart(this, 48, 20);
        modelPart2.mirror = true;
        modelPart2.addBox(-0.5f, -4.0f, 0.0f, 1.0f, 4.0f, 7.0f);
        modelPart2.setPos(2.0f, -2.0f, 4.0f);
        modelPart2.xRot = 1.0471976f;
        modelPart2.zRot = 2.0943952f;
        this.body.addChild(modelPart2);
        ModelPart modelPart3 = new ModelPart(this, 48, 20);
        modelPart3.addBox(-0.5f, -4.0f, 0.0f, 1.0f, 4.0f, 7.0f);
        modelPart3.setPos(-2.0f, -2.0f, 4.0f);
        modelPart3.xRot = 1.0471976f;
        modelPart3.zRot = -2.0943952f;
        this.body.addChild(modelPart3);
        this.tail = new ModelPart(this, 0, 19);
        this.tail.addBox(-2.0f, -2.5f, 0.0f, 4.0f, 5.0f, 11.0f);
        this.tail.setPos(0.0f, -2.5f, 11.0f);
        this.tail.xRot = -0.10471976f;
        this.body.addChild(this.tail);
        this.tailFin = new ModelPart(this, 19, 20);
        this.tailFin.addBox(-5.0f, -0.5f, 0.0f, 10.0f, 1.0f, 6.0f);
        this.tailFin.setPos(0.0f, 0.0f, 9.0f);
        this.tailFin.xRot = 0.0f;
        this.tail.addChild(this.tailFin);
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-4.0f, -3.0f, -3.0f, 8.0f, 7.0f, 6.0f);
        this.head.setPos(0.0f, -4.0f, -3.0f);
        ModelPart modelPart4 = new ModelPart(this, 0, 13);
        modelPart4.addBox(-1.0f, 2.0f, -7.0f, 2.0f, 2.0f, 4.0f);
        this.head.addChild(modelPart4);
        this.body.addChild(this.head);
    }

    @Override
    public void render(T entity, float f, float g, float h, float i, float j, float k) {
        this.body.render(k);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        this.body.xRot = j * ((float)Math.PI / 180);
        this.body.yRot = i * ((float)Math.PI / 180);
        if (Entity.getHorizontalDistanceSqr(((Entity)entity).getDeltaMovement()) > 1.0E-7) {
            this.body.xRot += -0.05f + -0.05f * Mth.cos(h * 0.3f);
            this.tail.xRot = -0.1f * Mth.cos(h * 0.3f);
            this.tailFin.xRot = -0.2f * Mth.cos(h * 0.3f);
        }
    }
}

