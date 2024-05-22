package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;

@Environment(EnvType.CLIENT)
public class SheepFurLayer extends RenderLayer<Sheep, SheepModel<Sheep>> {
	private static final ResourceLocation SHEEP_FUR_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/sheep/sheep_fur.png");
	private final SheepFurModel<Sheep> model;

	public SheepFurLayer(RenderLayerParent<Sheep, SheepModel<Sheep>> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new SheepFurModel<>(entityModelSet.bakeLayer(ModelLayers.SHEEP_FUR));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Sheep sheep, float f, float g, float h, float j, float k, float l) {
		if (!sheep.isSheared()) {
			if (sheep.isInvisible()) {
				Minecraft minecraft = Minecraft.getInstance();
				boolean bl = minecraft.shouldEntityAppearGlowing(sheep);
				if (bl) {
					this.getParentModel().copyPropertiesTo(this.model);
					this.model.prepareMobModel(sheep, f, g, h);
					this.model.setupAnim(sheep, f, g, j, k, l);
					VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.outline(SHEEP_FUR_LOCATION));
					this.model.renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(sheep, 0.0F), -16777216);
				}
			} else {
				int u;
				if (sheep.hasCustomName() && "jeb_".equals(sheep.getName().getString())) {
					int m = 25;
					int n = sheep.tickCount / 25 + sheep.getId();
					int o = DyeColor.values().length;
					int p = n % o;
					int q = (n + 1) % o;
					float r = ((float)(sheep.tickCount % 25) + h) / 25.0F;
					int s = Sheep.getColor(DyeColor.byId(p));
					int t = Sheep.getColor(DyeColor.byId(q));
					u = FastColor.ARGB32.lerp(r, s, t);
				} else {
					u = Sheep.getColor(sheep.getColor());
				}

				coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, SHEEP_FUR_LOCATION, poseStack, multiBufferSource, i, sheep, f, g, j, k, l, h, u);
			}
		}
	}
}
