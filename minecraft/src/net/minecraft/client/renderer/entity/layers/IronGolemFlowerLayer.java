package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;

@Environment(EnvType.CLIENT)
public class IronGolemFlowerLayer extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {
	public IronGolemFlowerLayer(RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(IronGolem ironGolem, float f, float g, float h, float i, float j, float k, float l) {
		if (ironGolem.getOfferFlowerTick() != 0) {
			RenderSystem.enableRescaleNormal();
			RenderSystem.pushMatrix();
			RenderSystem.rotatef(5.0F + 180.0F * this.getParentModel().getFlowerHoldingArm().xRot / (float) Math.PI, 1.0F, 0.0F, 0.0F);
			RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
			RenderSystem.translatef(-0.9375F, -0.625F, -0.9375F);
			float m = 0.5F;
			RenderSystem.scalef(0.5F, -0.5F, 0.5F);
			int n = ironGolem.getLightColor();
			int o = n % 65536;
			int p = n / 65536;
			RenderSystem.glMultiTexCoord2f(33985, (float)o, (float)p);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
			Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.POPPY.defaultBlockState(), 1.0F);
			RenderSystem.popMatrix();
			RenderSystem.disableRescaleNormal();
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
