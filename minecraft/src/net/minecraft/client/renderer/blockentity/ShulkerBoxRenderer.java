package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class ShulkerBoxRenderer implements BlockEntityRenderer<ShulkerBoxBlockEntity> {
	private final ShulkerBoxRenderer.ShulkerBoxModel model;

	public ShulkerBoxRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new ShulkerBoxRenderer.ShulkerBoxModel(context.bakeLayer(ModelLayers.SHULKER_BOX));
	}

	public void render(ShulkerBoxBlockEntity shulkerBoxBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		Direction direction = Direction.UP;
		if (shulkerBoxBlockEntity.hasLevel()) {
			BlockState blockState = shulkerBoxBlockEntity.getLevel().getBlockState(shulkerBoxBlockEntity.getBlockPos());
			if (blockState.getBlock() instanceof ShulkerBoxBlock) {
				direction = blockState.getValue(ShulkerBoxBlock.FACING);
			}
		}

		DyeColor dyeColor = shulkerBoxBlockEntity.getColor();
		Material material;
		if (dyeColor == null) {
			material = Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION;
		} else {
			material = (Material)Sheets.SHULKER_TEXTURE_LOCATION.get(dyeColor.getId());
		}

		poseStack.pushPose();
		poseStack.translate(0.5F, 0.5F, 0.5F);
		float g = 0.9995F;
		poseStack.scale(0.9995F, 0.9995F, 0.9995F);
		poseStack.mulPose(direction.getRotation());
		poseStack.scale(1.0F, -1.0F, -1.0F);
		poseStack.translate(0.0F, -1.0F, 0.0F);
		this.model.animate(shulkerBoxBlockEntity, f);
		VertexConsumer vertexConsumer = material.buffer(multiBufferSource, this.model::renderType);
		this.model.renderToBuffer(poseStack, vertexConsumer, i, j);
		poseStack.popPose();
	}

	@Environment(EnvType.CLIENT)
	static class ShulkerBoxModel extends Model {
		private final ModelPart root;
		private final ModelPart lid;

		public ShulkerBoxModel(ModelPart modelPart) {
			super(RenderType::entityCutoutNoCull);
			this.root = modelPart;
			this.lid = modelPart.getChild("lid");
		}

		public void animate(ShulkerBoxBlockEntity shulkerBoxBlockEntity, float f) {
			this.lid.setPos(0.0F, 24.0F - shulkerBoxBlockEntity.getProgress(f) * 0.5F * 16.0F, 0.0F);
			this.lid.yRot = 270.0F * shulkerBoxBlockEntity.getProgress(f) * (float) (Math.PI / 180.0);
		}

		@Override
		public ModelPart root() {
			return this.root;
		}
	}
}
