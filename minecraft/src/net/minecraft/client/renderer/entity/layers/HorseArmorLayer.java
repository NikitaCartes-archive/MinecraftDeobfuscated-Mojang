package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.DyeableHorseArmorItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class HorseArmorLayer extends RenderLayer<Horse, HorseModel<Horse>> {
	private final HorseModel<Horse> model = new HorseModel<>(0.1F);

	public HorseArmorLayer(RenderLayerParent<Horse, HorseModel<Horse>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Horse horse, float f, float g, float h, float j, float k, float l, float m) {
		ItemStack itemStack = horse.getArmor();
		if (itemStack.getItem() instanceof HorseArmorItem) {
			HorseArmorItem horseArmorItem = (HorseArmorItem)itemStack.getItem();
			this.getParentModel().copyPropertiesTo(this.model);
			this.model.prepareMobModel(horse, f, g, h);
			this.model.setupAnim(horse, f, g, j, k, l, m);
			float o;
			float p;
			float q;
			if (horseArmorItem instanceof DyeableHorseArmorItem) {
				int n = ((DyeableHorseArmorItem)horseArmorItem).getColor(itemStack);
				o = (float)(n >> 16 & 0xFF) / 255.0F;
				p = (float)(n >> 8 & 0xFF) / 255.0F;
				q = (float)(n & 0xFF) / 255.0F;
			} else {
				o = 1.0F;
				p = 1.0F;
				q = 1.0F;
			}

			renderModel(this.model, horseArmorItem.getTexture(), poseStack, multiBufferSource, i, o, p, q);
		}
	}
}
