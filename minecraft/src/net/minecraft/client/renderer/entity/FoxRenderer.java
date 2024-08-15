package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.FoxHeldItemLayer;
import net.minecraft.client.renderer.entity.state.FoxRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Fox;

@Environment(EnvType.CLIENT)
public class FoxRenderer extends AgeableMobRenderer<Fox, FoxRenderState, FoxModel> {
	private static final ResourceLocation RED_FOX_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fox/fox.png");
	private static final ResourceLocation RED_FOX_SLEEP_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fox/fox_sleep.png");
	private static final ResourceLocation SNOW_FOX_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fox/snow_fox.png");
	private static final ResourceLocation SNOW_FOX_SLEEP_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fox/snow_fox_sleep.png");

	public FoxRenderer(EntityRendererProvider.Context context) {
		super(context, new FoxModel(context.bakeLayer(ModelLayers.FOX)), new FoxModel(context.bakeLayer(ModelLayers.FOX_BABY)), 0.4F);
		this.addLayer(new FoxHeldItemLayer(this, context.getItemRenderer()));
	}

	protected void setupRotations(FoxRenderState foxRenderState, PoseStack poseStack, float f, float g) {
		super.setupRotations(foxRenderState, poseStack, f, g);
		if (foxRenderState.isPouncing || foxRenderState.isFaceplanted) {
			poseStack.mulPose(Axis.XP.rotationDegrees(-foxRenderState.xRot));
		}
	}

	public ResourceLocation getTextureLocation(FoxRenderState foxRenderState) {
		if (foxRenderState.variant == Fox.Variant.RED) {
			return foxRenderState.isSleeping ? RED_FOX_SLEEP_TEXTURE : RED_FOX_TEXTURE;
		} else {
			return foxRenderState.isSleeping ? SNOW_FOX_SLEEP_TEXTURE : SNOW_FOX_TEXTURE;
		}
	}

	public FoxRenderState createRenderState() {
		return new FoxRenderState();
	}

	public void extractRenderState(Fox fox, FoxRenderState foxRenderState, float f) {
		super.extractRenderState(fox, foxRenderState, f);
		foxRenderState.headRollAngle = fox.getHeadRollAngle(f);
		foxRenderState.isCrouching = fox.isCrouching();
		foxRenderState.crouchAmount = fox.getCrouchAmount(f);
		foxRenderState.isSleeping = fox.isSleeping();
		foxRenderState.isSitting = fox.isSitting();
		foxRenderState.isFaceplanted = fox.isFaceplanted();
		foxRenderState.isPouncing = fox.isPouncing();
		foxRenderState.variant = fox.getVariant();
	}
}
