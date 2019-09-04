package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HorseModel;
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

	public void render(Horse horse, float f, float g, float h, float i, float j, float k, float l) {
		ItemStack itemStack = horse.getArmor();
		if (itemStack.getItem() instanceof HorseArmorItem) {
			HorseArmorItem horseArmorItem = (HorseArmorItem)itemStack.getItem();
			this.getParentModel().copyPropertiesTo(this.model);
			this.model.prepareMobModel(horse, f, g, h);
			this.bindTexture(horseArmorItem.getTexture());
			if (horseArmorItem instanceof DyeableHorseArmorItem) {
				int m = ((DyeableHorseArmorItem)horseArmorItem).getColor(itemStack);
				float n = (float)(m >> 16 & 0xFF) / 255.0F;
				float o = (float)(m >> 8 & 0xFF) / 255.0F;
				float p = (float)(m & 0xFF) / 255.0F;
				RenderSystem.color4f(n, o, p, 1.0F);
				this.model.render(horse, f, g, i, j, k, l);
				return;
			}

			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.model.render(horse, f, g, i, j, k, l);
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
