package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class CarriedBlockLayer extends RenderLayer<EnderMan, EndermanModel<EnderMan>> {
	public CarriedBlockLayer(RenderLayerParent<EnderMan, EndermanModel<EnderMan>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(EnderMan enderMan, float f, float g, float h, float i, float j, float k, float l) {
		BlockState blockState = enderMan.getCarriedBlock();
		if (blockState != null) {
			RenderSystem.enableRescaleNormal();
			RenderSystem.pushMatrix();
			RenderSystem.translatef(0.0F, 0.6875F, -0.75F);
			RenderSystem.rotatef(20.0F, 1.0F, 0.0F, 0.0F);
			RenderSystem.rotatef(45.0F, 0.0F, 1.0F, 0.0F);
			RenderSystem.translatef(0.25F, 0.1875F, 0.25F);
			float m = 0.5F;
			RenderSystem.scalef(-0.5F, -0.5F, 0.5F);
			int n = enderMan.getLightColor();
			int o = n % 65536;
			int p = n / 65536;
			RenderSystem.glMultiTexCoord2f(33985, (float)o, (float)p);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
			Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockState, 1.0F);
			RenderSystem.popMatrix();
			RenderSystem.disableRescaleNormal();
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
