package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class PandaHoldsItemLayer extends RenderLayer<Panda, PandaModel<Panda>> {
	public PandaHoldsItemLayer(RenderLayerParent<Panda, PandaModel<Panda>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(Panda panda, float f, float g, float h, float i, float j, float k, float l) {
		ItemStack itemStack = panda.getItemBySlot(EquipmentSlot.MAINHAND);
		if (panda.isSitting() && !itemStack.isEmpty() && !panda.isScared()) {
			float m = -0.6F;
			float n = 1.4F;
			if (panda.isEating()) {
				m -= 0.2F * Mth.sin(i * 0.6F) + 0.2F;
				n -= 0.09F * Mth.sin(i * 0.6F);
			}

			GlStateManager.pushMatrix();
			GlStateManager.translatef(0.1F, n, m);
			Minecraft.getInstance().getItemRenderer().renderWithMobState(itemStack, panda, ItemTransforms.TransformType.GROUND, false);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
