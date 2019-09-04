package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.block.Block;

@Environment(EnvType.CLIENT)
public class VillagerTradeItemLayer<T extends LivingEntity> extends RenderLayer<T, VillagerModel<T>> {
	private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

	public VillagerTradeItemLayer(RenderLayerParent<T, VillagerModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
		ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.MAINHAND);
		if (!itemStack.isEmpty()) {
			Item item = itemStack.getItem();
			Block block = Block.byItem(item);
			RenderSystem.pushMatrix();
			boolean bl = this.itemRenderer.isGui3d(itemStack) && block.getRenderLayer() == BlockLayer.TRANSLUCENT;
			if (bl) {
				RenderSystem.depthMask(false);
			}

			RenderSystem.translatef(0.0F, 0.4F, -0.4F);
			RenderSystem.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
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
