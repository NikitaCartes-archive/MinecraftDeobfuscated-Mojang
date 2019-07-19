package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;

@Environment(EnvType.CLIENT)
public class LeashKnotRenderer extends EntityRenderer<LeashFenceKnotEntity> {
	private static final ResourceLocation KNOT_LOCATION = new ResourceLocation("textures/entity/lead_knot.png");
	private final LeashKnotModel<LeashFenceKnotEntity> model = new LeashKnotModel<>();

	public LeashKnotRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public void render(LeashFenceKnotEntity leashFenceKnotEntity, double d, double e, double f, float g, float h) {
		GlStateManager.pushMatrix();
		GlStateManager.disableCull();
		GlStateManager.translatef((float)d, (float)e, (float)f);
		float i = 0.0625F;
		GlStateManager.enableRescaleNormal();
		GlStateManager.scalef(-1.0F, -1.0F, 1.0F);
		GlStateManager.enableAlphaTest();
		this.bindTexture(leashFenceKnotEntity);
		if (this.solidRender) {
			GlStateManager.enableColorMaterial();
			GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(leashFenceKnotEntity));
		}

		this.model.render(leashFenceKnotEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		if (this.solidRender) {
			GlStateManager.tearDownSolidRenderingTextureCombine();
			GlStateManager.disableColorMaterial();
		}

		GlStateManager.popMatrix();
		super.render(leashFenceKnotEntity, d, e, f, g, h);
	}

	protected ResourceLocation getTextureLocation(LeashFenceKnotEntity leashFenceKnotEntity) {
		return KNOT_LOCATION;
	}
}
