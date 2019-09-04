package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class MushroomCowMushroomLayer<T extends MushroomCow> extends RenderLayer<T, CowModel<T>> {
	public MushroomCowMushroomLayer(RenderLayerParent<T, CowModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(T mushroomCow, float f, float g, float h, float i, float j, float k, float l) {
		if (!mushroomCow.isBaby() && !mushroomCow.isInvisible()) {
			BlockState blockState = mushroomCow.getMushroomType().getBlockState();
			this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
			RenderSystem.enableCull();
			RenderSystem.cullFace(GlStateManager.CullFace.FRONT);
			RenderSystem.pushMatrix();
			RenderSystem.scalef(1.0F, -1.0F, 1.0F);
			RenderSystem.translatef(0.2F, 0.35F, 0.5F);
			RenderSystem.rotatef(42.0F, 0.0F, 1.0F, 0.0F);
			BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
			RenderSystem.pushMatrix();
			RenderSystem.translatef(-0.5F, -0.5F, 0.5F);
			blockRenderDispatcher.renderSingleBlock(blockState, 1.0F);
			RenderSystem.popMatrix();
			RenderSystem.pushMatrix();
			RenderSystem.translatef(0.1F, 0.0F, -0.6F);
			RenderSystem.rotatef(42.0F, 0.0F, 1.0F, 0.0F);
			RenderSystem.translatef(-0.5F, -0.5F, 0.5F);
			blockRenderDispatcher.renderSingleBlock(blockState, 1.0F);
			RenderSystem.popMatrix();
			RenderSystem.popMatrix();
			RenderSystem.pushMatrix();
			this.getParentModel().getHead().translateTo(0.0625F);
			RenderSystem.scalef(1.0F, -1.0F, 1.0F);
			RenderSystem.translatef(0.0F, 0.7F, -0.2F);
			RenderSystem.rotatef(12.0F, 0.0F, 1.0F, 0.0F);
			RenderSystem.translatef(-0.5F, -0.5F, 0.5F);
			blockRenderDispatcher.renderSingleBlock(blockState, 1.0F);
			RenderSystem.popMatrix();
			RenderSystem.cullFace(GlStateManager.CullFace.BACK);
			RenderSystem.disableCull();
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return true;
	}
}
