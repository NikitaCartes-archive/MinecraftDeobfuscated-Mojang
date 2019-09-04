package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BoatModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;

@Environment(EnvType.CLIENT)
public class BoatRenderer extends EntityRenderer<Boat> {
	private static final ResourceLocation[] BOAT_TEXTURE_LOCATIONS = new ResourceLocation[]{
		new ResourceLocation("textures/entity/boat/oak.png"),
		new ResourceLocation("textures/entity/boat/spruce.png"),
		new ResourceLocation("textures/entity/boat/birch.png"),
		new ResourceLocation("textures/entity/boat/jungle.png"),
		new ResourceLocation("textures/entity/boat/acacia.png"),
		new ResourceLocation("textures/entity/boat/dark_oak.png")
	};
	protected final BoatModel model = new BoatModel();

	public BoatRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
		this.shadowRadius = 0.8F;
	}

	public void render(Boat boat, double d, double e, double f, float g, float h) {
		RenderSystem.pushMatrix();
		this.setupTranslation(d, e, f);
		this.setupRotation(boat, g, h);
		this.bindTexture(boat);
		if (this.solidRender) {
			RenderSystem.enableColorMaterial();
			RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(boat));
		}

		this.model.render(boat, h, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		if (this.solidRender) {
			RenderSystem.tearDownSolidRenderingTextureCombine();
			RenderSystem.disableColorMaterial();
		}

		RenderSystem.popMatrix();
		super.render(boat, d, e, f, g, h);
	}

	public void setupRotation(Boat boat, float f, float g) {
		RenderSystem.rotatef(180.0F - f, 0.0F, 1.0F, 0.0F);
		float h = (float)boat.getHurtTime() - g;
		float i = boat.getDamage() - g;
		if (i < 0.0F) {
			i = 0.0F;
		}

		if (h > 0.0F) {
			RenderSystem.rotatef(Mth.sin(h) * h * i / 10.0F * (float)boat.getHurtDir(), 1.0F, 0.0F, 0.0F);
		}

		float j = boat.getBubbleAngle(g);
		if (!Mth.equal(j, 0.0F)) {
			RenderSystem.rotatef(boat.getBubbleAngle(g), 1.0F, 0.0F, 1.0F);
		}

		RenderSystem.scalef(-1.0F, -1.0F, 1.0F);
	}

	public void setupTranslation(double d, double e, double f) {
		RenderSystem.translatef((float)d, (float)e + 0.375F, (float)f);
	}

	protected ResourceLocation getTextureLocation(Boat boat) {
		return BOAT_TEXTURE_LOCATIONS[boat.getBoatType().ordinal()];
	}

	@Override
	public boolean hasSecondPass() {
		return true;
	}

	public void renderSecondPass(Boat boat, double d, double e, double f, float g, float h) {
		RenderSystem.pushMatrix();
		this.setupTranslation(d, e, f);
		this.setupRotation(boat, g, h);
		this.bindTexture(boat);
		this.model.renderSecondPass(boat, h, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		RenderSystem.popMatrix();
	}
}
