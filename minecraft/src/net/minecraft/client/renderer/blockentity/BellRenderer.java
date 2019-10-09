package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;

@Environment(EnvType.CLIENT)
public class BellRenderer extends BlockEntityRenderer<BellBlockEntity> {
	public static final ResourceLocation BELL_RESOURCE_LOCATION = new ResourceLocation("entity/bell/bell_body");
	private final ModelPart bellBody = new ModelPart(32, 32, 0, 0);

	public BellRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
		this.bellBody.addBox(-3.0F, -6.0F, -3.0F, 6.0F, 7.0F, 6.0F);
		this.bellBody.setPos(8.0F, 12.0F, 8.0F);
		ModelPart modelPart = new ModelPart(32, 32, 0, 13);
		modelPart.addBox(4.0F, 4.0F, 4.0F, 8.0F, 2.0F, 8.0F);
		modelPart.setPos(-8.0F, -12.0F, -8.0F);
		this.bellBody.addChild(modelPart);
	}

	public void render(
		BellBlockEntity bellBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j
	) {
		float h = (float)bellBlockEntity.ticks + g;
		float k = 0.0F;
		float l = 0.0F;
		if (bellBlockEntity.shaking) {
			float m = Mth.sin(h / (float) Math.PI) / (4.0F + h / 3.0F);
			if (bellBlockEntity.clickDirection == Direction.NORTH) {
				k = -m;
			} else if (bellBlockEntity.clickDirection == Direction.SOUTH) {
				k = m;
			} else if (bellBlockEntity.clickDirection == Direction.EAST) {
				l = -m;
			} else if (bellBlockEntity.clickDirection == Direction.WEST) {
				l = m;
			}
		}

		this.bellBody.xRot = k;
		this.bellBody.zRot = l;
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
		this.bellBody.render(poseStack, vertexConsumer, 0.0625F, i, j, this.getSprite(BELL_RESOURCE_LOCATION));
	}
}
