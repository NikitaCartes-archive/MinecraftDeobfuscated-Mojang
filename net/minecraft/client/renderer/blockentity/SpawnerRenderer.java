/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

@Environment(value=EnvType.CLIENT)
public class SpawnerRenderer
extends BlockEntityRenderer<SpawnerBlockEntity> {
    public SpawnerRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);
    }

    @Override
    public void render(SpawnerBlockEntity spawnerBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        BaseSpawner baseSpawner = spawnerBlockEntity.getSpawner();
        Entity entity = baseSpawner.getOrCreateDisplayEntity();
        if (entity != null) {
            float h = 0.53125f;
            float k = Math.max(entity.getBbWidth(), entity.getBbHeight());
            if ((double)k > 1.0) {
                h /= k;
            }
            poseStack.translate(0.0, 0.4f, 0.0);
            poseStack.mulPose(Vector3f.YP.rotationDegrees((float)Mth.lerp((double)g, baseSpawner.getoSpin(), baseSpawner.getSpin()) * 10.0f));
            poseStack.translate(0.0, -0.2f, 0.0);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-30.0f));
            poseStack.scale(h, h, h);
            entity.moveTo(d, e, f, 0.0f, 0.0f);
            Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0.0, 0.0, 0.0, 0.0f, g, poseStack, multiBufferSource);
        }
        poseStack.popPose();
    }
}

