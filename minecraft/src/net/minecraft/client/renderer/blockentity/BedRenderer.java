package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

@Environment(EnvType.CLIENT)
public class BedRenderer extends BlockEntityRenderer<BedBlockEntity> {
	private static final ResourceLocation[] TEXTURES = (ResourceLocation[])Arrays.stream(DyeColor.values())
		.sorted(Comparator.comparingInt(DyeColor::getId))
		.map(dyeColor -> new ResourceLocation("textures/entity/bed/" + dyeColor.getName() + ".png"))
		.toArray(ResourceLocation[]::new);
	private final BedModel bedModel = new BedModel();

	public void render(BedBlockEntity bedBlockEntity, double d, double e, double f, float g, int i) {
		if (i >= 0) {
			this.bindTexture(BREAKING_LOCATIONS[i]);
			RenderSystem.matrixMode(5890);
			RenderSystem.pushMatrix();
			RenderSystem.scalef(4.0F, 4.0F, 1.0F);
			RenderSystem.translatef(0.0625F, 0.0625F, 0.0625F);
			RenderSystem.matrixMode(5888);
		} else {
			ResourceLocation resourceLocation = TEXTURES[bedBlockEntity.getColor().getId()];
			if (resourceLocation != null) {
				this.bindTexture(resourceLocation);
			}
		}

		if (bedBlockEntity.hasLevel()) {
			BlockState blockState = bedBlockEntity.getBlockState();
			this.renderPiece(blockState.getValue(BedBlock.PART) == BedPart.HEAD, d, e, f, blockState.getValue(BedBlock.FACING));
		} else {
			this.renderPiece(true, d, e, f, Direction.SOUTH);
			this.renderPiece(false, d, e, f - 1.0, Direction.SOUTH);
		}

		if (i >= 0) {
			RenderSystem.matrixMode(5890);
			RenderSystem.popMatrix();
			RenderSystem.matrixMode(5888);
		}
	}

	private void renderPiece(boolean bl, double d, double e, double f, Direction direction) {
		this.bedModel.preparePiece(bl);
		RenderSystem.pushMatrix();
		RenderSystem.translatef((float)d, (float)e + 0.5625F, (float)f);
		RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
		RenderSystem.translatef(0.5F, 0.5F, 0.5F);
		RenderSystem.rotatef(180.0F + direction.toYRot(), 0.0F, 0.0F, 1.0F);
		RenderSystem.translatef(-0.5F, -0.5F, -0.5F);
		RenderSystem.enableRescaleNormal();
		this.bedModel.render();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.popMatrix();
	}
}
