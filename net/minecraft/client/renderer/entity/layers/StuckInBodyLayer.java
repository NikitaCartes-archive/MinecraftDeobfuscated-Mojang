/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Environment(value=EnvType.CLIENT)
public abstract class StuckInBodyLayer<T extends LivingEntity, M extends PlayerModel<T>>
extends RenderLayer<T, M> {
    public StuckInBodyLayer(LivingEntityRenderer<T, M> livingEntityRenderer) {
        super(livingEntityRenderer);
    }

    protected abstract int numStuck(T var1);

    protected abstract void renderStuckItem(PoseStack var1, MultiBufferSource var2, Entity var3, float var4, float var5, float var6, float var7);

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m) {
        int n = this.numStuck(livingEntity);
        Random random = new Random(((Entity)livingEntity).getId());
        if (n <= 0) {
            return;
        }
        for (int o = 0; o < n; ++o) {
            poseStack.pushPose();
            ModelPart modelPart = ((PlayerModel)this.getParentModel()).getRandomModelPart(random);
            ModelPart.Cube cube = modelPart.getRandomCube(random);
            modelPart.translateAndRotate(poseStack, 0.0625f);
            float p = random.nextFloat();
            float q = random.nextFloat();
            float r = random.nextFloat();
            float s = Mth.lerp(p, cube.minX, cube.maxX) / 16.0f;
            float t = Mth.lerp(q, cube.minY, cube.maxY) / 16.0f;
            float u = Mth.lerp(r, cube.minZ, cube.maxZ) / 16.0f;
            poseStack.translate(s, t, u);
            p = -1.0f * (p * 2.0f - 1.0f);
            q = -1.0f * (q * 2.0f - 1.0f);
            r = -1.0f * (r * 2.0f - 1.0f);
            this.renderStuckItem(poseStack, multiBufferSource, (Entity)livingEntity, p, q, r, h);
            poseStack.popPose();
        }
    }
}

