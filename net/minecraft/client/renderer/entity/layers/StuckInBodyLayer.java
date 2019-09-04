/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.Cube;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Environment(value=EnvType.CLIENT)
public abstract class StuckInBodyLayer<T extends LivingEntity, M extends EntityModel<T>>
extends RenderLayer<T, M> {
    public StuckInBodyLayer(LivingEntityRenderer<T, M> livingEntityRenderer) {
        super(livingEntityRenderer);
    }

    protected abstract int numStuck(T var1);

    protected abstract void renderStuckItem(Entity var1, float var2, float var3, float var4, float var5);

    protected void preRenderStuckItem(T livingEntity) {
        Lighting.turnOff();
    }

    protected void postRenderStuckItem() {
        Lighting.turnOn();
    }

    @Override
    public void render(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
        int m = this.numStuck(livingEntity);
        Random random = new Random(((Entity)livingEntity).getId());
        if (m <= 0) {
            return;
        }
        this.preRenderStuckItem(livingEntity);
        for (int n = 0; n < m; ++n) {
            RenderSystem.pushMatrix();
            ModelPart modelPart = ((Model)this.getParentModel()).getRandomModelPart(random);
            Cube cube = modelPart.cubes.get(random.nextInt(modelPart.cubes.size()));
            modelPart.translateTo(0.0625f);
            float o = random.nextFloat();
            float p = random.nextFloat();
            float q = random.nextFloat();
            float r = Mth.lerp(o, cube.minX, cube.maxX) / 16.0f;
            float s = Mth.lerp(p, cube.minY, cube.maxY) / 16.0f;
            float t = Mth.lerp(q, cube.minZ, cube.maxZ) / 16.0f;
            RenderSystem.translatef(r, s, t);
            o = -1.0f * (o * 2.0f - 1.0f);
            p = -1.0f * (p * 2.0f - 1.0f);
            q = -1.0f * (q * 2.0f - 1.0f);
            this.renderStuckItem((Entity)livingEntity, o, p, q, h);
            RenderSystem.popMatrix();
        }
        this.postRenderStuckItem();
    }
}

