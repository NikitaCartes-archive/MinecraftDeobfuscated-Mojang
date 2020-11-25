package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ambient.Bat;

@Environment(EnvType.CLIENT)
public class BatRenderer extends MobRenderer<Bat, BatModel> {
	private static final ResourceLocation BAT_LOCATION = new ResourceLocation("textures/entity/bat.png");

	public BatRenderer(EntityRendererProvider.Context context) {
		super(context, new BatModel(context.bakeLayer(ModelLayers.BAT)), 0.25F);
	}

	public ResourceLocation getTextureLocation(Bat bat) {
		return BAT_LOCATION;
	}

	protected void scale(Bat bat, PoseStack poseStack, float f) {
		poseStack.scale(0.35F, 0.35F, 0.35F);
	}

	protected void setupRotations(Bat bat, PoseStack poseStack, float f, float g, float h) {
		if (bat.isResting()) {
			poseStack.translate(0.0, -0.1F, 0.0);
		} else {
			poseStack.translate(0.0, (double)(Mth.cos(f * 0.3F) * 0.1F), 0.0);
		}

		super.setupRotations(bat, poseStack, f, g, h);
	}
}
