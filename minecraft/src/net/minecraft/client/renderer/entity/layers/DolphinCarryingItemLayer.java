package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.DolphinModel;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

@Environment(EnvType.CLIENT)
public class DolphinCarryingItemLayer extends RenderLayer<Dolphin, DolphinModel<Dolphin>> {
	private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

	public DolphinCarryingItemLayer(RenderLayerParent<Dolphin, DolphinModel<Dolphin>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(Dolphin dolphin, float f, float g, float h, float i, float j, float k, float l) {
		boolean bl = dolphin.getMainArm() == HumanoidArm.RIGHT;
		ItemStack itemStack = bl ? dolphin.getOffhandItem() : dolphin.getMainHandItem();
		ItemStack itemStack2 = bl ? dolphin.getMainHandItem() : dolphin.getOffhandItem();
		if (!itemStack.isEmpty() || !itemStack2.isEmpty()) {
			this.renderItemOnNose(dolphin, itemStack2);
		}
	}

	private void renderItemOnNose(LivingEntity livingEntity, ItemStack itemStack) {
		if (!itemStack.isEmpty()) {
			Item item = itemStack.getItem();
			Block block = Block.byItem(item);
			RenderSystem.pushMatrix();
			boolean bl = this.itemRenderer.isGui3d(itemStack) && RenderType.getRenderLayer(block.defaultBlockState()) == RenderType.TRANSLUCENT;
			if (bl) {
				RenderSystem.depthMask(false);
			}

			float f = 1.0F;
			float g = -1.0F;
			float h = Mth.abs(livingEntity.xRot) / 60.0F;
			if (livingEntity.xRot < 0.0F) {
				RenderSystem.translatef(0.0F, 1.0F - h * 0.5F, -1.0F + h * 0.5F);
			} else {
				RenderSystem.translatef(0.0F, 1.0F + h * 0.8F, -1.0F + h * 0.2F);
			}

			this.itemRenderer.renderWithMobState(itemStack, livingEntity, ItemTransforms.TransformType.GROUND, false);
			if (bl) {
				RenderSystem.depthMask(true);
			}

			RenderSystem.popMatrix();
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
