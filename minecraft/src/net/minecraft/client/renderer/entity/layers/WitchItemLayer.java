package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
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
			RenderSystem.color3f(1.0F, 1.0F, 1.0F);
			RenderSystem.pushMatrix();
			if (this.getParentModel().young) {
				RenderSystem.translatef(0.0F, 0.625F, 0.0F);
				RenderSystem.rotatef(-20.0F, -1.0F, 0.0F, 0.0F);
				float m = 0.5F;
				RenderSystem.scalef(0.5F, 0.5F, 0.5F);
			}

			this.getParentModel().getNose().translateTo(0.0625F);
			RenderSystem.translatef(-0.0625F, 0.53125F, 0.21875F);
			Item item = itemStack.getItem();
			if (Block.byItem(item).defaultBlockState().getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED) {
				RenderSystem.translatef(0.0F, 0.0625F, -0.25F);
				RenderSystem.rotatef(30.0F, 1.0F, 0.0F, 0.0F);
				RenderSystem.rotatef(-5.0F, 0.0F, 1.0F, 0.0F);
				float n = 0.375F;
				RenderSystem.scalef(0.375F, -0.375F, 0.375F);
			} else if (item == Items.BOW) {
				RenderSystem.translatef(0.0F, 0.125F, -0.125F);
				RenderSystem.rotatef(-45.0F, 0.0F, 1.0F, 0.0F);
				float n = 0.625F;
				RenderSystem.scalef(0.625F, -0.625F, 0.625F);
				RenderSystem.rotatef(-100.0F, 1.0F, 0.0F, 0.0F);
				RenderSystem.rotatef(-20.0F, 0.0F, 1.0F, 0.0F);
			} else {
				RenderSystem.translatef(0.1875F, 0.1875F, 0.0F);
				float n = 0.875F;
				RenderSystem.scalef(0.875F, 0.875F, 0.875F);
				RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
				RenderSystem.rotatef(-60.0F, 1.0F, 0.0F, 0.0F);
				RenderSystem.rotatef(-30.0F, 0.0F, 0.0F, 1.0F);
			}

			RenderSystem.rotatef(-15.0F, 1.0F, 0.0F, 0.0F);
			RenderSystem.rotatef(40.0F, 0.0F, 0.0F, 1.0F);
			Minecraft.getInstance().getItemInHandRenderer().renderItem(livingEntity, itemStack, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
			RenderSystem.popMatrix();
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
