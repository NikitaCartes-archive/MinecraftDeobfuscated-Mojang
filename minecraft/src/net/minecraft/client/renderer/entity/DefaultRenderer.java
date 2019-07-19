package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class DefaultRenderer extends EntityRenderer<Entity> {
	public DefaultRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	@Override
	public void render(Entity entity, double d, double e, double f, float g, float h) {
		GlStateManager.pushMatrix();
		render(entity.getBoundingBox(), d - entity.xOld, e - entity.yOld, f - entity.zOld);
		GlStateManager.popMatrix();
		super.render(entity, d, e, f, g, h);
	}

	@Nullable
	@Override
	protected ResourceLocation getTextureLocation(Entity entity) {
		return null;
	}
}
