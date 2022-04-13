/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class SnowGolemHeadLayer
extends RenderLayer<SnowGolem, SnowGolemModel<SnowGolem>> {
    private final BlockRenderDispatcher blockRenderer;
    private final ItemRenderer itemRenderer;

    public SnowGolemHeadLayer(RenderLayerParent<SnowGolem, SnowGolemModel<SnowGolem>> renderLayerParent, BlockRenderDispatcher blockRenderDispatcher, ItemRenderer itemRenderer) {
        super(renderLayerParent);
        this.blockRenderer = blockRenderDispatcher;
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, SnowGolem snowGolem, float f, float g, float h, float j, float k, float l) {
        boolean bl;
        if (!snowGolem.hasPumpkin()) {
            return;
        }
        boolean bl2 = bl = Minecraft.getInstance().shouldEntityAppearGlowing(snowGolem) && snowGolem.isInvisible();
        if (snowGolem.isInvisible() && !bl) {
            return;
        }
        poseStack.pushPose();
        ((SnowGolemModel)this.getParentModel()).getHead().translateAndRotate(poseStack);
        float m = 0.625f;
        poseStack.translate(0.0, -0.34375, 0.0);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
        poseStack.scale(0.625f, -0.625f, -0.625f);
        ItemStack itemStack = new ItemStack(Blocks.CARVED_PUMPKIN);
        if (bl) {
            BlockState blockState = Blocks.CARVED_PUMPKIN.defaultBlockState();
            BakedModel bakedModel = this.blockRenderer.getBlockModel(blockState);
            int n = LivingEntityRenderer.getOverlayCoords(snowGolem, 0.0f);
            poseStack.translate(-0.5, -0.5, -0.5);
            this.blockRenderer.getModelRenderer().renderModel(poseStack.last(), multiBufferSource.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), blockState, bakedModel, 0.0f, 0.0f, 0.0f, i, n);
        } else {
            this.itemRenderer.renderStatic(snowGolem, itemStack, ItemTransforms.TransformType.HEAD, false, poseStack, multiBufferSource, snowGolem.level, i, LivingEntityRenderer.getOverlayCoords(snowGolem, 0.0f), snowGolem.getId());
        }
        poseStack.popPose();
    }
}

