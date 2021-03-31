package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;

@Environment(EnvType.CLIENT)
public class BellRenderer implements BlockEntityRenderer<BellBlockEntity> {
	public static final Material BELL_RESOURCE_LOCATION = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/bell/bell_body"));
	private static final String BELL_BODY = "bell_body";
	private final ModelPart bellBody;

	public BellRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart modelPart = context.bakeLayer(ModelLayers.BELL);
		this.bellBody = modelPart.getChild("bell_body");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"bell_body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 7.0F, 6.0F), PartPose.offset(8.0F, 12.0F, 8.0F)
		);
		partDefinition2.addOrReplaceChild(
			"bell_base", CubeListBuilder.create().texOffs(0, 13).addBox(4.0F, 4.0F, 4.0F, 8.0F, 2.0F, 8.0F), PartPose.offset(-8.0F, -12.0F, -8.0F)
		);
		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	public void render(BellBlockEntity bellBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		float g = (float)bellBlockEntity.ticks + f;
		float h = 0.0F;
		float k = 0.0F;
		if (bellBlockEntity.shaking) {
			float l = Mth.sin(g / (float) Math.PI) / (4.0F + g / 3.0F);
			if (bellBlockEntity.clickDirection == Direction.NORTH) {
				h = -l;
			} else if (bellBlockEntity.clickDirection == Direction.SOUTH) {
				h = l;
			} else if (bellBlockEntity.clickDirection == Direction.EAST) {
				k = -l;
			} else if (bellBlockEntity.clickDirection == Direction.WEST) {
				k = l;
			}
		}

		this.bellBody.xRot = h;
		this.bellBody.zRot = k;
		VertexConsumer vertexConsumer = BELL_RESOURCE_LOCATION.buffer(multiBufferSource, RenderType::entitySolid);
		this.bellBody.render(poseStack, vertexConsumer, i, j);
	}
}
