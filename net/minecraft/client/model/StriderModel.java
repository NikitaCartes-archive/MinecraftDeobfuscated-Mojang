/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Strider;

@Environment(value=EnvType.CLIENT)
public class StriderModel<T extends Strider>
extends ListModel<T> {
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart body;
    private final ModelPart bristle0;
    private final ModelPart bristle1;
    private final ModelPart bristle2;
    private final ModelPart bristle3;
    private final ModelPart bristle4;
    private final ModelPart bristle5;

    public StriderModel() {
        this.texWidth = 128;
        this.texHeight = 128;
        this.rightLeg = new ModelPart(this, 0, 32);
        this.rightLeg.setPos(-4.0f, 8.0f, 0.0f);
        this.rightLeg.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 16.0f, 4.0f, 0.0f);
        this.leftLeg = new ModelPart(this, 0, 32);
        this.leftLeg.setPos(4.0f, 8.0f, 0.0f);
        this.leftLeg.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 16.0f, 4.0f, 0.0f);
        this.body = new ModelPart(this, 0, 0);
        this.body.setPos(0.0f, 1.0f, 0.0f);
        this.body.addBox(-8.0f, -6.0f, -8.0f, 16.0f, 14.0f, 16.0f, 0.0f);
        this.bristle0 = new ModelPart(this, 16, 65);
        this.bristle0.setPos(-8.0f, 4.0f, -8.0f);
        this.bristle0.addBox(-12.0f, 0.0f, 0.0f, 12.0f, 0.0f, 16.0f, 0.0f, true);
        this.setRotationAngle(this.bristle0, 0.0f, 0.0f, -1.2217305f);
        this.bristle1 = new ModelPart(this, 16, 49);
        this.bristle1.setPos(-8.0f, -1.0f, -8.0f);
        this.bristle1.addBox(-12.0f, 0.0f, 0.0f, 12.0f, 0.0f, 16.0f, 0.0f, true);
        this.setRotationAngle(this.bristle1, 0.0f, 0.0f, -1.134464f);
        this.bristle2 = new ModelPart(this, 16, 33);
        this.bristle2.setPos(-8.0f, -5.0f, -8.0f);
        this.bristle2.addBox(-12.0f, 0.0f, 0.0f, 12.0f, 0.0f, 16.0f, 0.0f, true);
        this.setRotationAngle(this.bristle2, 0.0f, 0.0f, -0.87266463f);
        this.bristle3 = new ModelPart(this, 16, 33);
        this.bristle3.setPos(8.0f, -6.0f, -8.0f);
        this.bristle3.addBox(0.0f, 0.0f, 0.0f, 12.0f, 0.0f, 16.0f, 0.0f);
        this.setRotationAngle(this.bristle3, 0.0f, 0.0f, 0.87266463f);
        this.bristle4 = new ModelPart(this, 16, 49);
        this.bristle4.setPos(8.0f, -2.0f, -8.0f);
        this.bristle4.addBox(0.0f, 0.0f, 0.0f, 12.0f, 0.0f, 16.0f, 0.0f);
        this.setRotationAngle(this.bristle4, 0.0f, 0.0f, 1.134464f);
        this.bristle5 = new ModelPart(this, 16, 65);
        this.bristle5.setPos(8.0f, 3.0f, -8.0f);
        this.bristle5.addBox(0.0f, 0.0f, 0.0f, 12.0f, 0.0f, 16.0f, 0.0f);
        this.setRotationAngle(this.bristle5, 0.0f, 0.0f, 1.2217305f);
        this.body.addChild(this.bristle0);
        this.body.addChild(this.bristle1);
        this.body.addChild(this.bristle2);
        this.body.addChild(this.bristle3);
        this.body.addChild(this.bristle4);
        this.body.addChild(this.bristle5);
    }

    @Override
    public void setupAnim(Strider strider, float f, float g, float h, float i, float j) {
        g = Math.min(0.25f, g);
        if (strider.getPassengers().size() <= 0) {
            this.body.xRot = j * ((float)Math.PI / 180);
            this.body.yRot = i * ((float)Math.PI / 180);
        } else {
            this.body.xRot = 0.0f;
            this.body.yRot = 0.0f;
        }
        float k = 1.5f;
        this.body.zRot = 0.1f * Mth.sin(f * 1.5f) * 4.0f * g;
        this.body.y = 2.0f;
        this.body.y -= 2.0f * Mth.cos(f * 1.5f) * 2.0f * g;
        this.leftLeg.xRot = Mth.sin(f * 1.5f * 0.5f) * 2.0f * g;
        this.rightLeg.xRot = Mth.sin(f * 1.5f * 0.5f + (float)Math.PI) * 2.0f * g;
        this.leftLeg.zRot = 0.17453292f * Mth.cos(f * 1.5f * 0.5f) * g;
        this.rightLeg.zRot = 0.17453292f * Mth.cos(f * 1.5f * 0.5f + (float)Math.PI) * g;
        this.leftLeg.y = 8.0f + 2.0f * Mth.sin(f * 1.5f * 0.5f + (float)Math.PI) * 2.0f * g;
        this.rightLeg.y = 8.0f + 2.0f * Mth.sin(f * 1.5f * 0.5f) * 2.0f * g;
        this.bristle0.zRot = -1.2217305f;
        this.bristle1.zRot = -1.134464f;
        this.bristle2.zRot = -0.87266463f;
        this.bristle3.zRot = 0.87266463f;
        this.bristle4.zRot = 1.134464f;
        this.bristle5.zRot = 1.2217305f;
        float l = Mth.cos(f * 1.5f + (float)Math.PI) * g;
        this.bristle0.zRot += l * 1.3f;
        this.bristle1.zRot += l * 1.2f;
        this.bristle2.zRot += l * 0.6f;
        this.bristle3.zRot += l * 0.6f;
        this.bristle4.zRot += l * 1.2f;
        this.bristle5.zRot += l * 1.3f;
        float m = 1.0f;
        float n = 1.0f;
        this.bristle0.zRot += 0.05f * Mth.sin(h * 1.0f * -0.4f);
        this.bristle1.zRot += 0.1f * Mth.sin(h * 1.0f * 0.2f);
        this.bristle2.zRot += 0.1f * Mth.sin(h * 1.0f * 0.4f);
        this.bristle3.zRot += 0.1f * Mth.sin(h * 1.0f * 0.4f);
        this.bristle4.zRot += 0.1f * Mth.sin(h * 1.0f * 0.2f);
        this.bristle5.zRot += 0.05f * Mth.sin(h * 1.0f * -0.4f);
    }

    public void setRotationAngle(ModelPart modelPart, float f, float g, float h) {
        modelPart.xRot = f;
        modelPart.yRot = g;
        modelPart.zRot = h;
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.body, this.leftLeg, this.rightLeg);
    }
}

