package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.dragon.EndCrystalModel;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

@Environment(EnvType.CLIENT)
public class EndCrystalRenderer extends EntityRenderer<EndCrystal> {
	private static final ResourceLocation END_CRYSTAL_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal.png");
	private final EntityModel<EndCrystal> model = new EndCrystalModel<>(0.0F, true);
	private final EntityModel<EndCrystal> modelWithoutBottom = new EndCrystalModel<>(0.0F, false);

	public EndCrystalRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
		this.shadowRadius = 0.5F;
	}

	public void render(EndCrystal endCrystal, double d, double e, double f, float g, float h) {
		float i = (float)endCrystal.time + h;
		RenderSystem.pushMatrix();
		RenderSystem.translatef((float)d, (float)e, (float)f);
		this.bindTexture(END_CRYSTAL_LOCATION);
		float j = Mth.sin(i * 0.2F) / 2.0F + 0.5F;
		j = j * j + j;
		if (this.solidRender) {
			RenderSystem.enableColorMaterial();
			RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(endCrystal));
		}

		if (endCrystal.showsBottom()) {
			this.model.render(endCrystal, 0.0F, i * 3.0F, j * 0.2F, 0.0F, 0.0F, 0.0625F);
		} else {
			this.modelWithoutBottom.render(endCrystal, 0.0F, i * 3.0F, j * 0.2F, 0.0F, 0.0F, 0.0625F);
		}

		if (this.solidRender) {
			RenderSystem.tearDownSolidRenderingTextureCombine();
			RenderSystem.disableColorMaterial();
		}

		RenderSystem.popMatrix();
		BlockPos blockPos = endCrystal.getBeamTarget();
		if (blockPos != null) {
			this.bindTexture(EnderDragonRenderer.CRYSTAL_BEAM_LOCATION);
			float k = (float)blockPos.getX() + 0.5F;
			float l = (float)blockPos.getY() + 0.5F;
			float m = (float)blockPos.getZ() + 0.5F;
			double n = (double)k - endCrystal.x;
			double o = (double)l - endCrystal.y;
			double p = (double)m - endCrystal.z;
			EnderDragonRenderer.renderCrystalBeams(
				d + n, e - 0.3 + (double)(j * 0.4F) + o, f + p, h, (double)k, (double)l, (double)m, endCrystal.time, endCrystal.x, endCrystal.y, endCrystal.z
			);
		}

		super.render(endCrystal, d, e, f, g, h);
	}

	protected ResourceLocation getTextureLocation(EndCrystal endCrystal) {
		return END_CRYSTAL_LOCATION;
	}

	public boolean shouldRender(EndCrystal endCrystal, Culler culler, double d, double e, double f) {
		return super.shouldRender(endCrystal, culler, d, e, f) || endCrystal.getBeamTarget() != null;
	}
}
