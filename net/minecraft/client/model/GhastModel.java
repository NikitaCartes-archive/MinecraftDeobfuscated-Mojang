/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class GhastModel<T extends Entity>
extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart[] tentacles = new ModelPart[9];

    public GhastModel() {
        int i = -16;
        this.body = new ModelPart(this, 0, 0);
        this.body.addBox(-8.0f, -8.0f, -8.0f, 16.0f, 16.0f, 16.0f);
        this.body.y += 8.0f;
        Random random = new Random(1660L);
        for (int j = 0; j < this.tentacles.length; ++j) {
            this.tentacles[j] = new ModelPart(this, 0, 0);
            float f = (((float)(j % 3) - (float)(j / 3 % 2) * 0.5f + 0.25f) / 2.0f * 2.0f - 1.0f) * 5.0f;
            float g = ((float)(j / 3) / 2.0f * 2.0f - 1.0f) * 5.0f;
            int k = random.nextInt(7) + 8;
            this.tentacles[j].addBox(-1.0f, 0.0f, -1.0f, 2.0f, k, 2.0f);
            this.tentacles[j].x = f;
            this.tentacles[j].z = g;
            this.tentacles[j].y = 15.0f;
        }
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        for (int l = 0; l < this.tentacles.length; ++l) {
            this.tentacles[l].xRot = 0.2f * Mth.sin(h * 0.3f + (float)l) + 0.4f;
        }
    }

    @Override
    public void render(T entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim(entity, f, g, h, i, j, k);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0.0f, 0.6f, 0.0f);
        this.body.render(k);
        for (ModelPart modelPart : this.tentacles) {
            modelPart.render(k);
        }
        RenderSystem.popMatrix();
    }
}

