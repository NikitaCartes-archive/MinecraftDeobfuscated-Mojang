package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
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
			renderColoredCutoutModel(this.model, ((AnimalArmorItem)Items.WOLF_ARMOR).getTexture(), poseStack, multiBufferSource, i, wolf, 1.0F, 1.0F, 1.0F);
		}
	}
}
