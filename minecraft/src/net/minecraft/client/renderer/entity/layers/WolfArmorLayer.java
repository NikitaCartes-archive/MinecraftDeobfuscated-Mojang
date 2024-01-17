package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class WolfArmorLayer extends RenderLayer<Wolf, WolfModel<Wolf>> {
	private final WolfModel<Wolf> model;

	public WolfArmorLayer(RenderLayerParent<Wolf, WolfModel<Wolf>> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new WolfModel<>(entityModelSet.bakeLayer(ModelLayers.WOLF_ARMOR));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Wolf wolf, float f, float g, float h, float j, float k, float l) {
		if (wolf.hasArmor()) {
			this.getParentModel().copyPropertiesTo(this.model);
			this.model.prepareMobModel(wolf, f, g, h);
			this.model.setupAnim(wolf, f, g, j, k, l);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(((AnimalArmorItem)Items.WOLF_ARMOR).getTexture()));
			this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
}
