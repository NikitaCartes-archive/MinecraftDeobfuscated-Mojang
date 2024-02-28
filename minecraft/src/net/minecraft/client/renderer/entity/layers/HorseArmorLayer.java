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
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

@Environment(EnvType.CLIENT)
public class HorseArmorLayer extends RenderLayer<Horse, HorseModel<Horse>> {
	private final HorseModel<Horse> model;

	public HorseArmorLayer(RenderLayerParent<Horse, HorseModel<Horse>> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new HorseModel<>(entityModelSet.bakeLayer(ModelLayers.HORSE_ARMOR));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Horse horse, float f, float g, float h, float j, float k, float l) {
		ItemStack itemStack = horse.getBodyArmorItem();
		if (itemStack.getItem() instanceof AnimalArmorItem animalArmorItem && animalArmorItem.getBodyType() == AnimalArmorItem.BodyType.EQUESTRIAN) {
			this.getParentModel().copyPropertiesTo(this.model);
			this.model.prepareMobModel(horse, f, g, h);
			this.model.setupAnim(horse, f, g, j, k, l);
			float o;
			float p;
			float n;
			if (itemStack.is(ItemTags.DYEABLE)) {
				int m = DyedItemColor.getOrDefault(itemStack, -6265536);
				n = (float)FastColor.ARGB32.red(m) / 255.0F;
				o = (float)FastColor.ARGB32.green(m) / 255.0F;
				p = (float)FastColor.ARGB32.blue(m) / 255.0F;
			} else {
				n = 1.0F;
				o = 1.0F;
				p = 1.0F;
			}

			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(animalArmorItem.getTexture()));
			this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, n, o, p, 1.0F);
			return;
		}
	}
}
