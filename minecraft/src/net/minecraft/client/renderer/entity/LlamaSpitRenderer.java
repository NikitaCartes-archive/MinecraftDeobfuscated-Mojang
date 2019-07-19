package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LlamaSpitModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.LlamaSpit;

@Environment(EnvType.CLIENT)
public class LlamaSpitRenderer extends EntityRenderer<LlamaSpit> {
	private static final ResourceLocation LLAMA_SPIT_LOCATION = new ResourceLocation("textures/entity/llama/spit.png");
	private final LlamaSpitModel<LlamaSpit> model = new LlamaSpitModel<>();

	public LlamaSpitRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public void render(LlamaSpit llamaSpit, double d, double e, double f, float g, float h) {
		GlStateManager.pushMatrix();
		GlStateManager.translatef((float)d, (float)e + 0.15F, (float)f);
		GlStateManager.rotatef(Mth.lerp(h, llamaSpit.yRotO, llamaSpit.yRot) - 90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef(Mth.lerp(h, llamaSpit.xRotO, llamaSpit.xRot), 0.0F, 0.0F, 1.0F);
		this.bindTexture(llamaSpit);
		if (this.solidRender) {
			GlStateManager.enableColorMaterial();
			GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(llamaSpit));
		}

		this.model.render(llamaSpit, h, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		if (this.solidRender) {
			GlStateManager.tearDownSolidRenderingTextureCombine();
			GlStateManager.disableColorMaterial();
		}

		GlStateManager.popMatrix();
		super.render(llamaSpit, d, e, f, g, h);
	}

	protected ResourceLocation getTextureLocation(LlamaSpit llamaSpit) {
		return LLAMA_SPIT_LOCATION;
	}
}
