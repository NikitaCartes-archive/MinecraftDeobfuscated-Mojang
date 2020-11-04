/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class MushroomCowMushroomLayer<T extends MushroomCow>
extends RenderLayer<T, CowModel<T>> {
    public MushroomCowMushroomLayer(RenderLayerParent<T, CowModel<T>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T mushroomCow, float f, float g, float h, float j, float k, float l) {
        if (((AgeableMob)mushroomCow).isBaby() || ((Entity)mushroomCow).isInvisible()) {
            return;
        }
        BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
        BlockState blockState = ((MushroomCow)mushroomCow).getMushroomType().getBlockState();
        int m = LivingEntityRenderer.getOverlayCoords(mushroomCow, 0.0f);
        poseStack.pushPose();
        poseStack.translate(0.2f, -0.35f, 0.5);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-48.0f));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(-0.5, -0.5, -0.5);
        blockRenderDispatcher.renderSingleBlock(blockState, poseStack, multiBufferSource, i, m);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.2f, -0.35f, 0.5);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(42.0f));
        poseStack.translate(0.1f, 0.0, -0.6f);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-48.0f));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(-0.5, -0.5, -0.5);
        blockRenderDispatcher.renderSingleBlock(blockState, poseStack, multiBufferSource, i, m);
        poseStack.popPose();
        poseStack.pushPose();
        ((CowModel)this.getParentModel()).getHead().translateAndRotate(poseStack);
        poseStack.translate(0.0, -0.7f, -0.2f);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-78.0f));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(-0.5, -0.5, -0.5);
        blockRenderDispatcher.renderSingleBlock(blockState, poseStack, multiBufferSource, i, m);
        poseStack.popPose();
    }
}

