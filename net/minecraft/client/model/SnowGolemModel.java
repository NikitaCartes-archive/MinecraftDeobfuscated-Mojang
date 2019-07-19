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

@Environment(value=EnvType.CLIENT)
public class SnowGolemModel<T extends Entity>
extends EntityModel<T> {
    private final ModelPart piece1;
    private final ModelPart piece2;
    private final ModelPart head;
    private final ModelPart arm1;
    private final ModelPart arm2;

    public SnowGolemModel() {
        float f = 4.0f;
        float g = 0.0f;
        this.head = new ModelPart(this, 0, 0).setTexSize(64, 64);
        this.head.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, -0.5f);
        this.head.setPos(0.0f, 4.0f, 0.0f);
        this.arm1 = new ModelPart(this, 32, 0).setTexSize(64, 64);
        this.arm1.addBox(-1.0f, 0.0f, -1.0f, 12, 2, 2, -0.5f);
        this.arm1.setPos(0.0f, 6.0f, 0.0f);
        this.arm2 = new ModelPart(this, 32, 0).setTexSize(64, 64);
        this.arm2.addBox(-1.0f, 0.0f, -1.0f, 12, 2, 2, -0.5f);
        this.arm2.setPos(0.0f, 6.0f, 0.0f);
        this.piece1 = new ModelPart(this, 0, 16).setTexSize(64, 64);
        this.piece1.addBox(-5.0f, -10.0f, -5.0f, 10, 10, 10, -0.5f);
        this.piece1.setPos(0.0f, 13.0f, 0.0f);
        this.piece2 = new ModelPart(this, 0, 36).setTexSize(64, 64);
        this.piece2.addBox(-6.0f, -12.0f, -6.0f, 12, 12, 12, -0.5f);
        this.piece2.setPos(0.0f, 24.0f, 0.0f);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        super.setupAnim(entity, f, g, h, i, j, k);
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
        this.piece1.yRot = i * ((float)Math.PI / 180) * 0.25f;
        float l = Mth.sin(this.piece1.yRot);
        float m = Mth.cos(this.piece1.yRot);
        this.arm1.zRot = 1.0f;
        this.arm2.zRot = -1.0f;
        this.arm1.yRot = 0.0f + this.piece1.yRot;
        this.arm2.yRot = (float)Math.PI + this.piece1.yRot;
        this.arm1.x = m * 5.0f;
        this.arm1.z = -l * 5.0f;
        this.arm2.x = -m * 5.0f;
        this.arm2.z = l * 5.0f;
    }

    @Override
    public void render(T entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim(entity, f, g, h, i, j, k);
        this.piece1.render(k);
        this.piece2.render(k);
        this.head.render(k);
        this.arm1.render(k);
        this.arm2.render(k);
    }

    public ModelPart getHead() {
        return this.head;
    }
}

