package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AllayModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.allay.Allay;

@Environment(EnvType.CLIENT)
public class AllayRenderer extends MobRenderer<Allay, AllayModel> {
	private static final ResourceLocation ALLAY_TEXTURE = new ResourceLocation("textures/entity/allay/allay.png");
	private static final int BRIGHTNESS_LEVEL_TRANSITION_DURATION = 60;
	private static final int MIN_BRIGHTNESS_LEVEL = 5;

	public AllayRenderer(EntityRendererProvider.Context context) {
		super(context, new AllayModel(context.bakeLayer(ModelLayers.ALLAY)), 0.4F);
		this.addLayer(new ItemInHandLayer<>(this));
	}

	public ResourceLocation getTextureLocation(Allay allay) {
		return ALLAY_TEXTURE;
	}

	protected int getBlockLightLevel(Allay allay, BlockPos blockPos) {
		long l = allay.getLevel().getGameTime() + (long)Math.abs(allay.getUUID().hashCode());
		float f = (float)Math.abs(l % 120L - 60L);
		float g = f / 60.0F;
		return (int)Mth.lerp(g, 5.0F, 15.0F);
	}
}
