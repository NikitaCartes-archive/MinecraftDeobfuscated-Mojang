package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

@Environment(EnvType.CLIENT)
public class HuskRenderer extends ZombieRenderer {
	private static final ResourceLocation HUSK_LOCATION = new ResourceLocation("textures/entity/zombie/husk.png");

	public HuskRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	protected void scale(Zombie zombie, float f) {
		float g = 1.0625F;
		GlStateManager.scalef(1.0625F, 1.0625F, 1.0625F);
		super.scale(zombie, f);
	}

	@Override
	protected ResourceLocation getTextureLocation(Zombie zombie) {
		return HUSK_LOCATION;
	}
}
