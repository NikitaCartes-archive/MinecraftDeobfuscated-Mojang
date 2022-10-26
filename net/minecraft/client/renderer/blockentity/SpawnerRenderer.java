/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

@Environment(value=EnvType.CLIENT)
public class SpawnerRenderer
implements BlockEntityRenderer<SpawnerBlockEntity> {
    private final EntityRenderDispatcher entityRenderer;

    public SpawnerRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.getEntityRenderer();
    }

    @Override
    public void render(SpawnerBlockEntity spawnerBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.0f, 0.5f);
        BaseSpawner baseSpawner = spawnerBlockEntity.getSpawner();
        Entity entity = baseSpawner.getOrCreateDisplayEntity(spawnerBlockEntity.getLevel(), spawnerBlockEntity.getLevel().getRandom(), spawnerBlockEntity.getBlockPos());
        if (entity != null) {
            float g = 0.53125f;
            float h = Math.max(entity.getBbWidth(), entity.getBbHeight());
            if ((double)h > 1.0) {
                g /= h;
            }
            poseStack.translate(0.0f, 0.4f, 0.0f);
            poseStack.mulPose(Axis.YP.rotationDegrees((float)Mth.lerp((double)f, baseSpawner.getoSpin(), baseSpawner.getSpin()) * 10.0f));
            poseStack.translate(0.0f, -0.2f, 0.0f);
            poseStack.mulPose(Axis.XP.rotationDegrees(-30.0f));
            poseStack.scale(g, g, g);
            this.entityRenderer.render(entity, 0.0, 0.0, 0.0, 0.0f, f, poseStack, multiBufferSource, i);
        }
        poseStack.popPose();
    }
}

