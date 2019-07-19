package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class ShulkerBoxRenderer extends BlockEntityRenderer<ShulkerBoxBlockEntity> {
	private final ShulkerModel<?> model;

	public ShulkerBoxRenderer(ShulkerModel<?> shulkerModel) {
		this.model = shulkerModel;
	}

	public void render(ShulkerBoxBlockEntity shulkerBoxBlockEntity, double d, double e, double f, float g, int i) {
		Direction direction = Direction.UP;
		if (shulkerBoxBlockEntity.hasLevel()) {
			BlockState blockState = this.getLevel().getBlockState(shulkerBoxBlockEntity.getBlockPos());
			if (blockState.getBlock() instanceof ShulkerBoxBlock) {
				direction = blockState.getValue(ShulkerBoxBlock.FACING);
			}
		}

		GlStateManager.enableDepthTest();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		GlStateManager.disableCull();
		if (i >= 0) {
			this.bindTexture(BREAKING_LOCATIONS[i]);
			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix();
			GlStateManager.scalef(4.0F, 4.0F, 1.0F);
			GlStateManager.translatef(0.0625F, 0.0625F, 0.0625F);
			GlStateManager.matrixMode(5888);
		} else {
			DyeColor dyeColor = shulkerBoxBlockEntity.getColor();
			if (dyeColor == null) {
				this.bindTexture(ShulkerRenderer.DEFAULT_TEXTURE_LOCATION);
			} else {
				this.bindTexture(ShulkerRenderer.TEXTURE_LOCATION[dyeColor.getId()]);
			}
		}

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();
		if (i < 0) {
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		}

		GlStateManager.translatef((float)d + 0.5F, (float)e + 1.5F, (float)f + 0.5F);
		GlStateManager.scalef(1.0F, -1.0F, -1.0F);
		GlStateManager.translatef(0.0F, 1.0F, 0.0F);
		float h = 0.9995F;
		GlStateManager.scalef(0.9995F, 0.9995F, 0.9995F);
		GlStateManager.translatef(0.0F, -1.0F, 0.0F);
		switch (direction) {
			case DOWN:
				GlStateManager.translatef(0.0F, 2.0F, 0.0F);
				GlStateManager.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
			case UP:
			default:
				break;
			case NORTH:
				GlStateManager.translatef(0.0F, 1.0F, 1.0F);
				GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
				break;
			case SOUTH:
				GlStateManager.translatef(0.0F, 1.0F, -1.0F);
				GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
				break;
			case WEST:
				GlStateManager.translatef(-1.0F, 1.0F, 0.0F);
				GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotatef(-90.0F, 0.0F, 0.0F, 1.0F);
				break;
			case EAST:
				GlStateManager.translatef(1.0F, 1.0F, 0.0F);
				GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
		}

		this.model.getBase().render(0.0625F);
		GlStateManager.translatef(0.0F, -shulkerBoxBlockEntity.getProgress(g) * 0.5F, 0.0F);
		GlStateManager.rotatef(270.0F * shulkerBoxBlockEntity.getProgress(g), 0.0F, 1.0F, 0.0F);
		this.model.getLid().render(0.0625F);
		GlStateManager.enableCull();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (i >= 0) {
			GlStateManager.matrixMode(5890);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5888);
		}
	}
}
