package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

@Environment(EnvType.CLIENT)
public class HuskRenderer extends ZombieRenderer {
	private static final ResourceLocation HUSK_LOCATION = new ResourceLocation("textures/entity/zombie/husk.png");

	public HuskRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.HUSK, ModelLayers.HUSK_INNER_ARMOR, ModelLayers.HUSK_OUTER_ARMOR);
	}

	protected void scale(Zombie zombie, PoseStack poseStack, float f) {
		float g = 1.0625F;
		poseStack.scale(1.0625F, 1.0625F, 1.0625F);
		super.scale(zombie, poseStack, f);
	}

	@Override
	public ResourceLocation getTextureLocation(Zombie zombie) {
		return HUSK_LOCATION;
	}
}
