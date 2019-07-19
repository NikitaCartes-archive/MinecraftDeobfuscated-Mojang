/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ambient.Bat;

@Environment(value=EnvType.CLIENT)
public class BatModel
extends EntityModel<Bat> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart rightWingTip;
    private final ModelPart leftWingTip;

    public BatModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-3.0f, -3.0f, -3.0f, 6, 6, 6);
        ModelPart modelPart = new ModelPart(this, 24, 0);
        modelPart.addBox(-4.0f, -6.0f, -2.0f, 3, 4, 1);
        this.head.addChild(modelPart);
        ModelPart modelPart2 = new ModelPart(this, 24, 0);
        modelPart2.mirror = true;
        modelPart2.addBox(1.0f, -6.0f, -2.0f, 3, 4, 1);
        this.head.addChild(modelPart2);
        this.body = new ModelPart(this, 0, 16);
        this.body.addBox(-3.0f, 4.0f, -3.0f, 6, 12, 6);
        this.body.texOffs(0, 34).addBox(-5.0f, 16.0f, 0.0f, 10, 6, 1);
        this.rightWing = new ModelPart(this, 42, 0);
        this.rightWing.addBox(-12.0f, 1.0f, 1.5f, 10, 16, 1);
        this.rightWingTip = new ModelPart(this, 24, 16);
        this.rightWingTip.setPos(-12.0f, 1.0f, 1.5f);
        this.rightWingTip.addBox(-8.0f, 1.0f, 0.0f, 8, 12, 1);
        this.leftWing = new ModelPart(this, 42, 0);
        this.leftWing.mirror = true;
        this.leftWing.addBox(2.0f, 1.0f, 1.5f, 10, 16, 1);
        this.leftWingTip = new ModelPart(this, 24, 16);
        this.leftWingTip.mirror = true;
        this.leftWingTip.setPos(12.0f, 1.0f, 1.5f);
        this.leftWingTip.addBox(0.0f, 1.0f, 0.0f, 8, 12, 1);
        this.body.addChild(this.rightWing);
        this.body.addChild(this.leftWing);
        this.rightWing.addChild(this.rightWingTip);
        this.leftWing.addChild(this.leftWingTip);
    }

    @Override
    public void render(Bat bat, float f, float g, float h, float i, float j, float k) {
        this.setupAnim(bat, f, g, h, i, j, k);
        this.head.render(k);
        this.body.render(k);
    }

    @Override
    public void setupAnim(Bat bat, float f, float g, float h, float i, float j, float k) {
        if (bat.isResting()) {
            this.head.xRot = j * ((float)Math.PI / 180);
            this.head.yRot = (float)Math.PI - i * ((float)Math.PI / 180);
            this.head.zRot = (float)Math.PI;
            this.head.setPos(0.0f, -2.0f, 0.0f);
            this.rightWing.setPos(-3.0f, 0.0f, 3.0f);
            this.leftWing.setPos(3.0f, 0.0f, 3.0f);
            this.body.xRot = (float)Math.PI;
            this.rightWing.xRot = -0.15707964f;
            this.rightWing.yRot = -1.2566371f;
            this.rightWingTip.yRot = -1.7278761f;
            this.leftWing.xRot = this.rightWing.xRot;
            this.leftWing.yRot = -this.rightWing.yRot;
            this.leftWingTip.yRot = -this.rightWingTip.yRot;
        } else {
            this.head.xRot = j * ((float)Math.PI / 180);
            this.head.yRot = i * ((float)Math.PI / 180);
            this.head.zRot = 0.0f;
            this.head.setPos(0.0f, 0.0f, 0.0f);
            this.rightWing.setPos(0.0f, 0.0f, 0.0f);
            this.leftWing.setPos(0.0f, 0.0f, 0.0f);
            this.body.xRot = 0.7853982f + Mth.cos(h * 0.1f) * 0.15f;
            this.body.yRot = 0.0f;
            this.rightWing.yRot = Mth.cos(h * 1.3f) * (float)Math.PI * 0.25f;
            this.leftWing.yRot = -this.rightWing.yRot;
            this.rightWingTip.yRot = this.rightWing.yRot * 0.5f;
            this.leftWingTip.yRot = -this.rightWing.yRot * 0.5f;
        }
    }

    @Override
    public /* synthetic */ void setupAnim(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim((Bat)entity, f, g, h, i, j, k);
    }

    @Override
    public /* synthetic */ void render(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.render((Bat)entity, f, g, h, i, j, k);
    }
}

