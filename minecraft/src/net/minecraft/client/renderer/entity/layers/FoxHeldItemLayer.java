package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class FoxHeldItemLayer extends RenderLayer<Fox, FoxModel<Fox>> {
	public FoxHeldItemLayer(RenderLayerParent<Fox, FoxModel<Fox>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(Fox fox, float f, float g, float h, float i, float j, float k, float l) {
		ItemStack itemStack = fox.getItemBySlot(EquipmentSlot.MAINHAND);
		if (!itemStack.isEmpty()) {
			boolean bl = fox.isSleeping();
			boolean bl2 = fox.isBaby();
			GlStateManager.pushMatrix();
			if (bl2) {
				float m = 0.75F;
				GlStateManager.scalef(0.75F, 0.75F, 0.75F);
				GlStateManager.translatef(0.0F, 8.0F * l, 3.35F * l);
			}

			GlStateManager.translatef(this.getParentModel().head.x / 16.0F, this.getParentModel().head.y / 16.0F, this.getParentModel().head.z / 16.0F);
			float m = fox.getHeadRollAngle(h) * (180.0F / (float)Math.PI);
			GlStateManager.rotatef(m, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotatef(j, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotatef(k, 1.0F, 0.0F, 0.0F);
			if (fox.isBaby()) {
				if (bl) {
					GlStateManager.translatef(0.4F, 0.26F, 0.15F);
				} else {
					GlStateManager.translatef(0.06F, 0.26F, -0.5F);
				}
			} else if (bl) {
				GlStateManager.translatef(0.46F, 0.26F, 0.22F);
			} else {
				GlStateManager.translatef(0.06F, 0.27F, -0.5F);
			}

			GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
			if (bl) {
				GlStateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
			}

			Minecraft.getInstance().getItemRenderer().renderWithMobState(itemStack, fox, ItemTransforms.TransformType.GROUND, false);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
