package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BellModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BellBlockEntity;

@Environment(EnvType.CLIENT)
public class BellRenderer implements BlockEntityRenderer<BellBlockEntity> {
	public static final Material BELL_RESOURCE_LOCATION = new Material(
		TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("entity/bell/bell_body")
	);
	private final BellModel model;

	public BellRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new BellModel(context.bakeLayer(ModelLayers.BELL));
	}

	public void render(BellBlockEntity bellBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		VertexConsumer vertexConsumer = BELL_RESOURCE_LOCATION.buffer(multiBufferSource, RenderType::entitySolid);
		this.model.setupAnim(bellBlockEntity, f);
		this.model.renderToBuffer(poseStack, vertexConsumer, i, j);
	}
}
