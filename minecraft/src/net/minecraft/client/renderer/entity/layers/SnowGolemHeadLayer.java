package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class SnowGolemHeadLayer extends RenderLayer<LivingEntityRenderState, SnowGolemModel> {
	private final BlockRenderDispatcher blockRenderer;
	private final ItemRenderer itemRenderer;

	public SnowGolemHeadLayer(
		RenderLayerParent<LivingEntityRenderState, SnowGolemModel> renderLayerParent, BlockRenderDispatcher blockRenderDispatcher, ItemRenderer itemRenderer
	) {
		super(renderLayerParent);
		this.blockRenderer = blockRenderDispatcher;
		this.itemRenderer = itemRenderer;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, LivingEntityRenderState livingEntityRenderState, float f, float g) {
		BakedModel bakedModel = livingEntityRenderState.headItemModel;
		if (bakedModel != null) {
			boolean bl = livingEntityRenderState.appearsGlowing && livingEntityRenderState.isInvisible;
			if (!livingEntityRenderState.isInvisible || bl) {
				poseStack.pushPose();
				this.getParentModel().getHead().translateAndRotate(poseStack);
				float h = 0.625F;
				poseStack.translate(0.0F, -0.34375F, 0.0F);
				poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
				poseStack.scale(0.625F, -0.625F, -0.625F);
				ItemStack itemStack = livingEntityRenderState.headItem;
				if (bl && itemStack.getItem() instanceof BlockItem blockItem) {
					BlockState blockState = blockItem.getBlock().defaultBlockState();
					BakedModel bakedModel2 = this.blockRenderer.getBlockModel(blockState);
					int j = LivingEntityRenderer.getOverlayCoords(livingEntityRenderState, 0.0F);
					poseStack.translate(-0.5F, -0.5F, -0.5F);
					this.blockRenderer
						.getModelRenderer()
						.renderModel(
							poseStack.last(), multiBufferSource.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), blockState, bakedModel2, 0.0F, 0.0F, 0.0F, i, j
						);
				} else {
					this.itemRenderer
						.render(
							itemStack,
							ItemDisplayContext.HEAD,
							false,
							poseStack,
							multiBufferSource,
							i,
							LivingEntityRenderer.getOverlayCoords(livingEntityRenderState, 0.0F),
							bakedModel
						);
				}

				poseStack.popPose();
			}
		}
	}
}
