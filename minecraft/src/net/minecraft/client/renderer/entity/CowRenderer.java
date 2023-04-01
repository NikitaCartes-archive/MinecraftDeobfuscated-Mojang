package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;

@Environment(EnvType.CLIENT)
public class CowRenderer extends MobRenderer<Cow, CowModel<Cow>> {
	private static final ResourceLocation COW_LOCATION = new ResourceLocation("textures/entity/cow/cow.png");

	public CowRenderer(EntityRendererProvider.Context context) {
		super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), 0.7F);
	}

	public ResourceLocation getTextureLocation(Cow cow) {
		return COW_LOCATION;
	}

	protected void scale(Cow cow, PoseStack poseStack, float f) {
		float g = cow.getBloatScale();
		poseStack.scale(g, g, g);
	}
}
