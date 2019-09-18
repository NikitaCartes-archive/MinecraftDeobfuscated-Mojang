package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;

@Environment(EnvType.CLIENT)
public class ConduitRenderer extends BatchedBlockEntityRenderer<ConduitBlockEntity> {
	public static final ResourceLocation SHELL_TEXTURE = new ResourceLocation("entity/conduit/base");
	public static final ResourceLocation ACTIVE_SHELL_TEXTURE = new ResourceLocation("entity/conduit/cage");
	public static final ResourceLocation WIND_TEXTURE = new ResourceLocation("entity/conduit/wind");
	public static final ResourceLocation VERTICAL_WIND_TEXTURE = new ResourceLocation("entity/conduit/wind_vertical");
	public static final ResourceLocation OPEN_EYE_TEXTURE = new ResourceLocation("entity/conduit/open_eye");
	public static final ResourceLocation CLOSED_EYE_TEXTURE = new ResourceLocation("entity/conduit/closed_eye");
	private final ModelPart eye = new ModelPart(8, 8, 0, 0);
	private final ModelPart wind;
	private final ModelPart shell;
	private final ModelPart cage;

	public ConduitRenderer() {
		this.eye.addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, 0.01F);
		this.wind = new ModelPart(64, 32, 0, 0);
		this.wind.addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F);
		this.shell = new ModelPart(32, 16, 0, 0);
		this.shell.addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F);
		this.cage = new ModelPart(32, 16, 0, 0);
		this.cage.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
	}

	protected void renderToBuffer(
		ConduitBlockEntity conduitBlockEntity, double d, double e, double f, float g, int i, RenderType renderType, BufferBuilder bufferBuilder, int j, int k
	) {
		float h = (float)conduitBlockEntity.tickCount + g;
		if (!conduitBlockEntity.isActive()) {
			float l = conduitBlockEntity.getActiveRotation(0.0F);
			bufferBuilder.pushPose();
			bufferBuilder.translate(0.5, 0.5, 0.5);
			bufferBuilder.multiplyPose(new Quaternion(Vector3f.YP, l, true));
			this.shell.render(bufferBuilder, 0.0625F, j, k, this.getSprite(SHELL_TEXTURE));
			bufferBuilder.popPose();
		} else {
			float l = conduitBlockEntity.getActiveRotation(g) * (180.0F / (float)Math.PI);
			float m = Mth.sin(h * 0.1F) / 2.0F + 0.5F;
			m = m * m + m;
			bufferBuilder.pushPose();
			bufferBuilder.translate(0.5, (double)(0.3F + m * 0.2F), 0.5);
			Vector3f vector3f = new Vector3f(0.5F, 1.0F, 0.5F);
			vector3f.normalize();
			bufferBuilder.multiplyPose(new Quaternion(vector3f, l, true));
			this.cage.render(bufferBuilder, 0.0625F, j, k, this.getSprite(ACTIVE_SHELL_TEXTURE));
			bufferBuilder.popPose();
			int n = conduitBlockEntity.tickCount / 66 % 3;
			bufferBuilder.pushPose();
			bufferBuilder.translate(0.5, 0.5, 0.5);
			if (n == 1) {
				bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, 90.0F, true));
			} else if (n == 2) {
				bufferBuilder.multiplyPose(new Quaternion(Vector3f.ZP, 90.0F, true));
			}

			this.wind.render(bufferBuilder, 0.0625F, j, k, this.getSprite(n == 1 ? VERTICAL_WIND_TEXTURE : WIND_TEXTURE));
			bufferBuilder.popPose();
			bufferBuilder.pushPose();
			bufferBuilder.translate(0.5, 0.5, 0.5);
			bufferBuilder.scale(0.875F, 0.875F, 0.875F);
			bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, 180.0F, true));
			bufferBuilder.multiplyPose(new Quaternion(Vector3f.ZP, 180.0F, true));
			this.wind.render(0.0625F);
			bufferBuilder.popPose();
			Camera camera = this.blockEntityRenderDispatcher.camera;
			bufferBuilder.pushPose();
			bufferBuilder.translate(0.5, (double)(0.3F + m * 0.2F), 0.5);
			bufferBuilder.scale(0.5F, 0.5F, 0.5F);
			bufferBuilder.multiplyPose(new Quaternion(Vector3f.YP, -camera.getYRot(), true));
			bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, camera.getXRot(), true));
			bufferBuilder.multiplyPose(new Quaternion(Vector3f.ZP, 180.0F, true));
			this.eye.render(bufferBuilder, 0.083333336F, k, j, this.getSprite(conduitBlockEntity.isHunting() ? OPEN_EYE_TEXTURE : CLOSED_EYE_TEXTURE));
			bufferBuilder.popPose();
		}
	}
}
