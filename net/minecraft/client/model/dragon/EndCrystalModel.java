/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model.dragon;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class EndCrystalModel<T extends Entity>
extends EntityModel<T> {
    private final ModelPart cube;
    private final ModelPart glass = new ModelPart(this);
    private final ModelPart base;

    public EndCrystalModel(float f, boolean bl) {
        this.glass.texOffs(0, 0).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f);
        this.cube = new ModelPart(this);
        this.cube.texOffs(32, 0).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f);
        if (bl) {
            this.base = new ModelPart(this);
            this.base.texOffs(0, 16).addBox(-6.0f, 0.0f, -6.0f, 12.0f, 4.0f, 12.0f);
        } else {
            this.base = null;
        }
    }

    @Override
    public void render(T entity, float f, float g, float h, float i, float j, float k) {
        RenderSystem.pushMatrix();
        RenderSystem.scalef(2.0f, 2.0f, 2.0f);
        RenderSystem.translatef(0.0f, -0.5f, 0.0f);
        if (this.base != null) {
            this.base.render(k);
        }
        RenderSystem.rotatef(g, 0.0f, 1.0f, 0.0f);
        RenderSystem.translatef(0.0f, 0.8f + h, 0.0f);
        RenderSystem.rotatef(60.0f, 0.7071f, 0.0f, 0.7071f);
        this.glass.render(k);
        float l = 0.875f;
        RenderSystem.scalef(0.875f, 0.875f, 0.875f);
        RenderSystem.rotatef(60.0f, 0.7071f, 0.0f, 0.7071f);
        RenderSystem.rotatef(g, 0.0f, 1.0f, 0.0f);
        this.glass.render(k);
        RenderSystem.scalef(0.875f, 0.875f, 0.875f);
        RenderSystem.rotatef(60.0f, 0.7071f, 0.0f, 0.7071f);
        RenderSystem.rotatef(g, 0.0f, 1.0f, 0.0f);
        this.cube.render(k);
        RenderSystem.popMatrix();
    }
}

