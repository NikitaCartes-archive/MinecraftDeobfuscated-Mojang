package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ArmedModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ItemInHandLayer<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {
	public ItemInHandLayer(RenderLayerParent<T, M> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
		boolean bl = livingEntity.getMainArm() == HumanoidArm.RIGHT;
		ItemStack itemStack = bl ? livingEntity.getOffhandItem() : livingEntity.getMainHandItem();
		ItemStack itemStack2 = bl ? livingEntity.getMainHandItem() : livingEntity.getOffhandItem();
		if (!itemStack.isEmpty() || !itemStack2.isEmpty()) {
			GlStateManager.pushMatrix();
			if (this.getParentModel().young) {
				float m = 0.5F;
				GlStateManager.translatef(0.0F, 0.75F, 0.0F);
				GlStateManager.scalef(0.5F, 0.5F, 0.5F);
			}

			this.renderArmWithItem(livingEntity, itemStack2, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT);
			this.renderArmWithItem(livingEntity, itemStack, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT);
			GlStateManager.popMatrix();
		}
	}

	private void renderArmWithItem(LivingEntity livingEntity, ItemStack itemStack, ItemTransforms.TransformType transformType, HumanoidArm humanoidArm) {
		if (!itemStack.isEmpty()) {
			GlStateManager.pushMatrix();
			this.translateToHand(humanoidArm);
			if (livingEntity.isVisuallySneaking()) {
				GlStateManager.translatef(0.0F, 0.2F, 0.0F);
			}

			GlStateManager.rotatef(-90.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
			boolean bl = humanoidArm == HumanoidArm.LEFT;
			GlStateManager.translatef((float)(bl ? -1 : 1) / 16.0F, 0.125F, -0.625F);
			Minecraft.getInstance().getItemInHandRenderer().renderItem(livingEntity, itemStack, transformType, bl);
			GlStateManager.popMatrix();
		}
	}

	protected void translateToHand(HumanoidArm humanoidArm) {
		this.getParentModel().translateToHand(0.0625F, humanoidArm);
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
