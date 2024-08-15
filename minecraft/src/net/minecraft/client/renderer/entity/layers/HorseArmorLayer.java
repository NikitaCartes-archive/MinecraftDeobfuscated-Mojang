package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

@Environment(EnvType.CLIENT)
public class HorseArmorLayer extends RenderLayer<HorseRenderState, HorseModel> {
	private final HorseModel adultModel;
	private final HorseModel babyModel;

	public HorseArmorLayer(RenderLayerParent<HorseRenderState, HorseModel> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.adultModel = new HorseModel(entityModelSet.bakeLayer(ModelLayers.HORSE_ARMOR));
		this.babyModel = new HorseModel(entityModelSet.bakeLayer(ModelLayers.HORSE_BABY_ARMOR));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, HorseRenderState horseRenderState, float f, float g) {
		ItemStack itemStack = horseRenderState.bodyArmorItem;
		if (itemStack.getItem() instanceof AnimalArmorItem animalArmorItem && animalArmorItem.getBodyType() == AnimalArmorItem.BodyType.EQUESTRIAN) {
			HorseModel horseModel = horseRenderState.isBaby ? this.babyModel : this.adultModel;
			horseModel.setupAnim(horseRenderState);
			int j;
			if (itemStack.is(ItemTags.DYEABLE)) {
				j = ARGB.opaque(DyedItemColor.getOrDefault(itemStack, -6265536));
			} else {
				j = -1;
			}

			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(animalArmorItem.getTexture()));
			horseModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, j);
			return;
		}
	}
}
