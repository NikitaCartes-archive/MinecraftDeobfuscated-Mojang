package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.TurtleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.TurtleRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Turtle;

@Environment(EnvType.CLIENT)
public class TurtleRenderer extends AgeableMobRenderer<Turtle, TurtleRenderState, TurtleModel> {
	private static final ResourceLocation TURTLE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/turtle/big_sea_turtle.png");

	public TurtleRenderer(EntityRendererProvider.Context context) {
		super(context, new TurtleModel(context.bakeLayer(ModelLayers.TURTLE)), new TurtleModel(context.bakeLayer(ModelLayers.TURTLE_BABY)), 0.7F);
	}

	protected float getShadowRadius(TurtleRenderState turtleRenderState) {
		float f = super.getShadowRadius(turtleRenderState);
		return turtleRenderState.isBaby ? f * 0.83F : f;
	}

	public TurtleRenderState createRenderState() {
		return new TurtleRenderState();
	}

	public void extractRenderState(Turtle turtle, TurtleRenderState turtleRenderState, float f) {
		super.extractRenderState(turtle, turtleRenderState, f);
		turtleRenderState.isOnLand = !turtle.isInWater() && turtle.onGround();
		turtleRenderState.isLayingEgg = turtle.isLayingEgg();
		turtleRenderState.hasEgg = !turtle.isBaby() && turtle.hasEgg();
	}

	public ResourceLocation getTextureLocation(TurtleRenderState turtleRenderState) {
		return TURTLE_LOCATION;
	}
}
