package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CodModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cod;

@Environment(EnvType.CLIENT)
public class CodRenderer extends MobRenderer<Cod, LivingEntityRenderState, CodModel> {
	private static final ResourceLocation COD_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/fish/cod.png");

	public CodRenderer(EntityRendererProvider.Context context) {
		super(context, new CodModel(context.bakeLayer(ModelLayers.COD)), 0.3F);
	}

	@Override
	public ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
		return COD_LOCATION;
	}

	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	@Override
	protected void setupRotations(LivingEntityRenderState livingEntityRenderState, PoseStack poseStack, float f, float g) {
		super.setupRotations(livingEntityRenderState, poseStack, f, g);
		float h = 4.3F * Mth.sin(0.6F * livingEntityRenderState.ageInTicks);
		poseStack.mulPose(Axis.YP.rotationDegrees(h));
		if (!livingEntityRenderState.isInWater) {
			poseStack.translate(0.1F, 0.1F, -0.1F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
		}
	}
}
