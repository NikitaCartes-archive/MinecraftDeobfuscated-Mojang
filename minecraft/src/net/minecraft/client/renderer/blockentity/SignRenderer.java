package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
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
public class SignRenderer implements BlockEntityRenderer<SignBlockEntity> {
	private final Map<WoodType, SignRenderer.SignModel> signModels;
	private final Font font;

	public SignRenderer(BlockEntityRendererProvider.Context context) {
		this.signModels = (Map<WoodType, SignRenderer.SignModel>)WoodType.values()
			.collect(
				ImmutableMap.toImmutableMap(woodType -> woodType, woodType -> new SignRenderer.SignModel(context.bakeLayer(ModelLayers.createSignModelName(woodType))))
			);
		this.font = context.getFont();
	}

	public void render(SignBlockEntity signBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		BlockState blockState = signBlockEntity.getBlockState();
		poseStack.pushPose();
		float g = 0.6666667F;
		WoodType woodType = getWoodType(blockState.getBlock());
		SignRenderer.SignModel signModel = (SignRenderer.SignModel)this.signModels.get(woodType);
		if (blockState.getBlock() instanceof StandingSignBlock) {
			poseStack.translate(0.5, 0.5, 0.5);
			float h = -((float)((Integer)blockState.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
			signModel.stick.visible = true;
		} else {
			poseStack.translate(0.5, 0.5, 0.5);
			float h = -((Direction)blockState.getValue(WallSignBlock.FACING)).toYRot();
			poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
			poseStack.translate(0.0, -0.3125, -0.4375);
			signModel.stick.visible = false;
		}

		poseStack.pushPose();
		poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
		Material material = Sheets.signTexture(woodType);
		VertexConsumer vertexConsumer = material.buffer(multiBufferSource, signModel::renderType);
		signModel.root.render(poseStack, vertexConsumer, i, j);
		poseStack.popPose();
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
				List<FormattedCharSequence> list = this.font.split(component, 90);
				return list.isEmpty() ? FormattedCharSequence.EMPTY : (FormattedCharSequence)list.get(0);
			});
			if (formattedCharSequence != null) {
				float s = (float)(-this.font.width(formattedCharSequence) / 2);
				this.font.drawInBatch(formattedCharSequence, s, (float)(r * 10 - 20), p, false, poseStack.last().pose(), multiBufferSource, false, 0, i);
			}
		}

		poseStack.popPose();
	}

	public static WoodType getWoodType(Block block) {
		WoodType woodType;
		if (block instanceof SignBlock) {
			woodType = ((SignBlock)block).type();
		} else {
			woodType = WoodType.OAK;
		}

		return woodType;
	}

	public static SignRenderer.SignModel createSignModel(EntityModelSet entityModelSet, WoodType woodType) {
		return new SignRenderer.SignModel(entityModelSet.bakeLayer(ModelLayers.createSignModelName(woodType)));
	}

	public static LayerDefinition createSignLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("stick", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Environment(EnvType.CLIENT)
	public static final class SignModel extends Model {
		public final ModelPart root;
		public final ModelPart stick;

		public SignModel(ModelPart modelPart) {
			super(RenderType::entityCutoutNoCull);
			this.root = modelPart;
			this.stick = modelPart.getChild("stick");
		}

		@Override
		public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
			this.root.render(poseStack, vertexConsumer, i, j, f, g, h, k);
		}
	}
}
