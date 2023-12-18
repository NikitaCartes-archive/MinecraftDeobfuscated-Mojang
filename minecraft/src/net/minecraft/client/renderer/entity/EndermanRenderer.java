package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CarriedBlockLayer;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class EndermanRenderer extends MobRenderer<EnderMan, EndermanModel<EnderMan>> {
	private static final ResourceLocation ENDERMAN_LOCATION = new ResourceLocation("textures/entity/enderman/enderman.png");
	private final RandomSource random = RandomSource.create();

	public EndermanRenderer(EntityRendererProvider.Context context) {
		super(context, new EndermanModel<>(context.bakeLayer(ModelLayers.ENDERMAN)), 0.5F);
		this.addLayer(new EnderEyesLayer<>(this));
		this.addLayer(new CarriedBlockLayer(this, context.getBlockRenderDispatcher()));
	}

	public void render(EnderMan enderMan, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		BlockState blockState = enderMan.getCarriedBlock();
		EndermanModel<EnderMan> endermanModel = this.getModel();
		endermanModel.carrying = blockState != null;
		endermanModel.creepy = enderMan.isCreepy();
		super.render(enderMan, f, g, poseStack, multiBufferSource, i);
	}

	public Vec3 getRenderOffset(EnderMan enderMan, float f) {
		if (enderMan.isCreepy()) {
			double d = 0.02 * (double)enderMan.getScale();
			return new Vec3(this.random.nextGaussian() * d, 0.0, this.random.nextGaussian() * d);
		} else {
			return super.getRenderOffset(enderMan, f);
		}
	}

	public ResourceLocation getTextureLocation(EnderMan enderMan) {
		return ENDERMAN_LOCATION;
	}
}
