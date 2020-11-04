package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CodModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cod;

@Environment(EnvType.CLIENT)
public class CodRenderer extends MobRenderer<Cod, CodModel<Cod>> {
	private static final ResourceLocation COD_LOCATION = new ResourceLocation("textures/entity/fish/cod.png");

	public CodRenderer(EntityRendererProvider.Context context) {
		super(context, new CodModel<>(context.getLayer(ModelLayers.COD)), 0.3F);
	}

	public ResourceLocation getTextureLocation(Cod cod) {
		return COD_LOCATION;
	}

	protected void setupRotations(Cod cod, PoseStack poseStack, float f, float g, float h) {
		super.setupRotations(cod, poseStack, f, g, h);
		float i = 4.3F * Mth.sin(0.6F * f);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(i));
		if (!cod.isInWater()) {
			poseStack.translate(0.1F, 0.1F, -0.1F);
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
		}
	}
}
