package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;

@Environment(EnvType.CLIENT)
public class ParrotRenderer extends MobRenderer<Parrot, ParrotRenderState, ParrotModel> {
	private static final ResourceLocation RED_BLUE = ResourceLocation.withDefaultNamespace("textures/entity/parrot/parrot_red_blue.png");
	private static final ResourceLocation BLUE = ResourceLocation.withDefaultNamespace("textures/entity/parrot/parrot_blue.png");
	private static final ResourceLocation GREEN = ResourceLocation.withDefaultNamespace("textures/entity/parrot/parrot_green.png");
	private static final ResourceLocation YELLOW_BLUE = ResourceLocation.withDefaultNamespace("textures/entity/parrot/parrot_yellow_blue.png");
	private static final ResourceLocation GREY = ResourceLocation.withDefaultNamespace("textures/entity/parrot/parrot_grey.png");

	public ParrotRenderer(EntityRendererProvider.Context context) {
		super(context, new ParrotModel(context.bakeLayer(ModelLayers.PARROT)), 0.3F);
	}

	public ResourceLocation getTextureLocation(ParrotRenderState parrotRenderState) {
		return getVariantTexture(parrotRenderState.variant);
	}

	public ParrotRenderState createRenderState() {
		return new ParrotRenderState();
	}

	public void extractRenderState(Parrot parrot, ParrotRenderState parrotRenderState, float f) {
		super.extractRenderState(parrot, parrotRenderState, f);
		parrotRenderState.variant = parrot.getVariant();
		float g = Mth.lerp(f, parrot.oFlap, parrot.flap);
		float h = Mth.lerp(f, parrot.oFlapSpeed, parrot.flapSpeed);
		parrotRenderState.flapAngle = (Mth.sin(g) + 1.0F) * h;
		parrotRenderState.pose = ParrotModel.getPose(parrot);
	}

	public static ResourceLocation getVariantTexture(Parrot.Variant variant) {
		return switch (variant) {
			case RED_BLUE -> RED_BLUE;
			case BLUE -> BLUE;
			case GREEN -> GREEN;
			case YELLOW_BLUE -> YELLOW_BLUE;
			case GRAY -> GREY;
		};
	}
}
