package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;

@Environment(EnvType.CLIENT)
public class ThrownItemRenderer<T extends Entity & ItemSupplier> extends EntityRenderer<T> {
	private final ItemRenderer itemRenderer;
	private final float scale;

	public ThrownItemRenderer(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer, float f) {
		super(entityRenderDispatcher);
		this.itemRenderer = itemRenderer;
		this.scale = f;
	}

	public ThrownItemRenderer(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer) {
		this(entityRenderDispatcher, itemRenderer, 1.0F);
	}

	@Override
	public void render(T entity, double d, double e, double f, float g, float h) {
		RenderSystem.pushMatrix();
		RenderSystem.translatef((float)d, (float)e, (float)f);
		RenderSystem.enableRescaleNormal();
		RenderSystem.scalef(this.scale, this.scale, this.scale);
		RenderSystem.rotatef(-this.entityRenderDispatcher.playerRotY, 0.0F, 1.0F, 0.0F);
		RenderSystem.rotatef((float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * this.entityRenderDispatcher.playerRotX, 1.0F, 0.0F, 0.0F);
		RenderSystem.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
		this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
		if (this.solidRender) {
			RenderSystem.enableColorMaterial();
			RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
		}

		this.itemRenderer.renderStatic(entity.getItem(), ItemTransforms.TransformType.GROUND);
		if (this.solidRender) {
			RenderSystem.tearDownSolidRenderingTextureCombine();
			RenderSystem.disableColorMaterial();
		}

		RenderSystem.disableRescaleNormal();
		RenderSystem.popMatrix();
		super.render(entity, d, e, f, g, h);
	}

	@Override
	protected ResourceLocation getTextureLocation(Entity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
