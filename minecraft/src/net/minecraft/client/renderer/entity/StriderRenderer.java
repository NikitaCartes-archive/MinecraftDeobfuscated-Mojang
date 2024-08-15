package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.StriderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.client.renderer.entity.state.StriderRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Strider;

@Environment(EnvType.CLIENT)
public class StriderRenderer extends MobRenderer<Strider, StriderRenderState, StriderModel> {
	private static final ResourceLocation STRIDER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/strider/strider.png");
	private static final ResourceLocation COLD_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/strider/strider_cold.png");
	private static final float SHADOW_RADIUS = 0.5F;

	public StriderRenderer(EntityRendererProvider.Context context) {
		super(context, new StriderModel(context.bakeLayer(ModelLayers.STRIDER)), 0.5F);
		this.addLayer(
			new SaddleLayer<>(
				this, new StriderModel(context.bakeLayer(ModelLayers.STRIDER_SADDLE)), ResourceLocation.withDefaultNamespace("textures/entity/strider/strider_saddle.png")
			)
		);
	}

	public ResourceLocation getTextureLocation(StriderRenderState striderRenderState) {
		return striderRenderState.isSuffocating ? COLD_LOCATION : STRIDER_LOCATION;
	}

	protected float getShadowRadius(StriderRenderState striderRenderState) {
		float f = super.getShadowRadius(striderRenderState);
		return striderRenderState.isBaby ? f * 0.5F : f;
	}

	public StriderRenderState createRenderState() {
		return new StriderRenderState();
	}

	public void extractRenderState(Strider strider, StriderRenderState striderRenderState, float f) {
		super.extractRenderState(strider, striderRenderState, f);
		striderRenderState.isSaddled = strider.isSaddled();
		striderRenderState.isSuffocating = strider.isSuffocating();
		striderRenderState.isRidden = strider.isVehicle();
	}

	protected void scale(StriderRenderState striderRenderState, PoseStack poseStack) {
		float f = striderRenderState.ageScale;
		poseStack.scale(f, f, f);
	}

	protected boolean isShaking(StriderRenderState striderRenderState) {
		return super.isShaking(striderRenderState) || striderRenderState.isSuffocating;
	}
}
