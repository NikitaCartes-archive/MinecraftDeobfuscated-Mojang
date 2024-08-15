package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.IronGolemCrackinessLayer;
import net.minecraft.client.renderer.entity.layers.IronGolemFlowerLayer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;

@Environment(EnvType.CLIENT)
public class IronGolemRenderer extends MobRenderer<IronGolem, IronGolemRenderState, IronGolemModel> {
	private static final ResourceLocation GOLEM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem.png");

	public IronGolemRenderer(EntityRendererProvider.Context context) {
		super(context, new IronGolemModel(context.bakeLayer(ModelLayers.IRON_GOLEM)), 0.7F);
		this.addLayer(new IronGolemCrackinessLayer(this));
		this.addLayer(new IronGolemFlowerLayer(this, context.getBlockRenderDispatcher()));
	}

	public ResourceLocation getTextureLocation(IronGolemRenderState ironGolemRenderState) {
		return GOLEM_LOCATION;
	}

	public IronGolemRenderState createRenderState() {
		return new IronGolemRenderState();
	}

	public void extractRenderState(IronGolem ironGolem, IronGolemRenderState ironGolemRenderState, float f) {
		super.extractRenderState(ironGolem, ironGolemRenderState, f);
		ironGolemRenderState.attackTicksRemaining = (float)ironGolem.getAttackAnimationTick() > 0.0F ? (float)ironGolem.getAttackAnimationTick() - f : 0.0F;
		ironGolemRenderState.offerFlowerTick = ironGolem.getOfferFlowerTick();
		ironGolemRenderState.crackiness = ironGolem.getCrackiness();
	}

	protected void setupRotations(IronGolemRenderState ironGolemRenderState, PoseStack poseStack, float f, float g) {
		super.setupRotations(ironGolemRenderState, poseStack, f, g);
		if (!((double)ironGolemRenderState.walkAnimationSpeed < 0.01)) {
			float h = 13.0F;
			float i = ironGolemRenderState.walkAnimationPos + 6.0F;
			float j = (Math.abs(i % 13.0F - 6.5F) - 3.25F) / 3.25F;
			poseStack.mulPose(Axis.ZP.rotationDegrees(6.5F * j));
		}
	}
}
