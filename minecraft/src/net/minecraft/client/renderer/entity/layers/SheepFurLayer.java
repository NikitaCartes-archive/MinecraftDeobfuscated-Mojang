package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.renderer.MultiBufferSource;
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

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Sheep sheep, float f, float g, float h, float j, float k, float l, float m) {
		if (!sheep.isSheared() && !sheep.isInvisible()) {
			float t;
			float u;
			float v;
			if (sheep.hasCustomName() && "jeb_".equals(sheep.getName().getContents())) {
				int n = 25;
				int o = sheep.tickCount / 25 + sheep.getId();
				int p = DyeColor.values().length;
				int q = o % p;
				int r = (o + 1) % p;
				float s = ((float)(sheep.tickCount % 25) + h) / 25.0F;
				float[] fs = Sheep.getColorArray(DyeColor.byId(q));
				float[] gs = Sheep.getColorArray(DyeColor.byId(r));
				t = fs[0] * (1.0F - s) + gs[0] * s;
				u = fs[1] * (1.0F - s) + gs[1] * s;
				v = fs[2] * (1.0F - s) + gs[2] * s;
			} else {
				float[] hs = Sheep.getColorArray(sheep.getColor());
				t = hs[0];
				u = hs[1];
				v = hs[2];
			}

			coloredModelCopyLayerRender(this.getParentModel(), this.model, SHEEP_FUR_LOCATION, poseStack, multiBufferSource, i, sheep, f, g, j, k, l, m, h, t, u, v);
		}
	}
}
