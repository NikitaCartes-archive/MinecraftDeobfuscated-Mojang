package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

@Environment(EnvType.CLIENT)
public class BedRenderer extends BlockEntityRenderer<BedBlockEntity> {
	private final ModelPart headPiece;
	private final ModelPart footPiece;
	private final ModelPart[] legs = new ModelPart[4];

	public BedRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
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

	public void render(BedBlockEntity bedBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		Material material = Sheets.BED_TEXTURES[bedBlockEntity.getColor().getId()];
		if (bedBlockEntity.hasLevel()) {
			BlockState blockState = bedBlockEntity.getBlockState();
			this.renderPiece(
				poseStack, multiBufferSource, blockState.getValue(BedBlock.PART) == BedPart.HEAD, blockState.getValue(BedBlock.FACING), material, i, j, false
			);
		} else {
			this.renderPiece(poseStack, multiBufferSource, true, Direction.SOUTH, material, i, j, false);
			this.renderPiece(poseStack, multiBufferSource, false, Direction.SOUTH, material, i, j, true);
		}
	}

	private void renderPiece(
		PoseStack poseStack, MultiBufferSource multiBufferSource, boolean bl, Direction direction, Material material, int i, int j, boolean bl2
	) {
		this.headPiece.visible = bl;
		this.footPiece.visible = !bl;
		this.legs[0].visible = !bl;
		this.legs[1].visible = bl;
		this.legs[2].visible = !bl;
		this.legs[3].visible = bl;
		poseStack.pushPose();
		poseStack.translate(0.0, 0.5625, bl2 ? -1.0 : 0.0);
		poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
		poseStack.translate(0.5, 0.5, 0.5);
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F + direction.toYRot()));
		poseStack.translate(-0.5, -0.5, -0.5);
		VertexConsumer vertexConsumer = material.buffer(multiBufferSource, RenderType::entitySolid);
		this.headPiece.render(poseStack, vertexConsumer, i, j);
		this.footPiece.render(poseStack, vertexConsumer, i, j);
		this.legs[0].render(poseStack, vertexConsumer, i, j);
		this.legs[1].render(poseStack, vertexConsumer, i, j);
		this.legs[2].render(poseStack, vertexConsumer, i, j);
		this.legs[3].render(poseStack, vertexConsumer, i, j);
		poseStack.popPose();
	}
}
