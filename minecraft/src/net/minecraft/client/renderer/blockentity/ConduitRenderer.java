package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;

@Environment(EnvType.CLIENT)
public class ConduitRenderer extends BlockEntityRenderer<ConduitBlockEntity> {
	public static final ResourceLocation SHELL_TEXTURE = new ResourceLocation("entity/conduit/base");
	public static final ResourceLocation ACTIVE_SHELL_TEXTURE = new ResourceLocation("entity/conduit/cage");
	public static final ResourceLocation WIND_TEXTURE = new ResourceLocation("entity/conduit/wind");
	public static final ResourceLocation VERTICAL_WIND_TEXTURE = new ResourceLocation("entity/conduit/wind_vertical");
	public static final ResourceLocation OPEN_EYE_TEXTURE = new ResourceLocation("entity/conduit/open_eye");
	public static final ResourceLocation CLOSED_EYE_TEXTURE = new ResourceLocation("entity/conduit/closed_eye");
	private final ModelPart eye = new ModelPart(16, 16, 0, 0);
	private final ModelPart wind;
	private final ModelPart shell;
	private final ModelPart cage;

	public ConduitRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
		this.eye.addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, 0.01F);
		this.wind = new ModelPart(64, 32, 0, 0);
		this.wind.addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F);
		this.shell = new ModelPart(32, 16, 0, 0);
		this.shell.addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F);
		this.cage = new ModelPart(32, 16, 0, 0);
		this.cage.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
	}

	public void render(
		ConduitBlockEntity conduitBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j
	) {
		float h = (float)conduitBlockEntity.tickCount + g;
		if (!conduitBlockEntity.isActive()) {
			float k = conduitBlockEntity.getActiveRotation(0.0F);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
			poseStack.pushPose();
			poseStack.translate(0.5, 0.5, 0.5);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(k));
			this.shell.render(poseStack, vertexConsumer, 0.0625F, i, j, this.getSprite(SHELL_TEXTURE));
			poseStack.popPose();
		} else {
			VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
			float l = conduitBlockEntity.getActiveRotation(g) * (180.0F / (float)Math.PI);
			float m = Mth.sin(h * 0.1F) / 2.0F + 0.5F;
			m = m * m + m;
			poseStack.pushPose();
			poseStack.translate(0.5, (double)(0.3F + m * 0.2F), 0.5);
			Vector3f vector3f = new Vector3f(0.5F, 1.0F, 0.5F);
			vector3f.normalize();
			poseStack.mulPose(new Quaternion(vector3f, l, true));
			this.cage.render(poseStack, vertexConsumer2, 0.0625F, i, j, this.getSprite(ACTIVE_SHELL_TEXTURE));
			poseStack.popPose();
			int n = conduitBlockEntity.tickCount / 66 % 3;
			poseStack.pushPose();
			poseStack.translate(0.5, 0.5, 0.5);
			if (n == 1) {
				poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
			} else if (n == 2) {
				poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
			}

			TextureAtlasSprite textureAtlasSprite = this.getSprite(n == 1 ? VERTICAL_WIND_TEXTURE : WIND_TEXTURE);
			this.wind.render(poseStack, vertexConsumer2, 0.0625F, i, j, textureAtlasSprite);
			poseStack.popPose();
			poseStack.pushPose();
			poseStack.translate(0.5, 0.5, 0.5);
			poseStack.scale(0.875F, 0.875F, 0.875F);
			poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
			this.wind.render(poseStack, vertexConsumer2, 0.0625F, i, j, textureAtlasSprite);
			poseStack.popPose();
			Camera camera = this.renderer.camera;
			poseStack.pushPose();
			poseStack.translate(0.5, (double)(0.3F + m * 0.2F), 0.5);
			poseStack.scale(0.5F, 0.5F, 0.5F);
			float o = -camera.getYRot();
			poseStack.mulPose(Vector3f.YP.rotationDegrees(o));
			poseStack.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
			this.eye.render(poseStack, vertexConsumer2, 0.083333336F, i, j, this.getSprite(conduitBlockEntity.isHunting() ? OPEN_EYE_TEXTURE : CLOSED_EYE_TEXTURE));
			poseStack.popPose();
		}
	}
}
