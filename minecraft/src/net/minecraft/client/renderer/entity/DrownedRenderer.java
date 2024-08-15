package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Drowned;

@Environment(EnvType.CLIENT)
public class DrownedRenderer extends AbstractZombieRenderer<Drowned, ZombieRenderState, DrownedModel> {
	private static final ResourceLocation DROWNED_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/drowned.png");

	public DrownedRenderer(EntityRendererProvider.Context context) {
		super(
			context,
			new DrownedModel(context.bakeLayer(ModelLayers.DROWNED)),
			new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_BABY)),
			new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_INNER_ARMOR)),
			new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_OUTER_ARMOR)),
			new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_BABY_INNER_ARMOR)),
			new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_BABY_OUTER_ARMOR))
		);
		this.addLayer(new DrownedOuterLayer(this, context.getModelSet()));
	}

	public ZombieRenderState createRenderState() {
		return new ZombieRenderState();
	}

	@Override
	public ResourceLocation getTextureLocation(ZombieRenderState zombieRenderState) {
		return DROWNED_LOCATION;
	}

	protected void setupRotations(ZombieRenderState zombieRenderState, PoseStack poseStack, float f, float g) {
		super.setupRotations(zombieRenderState, poseStack, f, g);
		float h = zombieRenderState.swimAmount;
		if (h > 0.0F) {
			float i = -10.0F - zombieRenderState.xRot;
			float j = Mth.lerp(h, 0.0F, i);
			poseStack.rotateAround(Axis.XP.rotationDegrees(j), 0.0F, zombieRenderState.boundingBoxHeight / 2.0F / g, 0.0F);
		}
	}
}
