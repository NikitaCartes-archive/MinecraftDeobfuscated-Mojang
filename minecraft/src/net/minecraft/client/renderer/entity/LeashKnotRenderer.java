package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
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
		RenderSystem.pushMatrix();
		RenderSystem.disableCull();
		RenderSystem.translatef((float)d, (float)e, (float)f);
		float i = 0.0625F;
		RenderSystem.enableRescaleNormal();
		RenderSystem.scalef(-1.0F, -1.0F, 1.0F);
		RenderSystem.enableAlphaTest();
		this.bindTexture(leashFenceKnotEntity);
		if (this.solidRender) {
			RenderSystem.enableColorMaterial();
			RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(leashFenceKnotEntity));
		}

		this.model.render(leashFenceKnotEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		if (this.solidRender) {
			RenderSystem.tearDownSolidRenderingTextureCombine();
			RenderSystem.disableColorMaterial();
		}

		RenderSystem.popMatrix();
		super.render(leashFenceKnotEntity, d, e, f, g, h);
	}

	protected ResourceLocation getTextureLocation(LeashFenceKnotEntity leashFenceKnotEntity) {
		return KNOT_LOCATION;
	}
}
