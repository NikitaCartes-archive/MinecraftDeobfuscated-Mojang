package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vindicator;

@Environment(EnvType.CLIENT)
public class VindicatorRenderer extends IllagerRenderer<Vindicator> {
	private static final ResourceLocation VINDICATOR = new ResourceLocation("textures/entity/illager/vindicator.png");

	public VindicatorRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new IllagerModel<>(0.0F, 0.0F, 64, 64), 0.5F);
		this.addLayer(new ItemInHandLayer<Vindicator, IllagerModel<Vindicator>>(this) {
			public void render(Vindicator vindicator, float f, float g, float h, float i, float j, float k, float l) {
				if (vindicator.isAggressive()) {
					super.render(vindicator, f, g, h, i, j, k, l);
				}
			}
		});
	}

	protected ResourceLocation getTextureLocation(Vindicator vindicator) {
		return VINDICATOR;
	}
}
