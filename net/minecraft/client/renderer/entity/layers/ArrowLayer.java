/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.Cube;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;

@Environment(value=EnvType.CLIENT)
public class ArrowLayer<T extends LivingEntity, M extends EntityModel<T>>
extends RenderLayer<T, M> {
    private final EntityRenderDispatcher dispatcher;

    public ArrowLayer(LivingEntityRenderer<T, M> livingEntityRenderer) {
        super(livingEntityRenderer);
        this.dispatcher = livingEntityRenderer.getDispatcher();
    }

    @Override
    public void render(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
        int m = ((LivingEntity)livingEntity).getArrowCount();
        if (m <= 0) {
            return;
        }
        Arrow entity = new Arrow(((LivingEntity)livingEntity).level, ((LivingEntity)livingEntity).x, ((LivingEntity)livingEntity).y, ((LivingEntity)livingEntity).z);
        Random random = new Random(((Entity)livingEntity).getId());
        Lighting.turnOff();
        for (int n = 0; n < m; ++n) {
            GlStateManager.pushMatrix();
            ModelPart modelPart = ((Model)this.getParentModel()).getRandomModelPart(random);
            Cube cube = modelPart.cubes.get(random.nextInt(modelPart.cubes.size()));
            modelPart.translateTo(0.0625f);
            float o = random.nextFloat();
            float p = random.nextFloat();
            float q = random.nextFloat();
            float r = Mth.lerp(o, cube.minX, cube.maxX) / 16.0f;
            float s = Mth.lerp(p, cube.minY, cube.maxY) / 16.0f;
            float t = Mth.lerp(q, cube.minZ, cube.maxZ) / 16.0f;
            GlStateManager.translatef(r, s, t);
            o = o * 2.0f - 1.0f;
            p = p * 2.0f - 1.0f;
            q = q * 2.0f - 1.0f;
            float u = Mth.sqrt((o *= -1.0f) * o + (q *= -1.0f) * q);
            entity.yRot = (float)(Math.atan2(o, q) * 57.2957763671875);
            entity.xRot = (float)(Math.atan2(p *= -1.0f, u) * 57.2957763671875);
            entity.yRotO = entity.yRot;
            entity.xRotO = entity.xRot;
            double d = 0.0;
            double e = 0.0;
            double v = 0.0;
            this.dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, h, false);
            GlStateManager.popMatrix();
        }
        Lighting.turnOn();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

