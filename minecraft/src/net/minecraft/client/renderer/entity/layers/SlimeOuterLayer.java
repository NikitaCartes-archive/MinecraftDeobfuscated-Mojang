package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class SlimeOuterLayer<T extends Entity> extends RenderLayer<T, SlimeModel<T>> {
	private final EntityModel<T> model = new SlimeModel<>(0);

	public SlimeOuterLayer(RenderLayerParent<T, SlimeModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	@Override
	public void render(T entity, float f, float g, float h, float i, float j, float k, float l) {
		if (!entity.isInvisible()) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableNormalize();
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			this.getParentModel().copyPropertiesTo(this.model);
			this.model.render(entity, f, g, i, j, k, l);
			RenderSystem.disableBlend();
			RenderSystem.disableNormalize();
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return true;
	}
}
