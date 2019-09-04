package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.renderer.entity.layers.PhantomEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Phantom;

@Environment(EnvType.CLIENT)
public class PhantomRenderer extends MobRenderer<Phantom, PhantomModel<Phantom>> {
	private static final ResourceLocation PHANTOM_LOCATION = new ResourceLocation("textures/entity/phantom.png");

	public PhantomRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new PhantomModel<>(), 0.75F);
		this.addLayer(new PhantomEyesLayer<>(this));
	}

	protected ResourceLocation getTextureLocation(Phantom phantom) {
		return PHANTOM_LOCATION;
	}

	protected void scale(Phantom phantom, float f) {
		int i = phantom.getPhantomSize();
		float g = 1.0F + 0.15F * (float)i;
		RenderSystem.scalef(g, g, g);
		RenderSystem.translatef(0.0F, 1.3125F, 0.1875F);
	}

	protected void setupRotations(Phantom phantom, float f, float g, float h) {
		super.setupRotations(phantom, f, g, h);
		RenderSystem.rotatef(phantom.xRot, 1.0F, 0.0F, 0.0F);
	}
}
