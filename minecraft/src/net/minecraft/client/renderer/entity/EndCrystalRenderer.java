package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EndCrystalModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class EndCrystalRenderer extends EntityRenderer<EndCrystal, EndCrystalRenderState> {
	private static final ResourceLocation END_CRYSTAL_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/end_crystal/end_crystal.png");
	private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION);
	private final EndCrystalModel model;

	public EndCrystalRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.5F;
		this.model = new EndCrystalModel(context.bakeLayer(ModelLayers.END_CRYSTAL));
	}

	public void render(EndCrystalRenderState endCrystalRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.scale(2.0F, 2.0F, 2.0F);
		poseStack.translate(0.0F, -0.5F, 0.0F);
		this.model.setupAnim(endCrystalRenderState);
		this.model.renderToBuffer(poseStack, multiBufferSource.getBuffer(RENDER_TYPE), i, OverlayTexture.NO_OVERLAY);
		poseStack.popPose();
		Vec3 vec3 = endCrystalRenderState.beamOffset;
		if (vec3 != null) {
			float f = getY(endCrystalRenderState.ageInTicks);
			float g = (float)vec3.x;
			float h = (float)vec3.y;
			float j = (float)vec3.z;
			poseStack.translate(vec3);
			EnderDragonRenderer.renderCrystalBeams(-g, -h + f, -j, endCrystalRenderState.ageInTicks, poseStack, multiBufferSource, i);
		}

		super.render(endCrystalRenderState, poseStack, multiBufferSource, i);
	}

	public static float getY(float f) {
		float g = Mth.sin(f * 0.2F) / 2.0F + 0.5F;
		g = (g * g + g) * 0.4F;
		return g - 1.4F;
	}

	public EndCrystalRenderState createRenderState() {
		return new EndCrystalRenderState();
	}

	public void extractRenderState(EndCrystal endCrystal, EndCrystalRenderState endCrystalRenderState, float f) {
		super.extractRenderState(endCrystal, endCrystalRenderState, f);
		endCrystalRenderState.ageInTicks = (float)endCrystal.time + f;
		endCrystalRenderState.showsBottom = endCrystal.showsBottom();
		BlockPos blockPos = endCrystal.getBeamTarget();
		if (blockPos != null) {
			endCrystalRenderState.beamOffset = Vec3.atCenterOf(blockPos).subtract(endCrystal.getPosition(f));
		} else {
			endCrystalRenderState.beamOffset = null;
		}
	}

	public boolean shouldRender(EndCrystal endCrystal, Frustum frustum, double d, double e, double f) {
		return super.shouldRender(endCrystal, frustum, d, e, f) || endCrystal.getBeamTarget() != null;
	}
}
