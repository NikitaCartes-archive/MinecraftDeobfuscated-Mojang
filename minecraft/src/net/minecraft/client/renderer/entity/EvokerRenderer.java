package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.SpellcasterIllager;

@Environment(EnvType.CLIENT)
public class EvokerRenderer<T extends SpellcasterIllager> extends IllagerRenderer<T> {
	private static final ResourceLocation EVOKER_ILLAGER = new ResourceLocation("textures/entity/illager/evoker.png");

	public EvokerRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new IllagerModel<>(0.0F, 0.0F, 64, 64), 0.5F);
		this.addLayer(new ItemInHandLayer<T, IllagerModel<T>>(this) {
			public void render(T spellcasterIllager, float f, float g, float h, float i, float j, float k, float l) {
				if (spellcasterIllager.isCastingSpell()) {
					super.render(spellcasterIllager, f, g, h, i, j, k, l);
				}
			}
		});
	}

	protected ResourceLocation getTextureLocation(T spellcasterIllager) {
		return EVOKER_ILLAGER;
	}
}
