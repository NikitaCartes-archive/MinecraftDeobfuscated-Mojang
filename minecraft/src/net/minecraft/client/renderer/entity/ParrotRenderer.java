package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;

@Environment(EnvType.CLIENT)
public class ParrotRenderer extends MobRenderer<Parrot, ParrotModel> {
	public static final ResourceLocation[] PARROT_LOCATIONS = new ResourceLocation[]{
		new ResourceLocation("textures/entity/parrot/parrot_red_blue.png"),
		new ResourceLocation("textures/entity/parrot/parrot_blue.png"),
		new ResourceLocation("textures/entity/parrot/parrot_green.png"),
		new ResourceLocation("textures/entity/parrot/parrot_yellow_blue.png"),
		new ResourceLocation("textures/entity/parrot/parrot_grey.png")
	};

	public ParrotRenderer(EntityRendererProvider.Context context) {
		super(context, new ParrotModel(context.bakeLayer(ModelLayers.PARROT)), 0.3F);
	}

	public ResourceLocation getTextureLocation(Parrot parrot) {
		return PARROT_LOCATIONS[parrot.getVariant()];
	}

	public float getBob(Parrot parrot, float f) {
		float g = Mth.lerp(f, parrot.oFlap, parrot.flap);
		float h = Mth.lerp(f, parrot.oFlapSpeed, parrot.flapSpeed);
		return (Mth.sin(g) + 1.0F) * h;
	}
}
