package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

@Environment(EnvType.CLIENT)
public class BedRenderer extends BatchedBlockEntityRenderer<BedBlockEntity> {
	public static final ResourceLocation[] TEXTURES = (ResourceLocation[])Arrays.stream(DyeColor.values())
		.sorted(Comparator.comparingInt(DyeColor::getId))
		.map(dyeColor -> new ResourceLocation("entity/bed/" + dyeColor.getName()))
		.toArray(ResourceLocation[]::new);
	private final ModelPart headPiece;
	private final ModelPart footPiece;
	private final ModelPart[] legs = new ModelPart[4];

	public BedRenderer() {
		this.headPiece = new ModelPart(64, 64, 0, 0);
		this.headPiece.addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F, 0.0F);
		this.footPiece = new ModelPart(64, 64, 0, 22);
		this.footPiece.addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F, 0.0F);
		this.legs[0] = new ModelPart(64, 64, 50, 0);
		this.legs[1] = new ModelPart(64, 64, 50, 6);
		this.legs[2] = new ModelPart(64, 64, 50, 12);
		this.legs[3] = new ModelPart(64, 64, 50, 18);
		this.legs[0].addBox(0.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F);
		this.legs[1].addBox(0.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F);
		this.legs[2].addBox(-16.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F);
		this.legs[3].addBox(-16.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F);
		this.legs[0].xRot = (float) (Math.PI / 2);
		this.legs[1].xRot = (float) (Math.PI / 2);
		this.legs[2].xRot = (float) (Math.PI / 2);
		this.legs[3].xRot = (float) (Math.PI / 2);
		this.legs[0].zRot = 0.0F;
		this.legs[1].zRot = (float) (Math.PI / 2);
		this.legs[2].zRot = (float) (Math.PI * 3.0 / 2.0);
		this.legs[3].zRot = (float) Math.PI;
	}

	protected void renderToBuffer(
		BedBlockEntity bedBlockEntity, double d, double e, double f, float g, int i, RenderType renderType, BufferBuilder bufferBuilder, int j, int k
	) {
		ResourceLocation resourceLocation;
		if (i >= 0) {
			resourceLocation = (ResourceLocation)ModelBakery.DESTROY_STAGES.get(i);
		} else {
			resourceLocation = TEXTURES[bedBlockEntity.getColor().getId()];
		}

		this.doRender(bufferBuilder, resourceLocation, bedBlockEntity, j, k);
	}

	public void doRender(BufferBuilder bufferBuilder, ResourceLocation resourceLocation, BedBlockEntity bedBlockEntity, int i, int j) {
		if (bedBlockEntity.hasLevel()) {
			BlockState blockState = bedBlockEntity.getBlockState();
			this.renderPiece(bufferBuilder, blockState.getValue(BedBlock.PART) == BedPart.HEAD, blockState.getValue(BedBlock.FACING), resourceLocation, i, j, false);
		} else {
			this.renderPiece(bufferBuilder, true, Direction.SOUTH, resourceLocation, i, j, false);
			this.renderPiece(bufferBuilder, false, Direction.SOUTH, resourceLocation, i, j, true);
		}
	}

	private void renderPiece(BufferBuilder bufferBuilder, boolean bl, Direction direction, ResourceLocation resourceLocation, int i, int j, boolean bl2) {
		this.headPiece.visible = bl;
		this.footPiece.visible = !bl;
		this.legs[0].visible = !bl;
		this.legs[1].visible = bl;
		this.legs[2].visible = !bl;
		this.legs[3].visible = bl;
		bufferBuilder.pushPose();
		bufferBuilder.translate(0.0, 0.5625, bl2 ? -1.0 : 0.0);
		bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, 90.0F, true));
		bufferBuilder.translate(0.5, 0.5, 0.5);
		bufferBuilder.multiplyPose(new Quaternion(Vector3f.ZP, 180.0F + direction.toYRot(), true));
		bufferBuilder.translate(-0.5, -0.5, -0.5);
		TextureAtlasSprite textureAtlasSprite = this.getSprite(resourceLocation);
		this.headPiece.render(bufferBuilder, 0.0625F, i, j, textureAtlasSprite);
		this.footPiece.render(bufferBuilder, 0.0625F, i, j, textureAtlasSprite);
		this.legs[0].render(bufferBuilder, 0.0625F, i, j, textureAtlasSprite);
		this.legs[1].render(bufferBuilder, 0.0625F, i, j, textureAtlasSprite);
		this.legs[2].render(bufferBuilder, 0.0625F, i, j, textureAtlasSprite);
		this.legs[3].render(bufferBuilder, 0.0625F, i, j, textureAtlasSprite);
		bufferBuilder.popPose();
	}
}
