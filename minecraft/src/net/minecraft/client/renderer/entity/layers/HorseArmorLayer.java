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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.DyeableHorseArmorItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class HorseArmorLayer extends RenderLayer<Horse, HorseModel<Horse>> {
	private final HorseModel<Horse> model;

	public HorseArmorLayer(RenderLayerParent<Horse, HorseModel<Horse>> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new HorseModel<>(entityModelSet.getLayer(ModelLayers.HORSE_ARMOR));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Horse horse, float f, float g, float h, float j, float k, float l) {
		ItemStack itemStack = horse.getArmor();
		if (itemStack.getItem() instanceof HorseArmorItem) {
			HorseArmorItem horseArmorItem = (HorseArmorItem)itemStack.getItem();
			this.getParentModel().copyPropertiesTo(this.model);
			this.model.prepareMobModel(horse, f, g, h);
			this.model.setupAnim(horse, f, g, j, k, l);
			float n;
			float o;
			float p;
			if (horseArmorItem instanceof DyeableHorseArmorItem) {
				int m = ((DyeableHorseArmorItem)horseArmorItem).getColor(itemStack);
				n = (float)(m >> 16 & 0xFF) / 255.0F;
				o = (float)(m >> 8 & 0xFF) / 255.0F;
				p = (float)(m & 0xFF) / 255.0F;
			} else {
				n = 1.0F;
				o = 1.0F;
				p = 1.0F;
			}

			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(horseArmorItem.getTexture()));
			this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, n, o, p, 1.0F);
		}
	}
}
