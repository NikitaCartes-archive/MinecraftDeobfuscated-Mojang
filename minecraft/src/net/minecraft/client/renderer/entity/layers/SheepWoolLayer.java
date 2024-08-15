package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;

@Environment(EnvType.CLIENT)
public class SheepWoolLayer extends RenderLayer<SheepRenderState, SheepModel> {
	private static final ResourceLocation SHEEP_FUR_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/sheep/sheep_fur.png");
	private final EntityModel<SheepRenderState> adultModel;
	private final EntityModel<SheepRenderState> babyModel;

	public SheepWoolLayer(RenderLayerParent<SheepRenderState, SheepModel> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.adultModel = new SheepFurModel(entityModelSet.bakeLayer(ModelLayers.SHEEP_WOOL));
		this.babyModel = new SheepFurModel(entityModelSet.bakeLayer(ModelLayers.SHEEP_BABY_WOOL));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, SheepRenderState sheepRenderState, float f, float g) {
		if (!sheepRenderState.isSheared) {
			EntityModel<SheepRenderState> entityModel = sheepRenderState.isBaby ? this.babyModel : this.adultModel;
			if (sheepRenderState.isInvisible) {
				if (sheepRenderState.appearsGlowing) {
					entityModel.setupAnim(sheepRenderState);
					VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.outline(SHEEP_FUR_LOCATION));
					entityModel.renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(sheepRenderState, 0.0F), -16777216);
				}
			} else {
				int r;
				if (sheepRenderState.customName != null && "jeb_".equals(sheepRenderState.customName.getString())) {
					int j = 25;
					int k = Mth.floor(sheepRenderState.ageInTicks);
					int l = k / 25 + sheepRenderState.id;
					int m = DyeColor.values().length;
					int n = l % m;
					int o = (l + 1) % m;
					float h = ((float)(k % 25) + Mth.frac(sheepRenderState.ageInTicks)) / 25.0F;
					int p = Sheep.getColor(DyeColor.byId(n));
					int q = Sheep.getColor(DyeColor.byId(o));
					r = ARGB.lerp(h, p, q);
				} else {
					r = Sheep.getColor(sheepRenderState.woolColor);
				}

				coloredCutoutModelCopyLayerRender(entityModel, SHEEP_FUR_LOCATION, poseStack, multiBufferSource, i, sheepRenderState, r);
			}
		}
	}
}
