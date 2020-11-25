package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class SlimeOuterLayer<T extends LivingEntity> extends RenderLayer<T, SlimeModel<T>> {
	private final EntityModel<T> model;

	public SlimeOuterLayer(RenderLayerParent<T, SlimeModel<T>> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new SlimeModel<>(entityModelSet.bakeLayer(ModelLayers.SLIME_OUTER));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		if (!livingEntity.isInvisible()) {
			this.getParentModel().copyPropertiesTo(this.model);
			this.model.prepareMobModel(livingEntity, f, g, h);
			this.model.setupAnim(livingEntity, f, g, j, k, l);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(livingEntity)));
			this.model.renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
}
