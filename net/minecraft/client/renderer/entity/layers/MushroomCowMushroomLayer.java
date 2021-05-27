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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
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
        boolean bl;
        if (((AgeableMob)mushroomCow).isBaby()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean bl2 = bl = minecraft.shouldEntityAppearGlowing((Entity)mushroomCow) && ((Entity)mushroomCow).isInvisible();
        if (((Entity)mushroomCow).isInvisible() && !bl) {
            return;
        }
        BlockRenderDispatcher blockRenderDispatcher = minecraft.getBlockRenderer();
        BlockState blockState = ((MushroomCow)mushroomCow).getMushroomType().getBlockState();
        int m = LivingEntityRenderer.getOverlayCoords(mushroomCow, 0.0f);
        BakedModel bakedModel = blockRenderDispatcher.getBlockModel(blockState);
        poseStack.pushPose();
        poseStack.translate(0.2f, -0.35f, 0.5);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-48.0f));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(-0.5, -0.5, -0.5);
        this.renderMushroomBlock(poseStack, multiBufferSource, i, bl, blockRenderDispatcher, blockState, m, bakedModel);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.2f, -0.35f, 0.5);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(42.0f));
        poseStack.translate(0.1f, 0.0, -0.6f);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-48.0f));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(-0.5, -0.5, -0.5);
        this.renderMushroomBlock(poseStack, multiBufferSource, i, bl, blockRenderDispatcher, blockState, m, bakedModel);
        poseStack.popPose();
        poseStack.pushPose();
        ((CowModel)this.getParentModel()).getHead().translateAndRotate(poseStack);
        poseStack.translate(0.0, -0.7f, -0.2f);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-78.0f));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(-0.5, -0.5, -0.5);
        this.renderMushroomBlock(poseStack, multiBufferSource, i, bl, blockRenderDispatcher, blockState, m, bakedModel);
        poseStack.popPose();
    }

    private void renderMushroomBlock(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, boolean bl, BlockRenderDispatcher blockRenderDispatcher, BlockState blockState, int j, BakedModel bakedModel) {
        if (bl) {
            blockRenderDispatcher.getModelRenderer().renderModel(poseStack.last(), multiBufferSource.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), blockState, bakedModel, 0.0f, 0.0f, 0.0f, i, j);
        } else {
            blockRenderDispatcher.renderSingleBlock(blockState, poseStack, multiBufferSource, i, j);
        }
    }
}

