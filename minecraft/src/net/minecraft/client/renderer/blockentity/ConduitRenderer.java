package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;

@Environment(EnvType.CLIENT)
public class ConduitRenderer extends BlockEntityRenderer<ConduitBlockEntity> {
	private static final ResourceLocation SHELL_TEXTURE = new ResourceLocation("textures/entity/conduit/base.png");
	private static final ResourceLocation ACTIVE_SHELL_TEXTURE = new ResourceLocation("textures/entity/conduit/cage.png");
	private static final ResourceLocation WIND_TEXTURE = new ResourceLocation("textures/entity/conduit/wind.png");
	private static final ResourceLocation VERTICAL_WIND_TEXTURE = new ResourceLocation("textures/entity/conduit/wind_vertical.png");
	private static final ResourceLocation OPEN_EYE_TEXTURE = new ResourceLocation("textures/entity/conduit/open_eye.png");
	private static final ResourceLocation CLOSED_EYE_TEXTURE = new ResourceLocation("textures/entity/conduit/closed_eye.png");
	private final ConduitRenderer.ShellModel shellModel = new ConduitRenderer.ShellModel();
	private final ConduitRenderer.CageModel cageModel = new ConduitRenderer.CageModel();
	private final ConduitRenderer.WindModel windModel = new ConduitRenderer.WindModel();
	private final ConduitRenderer.EyeModel eyeModel = new ConduitRenderer.EyeModel();

	public void render(ConduitBlockEntity conduitBlockEntity, double d, double e, double f, float g, int i) {
		float h = (float)conduitBlockEntity.tickCount + g;
		if (!conduitBlockEntity.isActive()) {
			float j = conduitBlockEntity.getActiveRotation(0.0F);
			this.bindTexture(SHELL_TEXTURE);
			RenderSystem.pushMatrix();
			RenderSystem.translatef((float)d + 0.5F, (float)e + 0.5F, (float)f + 0.5F);
			RenderSystem.rotatef(j, 0.0F, 1.0F, 0.0F);
			this.shellModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			RenderSystem.popMatrix();
		} else if (conduitBlockEntity.isActive()) {
			float j = conduitBlockEntity.getActiveRotation(g) * (180.0F / (float)Math.PI);
			float k = Mth.sin(h * 0.1F) / 2.0F + 0.5F;
			k = k * k + k;
			this.bindTexture(ACTIVE_SHELL_TEXTURE);
			RenderSystem.disableCull();
			RenderSystem.pushMatrix();
			RenderSystem.translatef((float)d + 0.5F, (float)e + 0.3F + k * 0.2F, (float)f + 0.5F);
			RenderSystem.rotatef(j, 0.5F, 1.0F, 0.5F);
			this.cageModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			RenderSystem.popMatrix();
			int l = 3;
			int m = conduitBlockEntity.tickCount / 3 % 22;
			this.windModel.setActiveAnim(m);
			int n = conduitBlockEntity.tickCount / 66 % 3;
			switch (n) {
				case 0:
					this.bindTexture(WIND_TEXTURE);
					RenderSystem.pushMatrix();
					RenderSystem.translatef((float)d + 0.5F, (float)e + 0.5F, (float)f + 0.5F);
					this.windModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
					RenderSystem.popMatrix();
					RenderSystem.pushMatrix();
					RenderSystem.translatef((float)d + 0.5F, (float)e + 0.5F, (float)f + 0.5F);
					RenderSystem.scalef(0.875F, 0.875F, 0.875F);
					RenderSystem.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
					RenderSystem.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
					this.windModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
					RenderSystem.popMatrix();
					break;
				case 1:
					this.bindTexture(VERTICAL_WIND_TEXTURE);
					RenderSystem.pushMatrix();
					RenderSystem.translatef((float)d + 0.5F, (float)e + 0.5F, (float)f + 0.5F);
					RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
					this.windModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
					RenderSystem.popMatrix();
					RenderSystem.pushMatrix();
					RenderSystem.translatef((float)d + 0.5F, (float)e + 0.5F, (float)f + 0.5F);
					RenderSystem.scalef(0.875F, 0.875F, 0.875F);
					RenderSystem.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
					RenderSystem.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
					this.windModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
					RenderSystem.popMatrix();
					break;
				case 2:
					this.bindTexture(WIND_TEXTURE);
					RenderSystem.pushMatrix();
					RenderSystem.translatef((float)d + 0.5F, (float)e + 0.5F, (float)f + 0.5F);
					RenderSystem.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
					this.windModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
					RenderSystem.popMatrix();
					RenderSystem.pushMatrix();
					RenderSystem.translatef((float)d + 0.5F, (float)e + 0.5F, (float)f + 0.5F);
					RenderSystem.scalef(0.875F, 0.875F, 0.875F);
					RenderSystem.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
					RenderSystem.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
					this.windModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
					RenderSystem.popMatrix();
			}

			Camera camera = this.blockEntityRenderDispatcher.camera;
			if (conduitBlockEntity.isHunting()) {
				this.bindTexture(OPEN_EYE_TEXTURE);
			} else {
				this.bindTexture(CLOSED_EYE_TEXTURE);
			}

			RenderSystem.pushMatrix();
			RenderSystem.translatef((float)d + 0.5F, (float)e + 0.3F + k * 0.2F, (float)f + 0.5F);
			RenderSystem.scalef(0.5F, 0.5F, 0.5F);
			RenderSystem.rotatef(-camera.getYRot(), 0.0F, 1.0F, 0.0F);
			RenderSystem.rotatef(camera.getXRot(), 1.0F, 0.0F, 0.0F);
			RenderSystem.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
			this.eyeModel.render(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.083333336F);
			RenderSystem.popMatrix();
		}

		super.render(conduitBlockEntity, d, e, f, g, i);
	}

	@Environment(EnvType.CLIENT)
	static class CageModel extends Model {
		private final ModelPart box;

		public CageModel() {
			this.texWidth = 32;
			this.texHeight = 16;
			this.box = new ModelPart(this, 0, 0);
			this.box.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
		}

		public void render(float f, float g, float h, float i, float j, float k) {
			this.box.render(k);
		}
	}

	@Environment(EnvType.CLIENT)
	static class EyeModel extends Model {
		private final ModelPart eye;

		public EyeModel() {
			this.texWidth = 8;
			this.texHeight = 8;
			this.eye = new ModelPart(this, 0, 0);
			this.eye.addBox(-4.0F, -4.0F, 0.0F, 8, 8, 0, 0.01F);
		}

		public void render(float f, float g, float h, float i, float j, float k) {
			this.eye.render(k);
		}
	}

	@Environment(EnvType.CLIENT)
	static class ShellModel extends Model {
		private final ModelPart box;

		public ShellModel() {
			this.texWidth = 32;
			this.texHeight = 16;
			this.box = new ModelPart(this, 0, 0);
			this.box.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6);
		}

		public void render(float f, float g, float h, float i, float j, float k) {
			this.box.render(k);
		}
	}

	@Environment(EnvType.CLIENT)
	static class WindModel extends Model {
		private final ModelPart[] box = new ModelPart[22];
		private int activeAnim;

		public WindModel() {
			this.texWidth = 64;
			this.texHeight = 1024;

			for (int i = 0; i < 22; i++) {
				this.box[i] = new ModelPart(this, 0, 32 * i);
				this.box[i].addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16);
			}
		}

		public void render(float f, float g, float h, float i, float j, float k) {
			this.box[this.activeAnim].render(k);
		}

		public void setActiveAnim(int i) {
			this.activeAnim = i;
		}
	}
}
