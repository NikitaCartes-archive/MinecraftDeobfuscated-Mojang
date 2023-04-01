package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TailModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class TailLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	private static final int FORCED_TAIL_SKIN = -1;
	private static final List<ResourceLocation> TAIL_LOCATIONS = List.of(
		new ResourceLocation("textures/entity/player/tails/red_fox.png"),
		new ResourceLocation("textures/entity/player/tails/snow_fox.png"),
		new ResourceLocation("textures/entity/player/tails/alex.png"),
		new ResourceLocation("textures/entity/player/tails/ari.png"),
		new ResourceLocation("textures/entity/player/tails/efe.png"),
		new ResourceLocation("textures/entity/player/tails/kai.png"),
		new ResourceLocation("textures/entity/player/tails/makena.png"),
		new ResourceLocation("textures/entity/player/tails/noor.png"),
		new ResourceLocation("textures/entity/player/tails/steve.png"),
		new ResourceLocation("textures/entity/player/tails/sunny.png"),
		new ResourceLocation("textures/entity/player/tails/zuri.png"),
		new ResourceLocation("textures/entity/player/tails/brown_bear.png"),
		new ResourceLocation("textures/entity/player/tails/striped.png"),
		new ResourceLocation("textures/entity/player/tails/black_fox.png"),
		new ResourceLocation("textures/entity/player/tails/earthern.png"),
		new ResourceLocation("textures/entity/player/tails/fire_fox.png")
	);
	private final TailModel<T> model;

	public TailLayer(RenderLayerParent<T, M> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new TailModel<>(entityModelSet.bakeLayer(ModelLayers.TAIL));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		if (Rules.TRAILS_AND_TAILS.get()) {
			poseStack.pushPose();
			this.model.setupAnim(livingEntity, f, g, j, k, l);
			int m = Math.floorMod(livingEntity.getUUID().getLeastSignificantBits(), TAIL_LOCATIONS.size());
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType((ResourceLocation)TAIL_LOCATIONS.get(m)));
			this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			poseStack.popPose();
		}
	}
}
