/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class SlimeModel<T extends Entity>
extends EntityModel<T> {
    private final ModelPart cube;
    private final ModelPart eye0;
    private final ModelPart eye1;
    private final ModelPart mouth;

    public SlimeModel(int i) {
        if (i > 0) {
            this.cube = new ModelPart(this, 0, i);
            this.cube.addBox(-3.0f, 17.0f, -3.0f, 6, 6, 6);
            this.eye0 = new ModelPart(this, 32, 0);
            this.eye0.addBox(-3.25f, 18.0f, -3.5f, 2, 2, 2);
            this.eye1 = new ModelPart(this, 32, 4);
            this.eye1.addBox(1.25f, 18.0f, -3.5f, 2, 2, 2);
            this.mouth = new ModelPart(this, 32, 8);
            this.mouth.addBox(0.0f, 21.0f, -3.5f, 1, 1, 1);
        } else {
            this.cube = new ModelPart(this, 0, i);
            this.cube.addBox(-4.0f, 16.0f, -4.0f, 8, 8, 8);
            this.eye0 = null;
            this.eye1 = null;
            this.mouth = null;
        }
    }

    @Override
    public void render(T entity, float f, float g, float h, float i, float j, float k) {
        this.setupAnim(entity, f, g, h, i, j, k);
        GlStateManager.translatef(0.0f, 0.001f, 0.0f);
        this.cube.render(k);
        if (this.eye0 != null) {
            this.eye0.render(k);
            this.eye1.render(k);
            this.mouth.render(k);
        }
    }
}

