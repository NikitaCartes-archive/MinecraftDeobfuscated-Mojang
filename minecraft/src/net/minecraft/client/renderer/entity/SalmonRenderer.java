package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.SalmonRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Salmon;

@Environment(EnvType.CLIENT)
public class SalmonRenderer extends MobRenderer<Salmon, SalmonRenderState, SalmonModel> {
	private static final ResourceLocation SALMON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/fish/salmon.png");
	private final SalmonModel smallSalmonModel;
	private final SalmonModel mediumSalmonModel;
	private final SalmonModel largeSalmonModel;

	public SalmonRenderer(EntityRendererProvider.Context context) {
		super(context, new SalmonModel(context.bakeLayer(ModelLayers.SALMON)), 0.4F);
		this.smallSalmonModel = new SalmonModel(context.bakeLayer(ModelLayers.SALMON_SMALL));
		this.mediumSalmonModel = new SalmonModel(context.bakeLayer(ModelLayers.SALMON));
		this.largeSalmonModel = new SalmonModel(context.bakeLayer(ModelLayers.SALMON_LARGE));
	}

	public void extractRenderState(Salmon salmon, SalmonRenderState salmonRenderState, float f) {
		super.extractRenderState(salmon, salmonRenderState, f);
		salmonRenderState.variant = salmon.getVariant();
	}

	public ResourceLocation getTextureLocation(SalmonRenderState salmonRenderState) {
		return SALMON_LOCATION;
	}

	public SalmonRenderState createRenderState() {
		return new SalmonRenderState();
	}

	protected void setupRotations(SalmonRenderState salmonRenderState, PoseStack poseStack, float f, float g) {
		super.setupRotations(salmonRenderState, poseStack, f, g);
		float h = 1.0F;
		float i = 1.0F;
		if (!salmonRenderState.isInWater) {
			h = 1.3F;
			i = 1.7F;
		}

		float j = h * 4.3F * Mth.sin(i * 0.6F * salmonRenderState.ageInTicks);
		poseStack.mulPose(Axis.YP.rotationDegrees(j));
		if (!salmonRenderState.isInWater) {
			poseStack.translate(0.2F, 0.1F, 0.0F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
		}
	}

	public void render(SalmonRenderState salmonRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		if (salmonRenderState.variant == Salmon.Variant.SMALL) {
			this.model = this.smallSalmonModel;
		} else if (salmonRenderState.variant == Salmon.Variant.LARGE) {
			this.model = this.largeSalmonModel;
		} else {
			this.model = this.mediumSalmonModel;
		}

		super.render(salmonRenderState, poseStack, multiBufferSource, i);
	}
}
