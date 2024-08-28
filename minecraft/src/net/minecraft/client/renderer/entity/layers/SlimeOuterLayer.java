package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;

@Environment(EnvType.CLIENT)
public class SlimeOuterLayer extends RenderLayer<SlimeRenderState, SlimeModel> {
	private final SlimeModel model;

	public SlimeOuterLayer(RenderLayerParent<SlimeRenderState, SlimeModel> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new SlimeModel(entityModelSet.bakeLayer(ModelLayers.SLIME_OUTER));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, SlimeRenderState slimeRenderState, float f, float g) {
		boolean bl = slimeRenderState.appearsGlowing && slimeRenderState.isInvisible;
		if (!slimeRenderState.isInvisible || bl) {
			VertexConsumer vertexConsumer;
			if (bl) {
				vertexConsumer = multiBufferSource.getBuffer(RenderType.outline(SlimeRenderer.SLIME_LOCATION));
			} else {
				vertexConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucent(SlimeRenderer.SLIME_LOCATION));
			}

			this.model.setupAnim(slimeRenderState);
			this.model.renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(slimeRenderState, 0.0F));
		}
	}
}
