package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

@Environment(EnvType.CLIENT)
public class SnowGolemHeadLayer extends RenderLayer<SnowGolem, SnowGolemModel<SnowGolem>> {
	public SnowGolemHeadLayer(RenderLayerParent<SnowGolem, SnowGolemModel<SnowGolem>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(SnowGolem snowGolem, float f, float g, float h, float i, float j, float k, float l) {
		if (!snowGolem.isInvisible() && snowGolem.hasPumpkin()) {
			RenderSystem.pushMatrix();
			this.getParentModel().getHead().translateTo(0.0625F);
			float m = 0.625F;
			RenderSystem.translatef(0.0F, -0.34375F, 0.0F);
			RenderSystem.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
			RenderSystem.scalef(0.625F, -0.625F, -0.625F);
			Minecraft.getInstance().getItemInHandRenderer().renderItem(snowGolem, new ItemStack(Blocks.CARVED_PUMPKIN), ItemTransforms.TransformType.HEAD);
			RenderSystem.popMatrix();
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return true;
	}
}
