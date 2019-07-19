package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
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
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableNormalize();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			this.getParentModel().copyPropertiesTo(this.model);
			this.model.render(entity, f, g, i, j, k, l);
			GlStateManager.disableBlend();
			GlStateManager.disableNormalize();
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return true;
	}
}
