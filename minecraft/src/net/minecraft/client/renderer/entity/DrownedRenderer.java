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
	private static final ResourceLocation DROWNED_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/drowned.png");

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

	protected void setupRotations(Drowned drowned, PoseStack poseStack, float f, float g, float h, float i) {
		super.setupRotations(drowned, poseStack, f, g, h, i);
		float j = drowned.getSwimAmount(h);
		if (j > 0.0F) {
			float k = -10.0F - drowned.getXRot();
			float l = Mth.lerp(j, 0.0F, k);
			poseStack.rotateAround(Axis.XP.rotationDegrees(l), 0.0F, drowned.getBbHeight() / 2.0F / i, 0.0F);
		}
	}
}
