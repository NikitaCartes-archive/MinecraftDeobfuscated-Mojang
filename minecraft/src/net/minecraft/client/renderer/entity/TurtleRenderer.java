package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.TurtleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Turtle;

@Environment(EnvType.CLIENT)
public class TurtleRenderer extends MobRenderer<Turtle, TurtleModel<Turtle>> {
	private static final ResourceLocation TURTLE_LOCATION = new ResourceLocation("textures/entity/turtle/big_sea_turtle.png");

	public TurtleRenderer(EntityRendererProvider.Context context) {
		super(context, new TurtleModel<>(context.bakeLayer(ModelLayers.TURTLE)), 0.7F);
	}

	public void render(Turtle turtle, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		if (turtle.isBaby()) {
			this.shadowRadius *= 0.5F;
		}

		super.render(turtle, f, g, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(Turtle turtle) {
		return TURTLE_LOCATION;
	}
}
