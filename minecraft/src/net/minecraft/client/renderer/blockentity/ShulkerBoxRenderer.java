package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class ShulkerBoxRenderer extends BlockEntityRenderer<ShulkerBoxBlockEntity> {
	private final ShulkerModel<?> model;

	public ShulkerBoxRenderer(ShulkerModel<?> shulkerModel, BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
		this.model = shulkerModel;
	}

	public void render(
		ShulkerBoxBlockEntity shulkerBoxBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i
	) {
		Direction direction = Direction.UP;
		if (shulkerBoxBlockEntity.hasLevel()) {
			BlockState blockState = shulkerBoxBlockEntity.getLevel().getBlockState(shulkerBoxBlockEntity.getBlockPos());
			if (blockState.getBlock() instanceof ShulkerBoxBlock) {
				direction = blockState.getValue(ShulkerBoxBlock.FACING);
			}
		}

		DyeColor dyeColor = shulkerBoxBlockEntity.getColor();
		ResourceLocation resourceLocation;
		if (dyeColor == null) {
			resourceLocation = ModelBakery.DEFAULT_SHULKER_TEXTURE_LOCATION;
		} else {
			resourceLocation = (ResourceLocation)ModelBakery.SHULKER_TEXTURE_LOCATION.get(dyeColor.getId());
		}

		TextureAtlasSprite textureAtlasSprite = this.getSprite(resourceLocation);
		poseStack.pushPose();
		poseStack.translate(0.5, 1.5, 0.5);
		poseStack.scale(1.0F, -1.0F, -1.0F);
		poseStack.translate(0.0, 1.0, 0.0);
		float h = 0.9995F;
		poseStack.scale(0.9995F, 0.9995F, 0.9995F);
		poseStack.mulPose(direction.getRotation());
		poseStack.translate(0.0, -1.0, 0.0);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.CUTOUT_MIPPED);
		this.model.getBase().render(poseStack, vertexConsumer, 0.0625F, i, textureAtlasSprite);
		poseStack.translate(0.0, (double)(-shulkerBoxBlockEntity.getProgress(g) * 0.5F), 0.0);
		poseStack.mulPose(Vector3f.YP.rotation(270.0F * shulkerBoxBlockEntity.getProgress(g), true));
		this.model.getLid().render(poseStack, vertexConsumer, 0.0625F, i, textureAtlasSprite);
		poseStack.popPose();
	}
}
