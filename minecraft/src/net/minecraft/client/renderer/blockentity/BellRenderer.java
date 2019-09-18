package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;

@Environment(EnvType.CLIENT)
public class BellRenderer extends BatchedBlockEntityRenderer<BellBlockEntity> {
	public static final ResourceLocation BELL_RESOURCE_LOCATION = new ResourceLocation("entity/bell/bell_body");
	private final ModelPart bellBody = new ModelPart(32, 32, 0, 0);

	public BellRenderer() {
		this.bellBody.addBox(-3.0F, -6.0F, -3.0F, 6.0F, 7.0F, 6.0F);
		this.bellBody.setPos(8.0F, 12.0F, 8.0F);
		ModelPart modelPart = new ModelPart(32, 32, 0, 13);
		modelPart.addBox(4.0F, 4.0F, 4.0F, 8.0F, 2.0F, 8.0F);
		modelPart.setPos(-8.0F, -12.0F, -8.0F);
		this.bellBody.addChild(modelPart);
	}

	protected void renderToBuffer(
		BellBlockEntity bellBlockEntity, double d, double e, double f, float g, int i, RenderType renderType, BufferBuilder bufferBuilder, int j, int k
	) {
		float h = (float)bellBlockEntity.ticks + g;
		float l = 0.0F;
		float m = 0.0F;
		if (bellBlockEntity.shaking) {
			float n = Mth.sin(h / (float) Math.PI) / (4.0F + h / 3.0F);
			if (bellBlockEntity.clickDirection == Direction.NORTH) {
				l = -n;
			} else if (bellBlockEntity.clickDirection == Direction.SOUTH) {
				l = n;
			} else if (bellBlockEntity.clickDirection == Direction.EAST) {
				m = -n;
			} else if (bellBlockEntity.clickDirection == Direction.WEST) {
				m = n;
			}
		}

		this.bellBody.xRot = l;
		this.bellBody.zRot = m;
		bufferBuilder.pushPose();
		bufferBuilder.getPose().setIdentity();
		this.bellBody.render(bufferBuilder, 0.0625F, j, k, this.getSprite(BELL_RESOURCE_LOCATION));
		bufferBuilder.popPose();
	}
}
