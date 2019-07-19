package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;

@Environment(EnvType.CLIENT)
public class WitchItemLayer<T extends LivingEntity> extends RenderLayer<T, WitchModel<T>> {
	public WitchItemLayer(RenderLayerParent<T, WitchModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
		ItemStack itemStack = livingEntity.getMainHandItem();
		if (!itemStack.isEmpty()) {
			GlStateManager.color3f(1.0F, 1.0F, 1.0F);
			GlStateManager.pushMatrix();
			if (this.getParentModel().young) {
				GlStateManager.translatef(0.0F, 0.625F, 0.0F);
				GlStateManager.rotatef(-20.0F, -1.0F, 0.0F, 0.0F);
				float m = 0.5F;
				GlStateManager.scalef(0.5F, 0.5F, 0.5F);
			}

			this.getParentModel().getNose().translateTo(0.0625F);
			GlStateManager.translatef(-0.0625F, 0.53125F, 0.21875F);
			Item item = itemStack.getItem();
			if (Block.byItem(item).defaultBlockState().getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED) {
				GlStateManager.translatef(0.0F, 0.0625F, -0.25F);
				GlStateManager.rotatef(30.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotatef(-5.0F, 0.0F, 1.0F, 0.0F);
				float n = 0.375F;
				GlStateManager.scalef(0.375F, -0.375F, 0.375F);
			} else if (item == Items.BOW) {
				GlStateManager.translatef(0.0F, 0.125F, -0.125F);
				GlStateManager.rotatef(-45.0F, 0.0F, 1.0F, 0.0F);
				float n = 0.625F;
				GlStateManager.scalef(0.625F, -0.625F, 0.625F);
				GlStateManager.rotatef(-100.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotatef(-20.0F, 0.0F, 1.0F, 0.0F);
			} else {
				GlStateManager.translatef(0.1875F, 0.1875F, 0.0F);
				float n = 0.875F;
				GlStateManager.scalef(0.875F, 0.875F, 0.875F);
				GlStateManager.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotatef(-60.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotatef(-30.0F, 0.0F, 0.0F, 1.0F);
			}

			GlStateManager.rotatef(-15.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotatef(40.0F, 0.0F, 0.0F, 1.0F);
			Minecraft.getInstance().getItemInHandRenderer().renderItem(livingEntity, itemStack, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
