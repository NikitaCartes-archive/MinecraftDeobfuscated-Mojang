package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.MoonCow;

@Environment(EnvType.CLIENT)
public class MoonCowRenderer extends MobRenderer<MoonCow, CowModel<MoonCow>> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/cow/moon_cow.png");

	public MoonCowRenderer(EntityRendererProvider.Context context) {
		super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), 0.7F);
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
	}

	public ResourceLocation getTextureLocation(MoonCow moonCow) {
		return TEXTURE;
	}

	protected void scale(MoonCow moonCow, PoseStack poseStack, float f) {
		float g = moonCow.getBloatScale();
		poseStack.scale(g, g, g);
	}
}
