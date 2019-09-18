package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
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
public class ShulkerBoxRenderer extends BatchedBlockEntityRenderer<ShulkerBoxBlockEntity> {
	private final ShulkerModel<?> model;

	public ShulkerBoxRenderer(ShulkerModel<?> shulkerModel) {
		this.model = shulkerModel;
	}

	protected void renderToBuffer(
		ShulkerBoxBlockEntity shulkerBoxBlockEntity, double d, double e, double f, float g, int i, RenderType renderType, BufferBuilder bufferBuilder, int j, int k
	) {
		Direction direction = Direction.UP;
		if (shulkerBoxBlockEntity.hasLevel()) {
			BlockState blockState = this.getLevel().getBlockState(shulkerBoxBlockEntity.getBlockPos());
			if (blockState.getBlock() instanceof ShulkerBoxBlock) {
				direction = blockState.getValue(ShulkerBoxBlock.FACING);
			}
		}

		ResourceLocation resourceLocation;
		if (i >= 0) {
			resourceLocation = (ResourceLocation)ModelBakery.DESTROY_STAGES.get(i);
		} else {
			DyeColor dyeColor = shulkerBoxBlockEntity.getColor();
			if (dyeColor == null) {
				resourceLocation = ModelBakery.DEFAULT_SHULKER_TEXTURE_LOCATION;
			} else {
				resourceLocation = (ResourceLocation)ModelBakery.SHULKER_TEXTURE_LOCATION.get(dyeColor.getId());
			}
		}

		TextureAtlasSprite textureAtlasSprite = this.getSprite(resourceLocation);
		bufferBuilder.pushPose();
		bufferBuilder.translate(0.5, 1.5, 0.5);
		bufferBuilder.scale(1.0F, -1.0F, -1.0F);
		bufferBuilder.translate(0.0, 1.0, 0.0);
		float h = 0.9995F;
		bufferBuilder.scale(0.9995F, 0.9995F, 0.9995F);
		bufferBuilder.translate(0.0, -1.0, 0.0);
		switch (direction) {
			case DOWN:
				bufferBuilder.translate(0.0, 2.0, 0.0);
				bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, 180.0F, true));
			case UP:
			default:
				break;
			case NORTH:
				bufferBuilder.translate(0.0, 1.0, 1.0);
				bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, 90.0F, true));
				bufferBuilder.multiplyPose(new Quaternion(Vector3f.ZP, 180.0F, true));
				break;
			case SOUTH:
				bufferBuilder.translate(0.0, 1.0, -1.0);
				bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, 90.0F, true));
				break;
			case WEST:
				bufferBuilder.translate(-1.0, 1.0, 0.0);
				bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, 90.0F, true));
				bufferBuilder.multiplyPose(new Quaternion(Vector3f.ZP, -90.0F, true));
				break;
			case EAST:
				bufferBuilder.translate(1.0, 1.0, 0.0);
				bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, 90.0F, true));
				bufferBuilder.multiplyPose(new Quaternion(Vector3f.ZP, 90.0F, true));
		}

		this.model.getBase().render(bufferBuilder, 0.0625F, j, k, textureAtlasSprite);
		bufferBuilder.translate(0.0, (double)(-shulkerBoxBlockEntity.getProgress(g) * 0.5F), 0.0);
		bufferBuilder.multiplyPose(new Quaternion(Vector3f.YP, 270.0F * shulkerBoxBlockEntity.getProgress(g), true));
		this.model.getLid().render(bufferBuilder, 0.0625F, j, k, textureAtlasSprite);
		bufferBuilder.popPose();
	}
}
