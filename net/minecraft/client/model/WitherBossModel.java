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
import net.minecraft.world.entity.boss.wither.WitherBoss;

@Environment(value=EnvType.CLIENT)
public class WitherBossModel<T extends WitherBoss>
extends EntityModel<T> {
    private final ModelPart[] upperBodyParts;
    private final ModelPart[] heads;

    public WitherBossModel(float f) {
        this.texWidth = 64;
        this.texHeight = 64;
        this.upperBodyParts = new ModelPart[3];
        this.upperBodyParts[0] = new ModelPart(this, 0, 16);
        this.upperBodyParts[0].addBox(-10.0f, 3.9f, -0.5f, 20.0f, 3.0f, 3.0f, f);
        this.upperBodyParts[1] = new ModelPart(this).setTexSize(this.texWidth, this.texHeight);
        this.upperBodyParts[1].setPos(-2.0f, 6.9f, -0.5f);
        this.upperBodyParts[1].texOffs(0, 22).addBox(0.0f, 0.0f, 0.0f, 3.0f, 10.0f, 3.0f, f);
        this.upperBodyParts[1].texOffs(24, 22).addBox(-4.0f, 1.5f, 0.5f, 11.0f, 2.0f, 2.0f, f);
        this.upperBodyParts[1].texOffs(24, 22).addBox(-4.0f, 4.0f, 0.5f, 11.0f, 2.0f, 2.0f, f);
        this.upperBodyParts[1].texOffs(24, 22).addBox(-4.0f, 6.5f, 0.5f, 11.0f, 2.0f, 2.0f, f);
        this.upperBodyParts[2] = new ModelPart(this, 12, 22);
        this.upperBodyParts[2].addBox(0.0f, 0.0f, 0.0f, 3.0f, 6.0f, 3.0f, f);
        this.heads = new ModelPart[3];
        this.heads[0] = new ModelPart(this, 0, 0);
        this.heads[0].addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f, f);
        this.heads[1] = new ModelPart(this, 32, 0);
        this.heads[1].addBox(-4.0f, -4.0f, -4.0f, 6.0f, 6.0f, 6.0f, f);
        this.heads[1].x = -8.0f;
        this.heads[1].y = 4.0f;
        this.heads[2] = new ModelPart(this, 32, 0);
        this.heads[2].addBox(-4.0f, -4.0f, -4.0f, 6.0f, 6.0f, 6.0f, f);
        this.heads[2].x = 10.0f;
        this.heads[2].y = 4.0f;
    }

    @Override
    public void render(T witherBoss, float f, float g, float h, float i, float j, float k) {
        this.setupAnim(witherBoss, f, g, h, i, j, k);
        for (ModelPart modelPart : this.heads) {
            modelPart.render(k);
        }
        for (ModelPart modelPart : this.upperBodyParts) {
            modelPart.render(k);
        }
    }

    @Override
    public void setupAnim(T witherBoss, float f, float g, float h, float i, float j, float k) {
        float l = Mth.cos(h * 0.1f);
        this.upperBodyParts[1].xRot = (0.065f + 0.05f * l) * (float)Math.PI;
        this.upperBodyParts[2].setPos(-2.0f, 6.9f + Mth.cos(this.upperBodyParts[1].xRot) * 10.0f, -0.5f + Mth.sin(this.upperBodyParts[1].xRot) * 10.0f);
        this.upperBodyParts[2].xRot = (0.265f + 0.1f * l) * (float)Math.PI;
        this.heads[0].yRot = i * ((float)Math.PI / 180);
        this.heads[0].xRot = j * ((float)Math.PI / 180);
    }

    @Override
    public void prepareMobModel(T witherBoss, float f, float g, float h) {
        for (int i = 1; i < 3; ++i) {
            this.heads[i].yRot = (((WitherBoss)witherBoss).getHeadYRot(i - 1) - ((WitherBoss)witherBoss).yBodyRot) * ((float)Math.PI / 180);
            this.heads[i].xRot = ((WitherBoss)witherBoss).getHeadXRot(i - 1) * ((float)Math.PI / 180);
        }
    }

    @Override
    public /* synthetic */ void setupAnim(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim((T)((WitherBoss)entity), f, g, h, i, j, k);
    }

    @Override
    public /* synthetic */ void render(Entity entity, float f, float g, float h, float i, float j, float k) {
        this.render((T)((WitherBoss)entity), f, g, h, i, j, k);
    }
}

