package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;

@Environment(EnvType.CLIENT)
public class DrownedRenderer extends AbstractZombieRenderer<Drowned, DrownedModel<Drowned>> {
	private static final ResourceLocation DROWNED_LOCATION = new ResourceLocation("textures/entity/zombie/drowned.png");

	public DrownedRenderer(EntityRendererProvider.Context context) {
		super(
			context,
			new DrownedModel<>(context.bakeLayer(ModelLayers.DROWNED)),
			new DrownedModel<>(context.bakeLayer(ModelLayers.DROWNED_INNER_ARMOR)),
			new DrownedModel<>(context.bakeLayer(ModelLayers.DROWNED_OUTER_ARMOR))
		);
		this.addLayer(new DrownedOuterLayer<>(this, context.getModelSet()));
	}

	@Override
	public ResourceLocation getTextureLocation(Zombie zombie) {
		return DROWNED_LOCATION;
	}

	protected void setupRotations(Drowned drowned, PoseStack poseStack, float f, float g, float h) {
		super.setupRotations(drowned, poseStack, f, g, h);
		float i = drowned.getSwimAmount(h);
		if (i > 0.0F) {
			float j = -10.0F - drowned.getXRot();
			float k = Mth.lerp(i, 0.0F, j);
			poseStack.rotateAround(Axis.XP.rotationDegrees(k), 0.0F, drowned.getBbHeight() / 2.0F, 0.0F);
		}
	}
}
