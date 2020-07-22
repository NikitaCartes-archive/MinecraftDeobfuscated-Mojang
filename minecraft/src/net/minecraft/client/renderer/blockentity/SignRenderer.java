package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

@Environment(EnvType.CLIENT)
public class SignRenderer extends BlockEntityRenderer<SignBlockEntity> {
	private final SignRenderer.SignModel signModel = new SignRenderer.SignModel();

	public SignRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}

	public void render(SignBlockEntity signBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		BlockState blockState = signBlockEntity.getBlockState();
		poseStack.pushPose();
		float g = 0.6666667F;
		if (blockState.getBlock() instanceof StandingSignBlock) {
			poseStack.translate(0.5, 0.5, 0.5);
			float h = -((float)((Integer)blockState.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
			this.signModel.stick.visible = true;
		} else {
			poseStack.translate(0.5, 0.5, 0.5);
			float h = -((Direction)blockState.getValue(WallSignBlock.FACING)).toYRot();
			poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
			poseStack.translate(0.0, -0.3125, -0.4375);
			this.signModel.stick.visible = false;
		}

		poseStack.pushPose();
		poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
		Material material = getMaterial(blockState.getBlock());
		VertexConsumer vertexConsumer = material.buffer(multiBufferSource, this.signModel::renderType);
		this.signModel.sign.render(poseStack, vertexConsumer, i, j);
		this.signModel.stick.render(poseStack, vertexConsumer, i, j);
		poseStack.popPose();
		Font font = this.renderer.getFont();
		float k = 0.010416667F;
		poseStack.translate(0.0, 0.33333334F, 0.046666667F);
		poseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
		int l = signBlockEntity.getColor().getTextColor();
		double d = 0.4;
		int m = (int)((double)NativeImage.getR(l) * 0.4);
		int n = (int)((double)NativeImage.getG(l) * 0.4);
		int o = (int)((double)NativeImage.getB(l) * 0.4);
		int p = NativeImage.combine(0, o, n, m);
		int q = 20;

		for (int r = 0; r < 4; r++) {
			FormattedCharSequence formattedCharSequence = signBlockEntity.getRenderMessage(r, component -> {
				List<FormattedCharSequence> list = font.split(component, 90);
				return list.isEmpty() ? FormattedCharSequence.EMPTY : (FormattedCharSequence)list.get(0);
			});
			if (formattedCharSequence != null) {
				float s = (float)(-font.width(formattedCharSequence) / 2);
				font.drawInBatch(formattedCharSequence, s, (float)(r * 10 - 20), p, false, poseStack.last().pose(), multiBufferSource, false, 0, i);
			}
		}

		poseStack.popPose();
	}

	public static Material getMaterial(Block block) {
		WoodType woodType;
		if (block instanceof SignBlock) {
			woodType = ((SignBlock)block).type();
		} else {
			woodType = WoodType.OAK;
		}

		return Sheets.signTexture(woodType);
	}

	@Environment(EnvType.CLIENT)
	public static final class SignModel extends Model {
		public final ModelPart sign = new ModelPart(64, 32, 0, 0);
		public final ModelPart stick;

		public SignModel() {
			super(RenderType::entityCutoutNoCull);
			this.sign.addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F, 0.0F);
			this.stick = new ModelPart(64, 32, 0, 14);
			this.stick.addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F, 0.0F);
		}

		@Override
		public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
			this.sign.render(poseStack, vertexConsumer, i, j, f, g, h, k);
			this.stick.render(poseStack, vertexConsumer, i, j, f, g, h, k);
		}
	}
}
