package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class EndCrystalRenderer extends EntityRenderer<EndCrystal> {
	private static final ResourceLocation END_CRYSTAL_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal.png");
	private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION);
	private static final float SIN_45 = (float)Math.sin(Math.PI / 4);
	private static final String GLASS = "glass";
	private static final String BASE = "base";
	private final ModelPart cube;
	private final ModelPart glass;
	private final ModelPart base;

	public EndCrystalRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.5F;
		ModelPart modelPart = context.bakeLayer(ModelLayers.END_CRYSTAL);
		this.glass = modelPart.getChild("glass");
		this.cube = modelPart.getChild("cube");
		this.base = modelPart.getChild("base");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 16).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void render(EndCrystal endCrystal, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		float h = getY(endCrystal, g);
		float j = ((float)endCrystal.time + g) * 3.0F;
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
		poseStack.pushPose();
		poseStack.scale(2.0F, 2.0F, 2.0F);
		poseStack.translate(0.0F, -0.5F, 0.0F);
		int k = OverlayTexture.NO_OVERLAY;
		if (endCrystal.showsBottom()) {
			this.base.render(poseStack, vertexConsumer, i, k);
		}

		poseStack.mulPose(Axis.YP.rotationDegrees(j));
		poseStack.translate(0.0F, 1.5F + h / 2.0F, 0.0F);
		poseStack.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45));
		this.glass.render(poseStack, vertexConsumer, i, k);
		float l = 0.875F;
		poseStack.scale(0.875F, 0.875F, 0.875F);
		poseStack.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45));
		poseStack.mulPose(Axis.YP.rotationDegrees(j));
		this.glass.render(poseStack, vertexConsumer, i, k);
		poseStack.scale(0.875F, 0.875F, 0.875F);
		poseStack.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45));
		poseStack.mulPose(Axis.YP.rotationDegrees(j));
		this.cube.render(poseStack, vertexConsumer, i, k);
		poseStack.popPose();
		poseStack.popPose();
		BlockPos blockPos = endCrystal.getBeamTarget();
		if (blockPos != null) {
			float m = (float)blockPos.getX() + 0.5F;
			float n = (float)blockPos.getY() + 0.5F;
			float o = (float)blockPos.getZ() + 0.5F;
			float p = (float)((double)m - endCrystal.getX());
			float q = (float)((double)n - endCrystal.getY());
			float r = (float)((double)o - endCrystal.getZ());
			poseStack.translate(p, q, r);
			EnderDragonRenderer.renderCrystalBeams(-p, -q + h, -r, g, endCrystal.time, poseStack, multiBufferSource, i);
		}

		super.render(endCrystal, f, g, poseStack, multiBufferSource, i);
	}

	public static float getY(EndCrystal endCrystal, float f) {
		float g = (float)endCrystal.time + f;
		float h = Mth.sin(g * 0.2F) / 2.0F + 0.5F;
		h = (h * h + h) * 0.4F;
		return h - 1.4F;
	}

	public ResourceLocation getTextureLocation(EndCrystal endCrystal) {
		return END_CRYSTAL_LOCATION;
	}

	public boolean shouldRender(EndCrystal endCrystal, Frustum frustum, double d, double e, double f) {
		return super.shouldRender(endCrystal, frustum, d, e, f) || endCrystal.getBeamTarget() != null;
	}
}
