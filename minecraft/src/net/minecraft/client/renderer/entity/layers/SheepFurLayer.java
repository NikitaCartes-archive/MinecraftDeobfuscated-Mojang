package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;

@Environment(EnvType.CLIENT)
public class SheepFurLayer extends RenderLayer<Sheep, SheepModel<Sheep>> {
	private static final ResourceLocation SHEEP_FUR_LOCATION = new ResourceLocation("textures/entity/sheep/sheep_fur.png");
	private final SheepFurModel<Sheep> model = new SheepFurModel<>();

	public SheepFurLayer(RenderLayerParent<Sheep, SheepModel<Sheep>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(Sheep sheep, float f, float g, float h, float i, float j, float k, float l) {
		if (!sheep.isSheared() && !sheep.isInvisible()) {
			this.bindTexture(SHEEP_FUR_LOCATION);
			if (sheep.hasCustomName() && "jeb_".equals(sheep.getName().getContents())) {
				int m = 25;
				int n = sheep.tickCount / 25 + sheep.getId();
				int o = DyeColor.values().length;
				int p = n % o;
				int q = (n + 1) % o;
				float r = ((float)(sheep.tickCount % 25) + h) / 25.0F;
				float[] fs = Sheep.getColorArray(DyeColor.byId(p));
				float[] gs = Sheep.getColorArray(DyeColor.byId(q));
				RenderSystem.color3f(fs[0] * (1.0F - r) + gs[0] * r, fs[1] * (1.0F - r) + gs[1] * r, fs[2] * (1.0F - r) + gs[2] * r);
			} else {
				float[] hs = Sheep.getColorArray(sheep.getColor());
				RenderSystem.color3f(hs[0], hs[1], hs[2]);
			}

			this.getParentModel().copyPropertiesTo(this.model);
			this.model.prepareMobModel(sheep, f, g, h);
			this.model.render(sheep, f, g, i, j, k, l);
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return true;
	}
}
