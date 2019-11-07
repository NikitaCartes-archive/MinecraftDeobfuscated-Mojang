package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
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

	public void render(ShulkerBoxBlockEntity shulkerBoxBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
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
		poseStack.translate(0.5, 0.5, 0.5);
		float g = 0.9995F;
		poseStack.scale(0.9995F, 0.9995F, 0.9995F);
		poseStack.mulPose(direction.getRotation());
		poseStack.scale(1.0F, -1.0F, -1.0F);
		poseStack.translate(0.0, -1.0, 0.0);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
		this.model.getBase().render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
		poseStack.translate(0.0, (double)(-shulkerBoxBlockEntity.getProgress(f) * 0.5F), 0.0);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(270.0F * shulkerBoxBlockEntity.getProgress(f)));
		this.model.getLid().render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
		poseStack.popPose();
	}
}
