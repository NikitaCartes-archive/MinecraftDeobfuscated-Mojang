package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.Blocks;

@Environment(EnvType.CLIENT)
public class TntRenderer extends EntityRenderer<PrimedTnt> {
	public TntRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
		this.shadowRadius = 0.5F;
	}

	public void render(PrimedTnt primedTnt, double d, double e, double f, float g, float h) {
		BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
		RenderSystem.pushMatrix();
		RenderSystem.translatef((float)d, (float)e + 0.5F, (float)f);
		if ((float)primedTnt.getLife() - h + 1.0F < 10.0F) {
			float i = 1.0F - ((float)primedTnt.getLife() - h + 1.0F) / 10.0F;
			i = Mth.clamp(i, 0.0F, 1.0F);
			i *= i;
			i *= i;
			float j = 1.0F + i * 0.3F;
			RenderSystem.scalef(j, j, j);
		}

		float i = (1.0F - ((float)primedTnt.getLife() - h + 1.0F) / 100.0F) * 0.8F;
		this.bindTexture(primedTnt);
		RenderSystem.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
		RenderSystem.translatef(-0.5F, -0.5F, 0.5F);
		blockRenderDispatcher.renderSingleBlock(Blocks.TNT.defaultBlockState(), primedTnt.getBrightness());
		RenderSystem.translatef(0.0F, 0.0F, 1.0F);
		if (this.solidRender) {
			RenderSystem.enableColorMaterial();
			RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(primedTnt));
			blockRenderDispatcher.renderSingleBlock(Blocks.TNT.defaultBlockState(), 1.0F);
			RenderSystem.tearDownSolidRenderingTextureCombine();
			RenderSystem.disableColorMaterial();
		} else if (primedTnt.getLife() / 5 % 2 == 0) {
			RenderSystem.disableTexture();
			RenderSystem.disableLighting();
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, i);
			RenderSystem.polygonOffset(-3.0F, -3.0F);
			RenderSystem.enablePolygonOffset();
			blockRenderDispatcher.renderSingleBlock(Blocks.TNT.defaultBlockState(), 1.0F);
			RenderSystem.polygonOffset(0.0F, 0.0F);
			RenderSystem.disablePolygonOffset();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.disableBlend();
			RenderSystem.enableLighting();
			RenderSystem.enableTexture();
		}

		RenderSystem.popMatrix();
		super.render(primedTnt, d, e, f, g, h);
	}

	protected ResourceLocation getTextureLocation(PrimedTnt primedTnt) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
