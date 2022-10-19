package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
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
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class HangingSignRenderer extends SignRenderer {
	private static final String PLANK = "plank";
	private static final String V_CHAINS = "vChains";
	public static final String NORMAL_CHAINS = "normalChains";
	public static final String CHAIN_L_1 = "chainL1";
	public static final String CHAIN_L_2 = "chainL2";
	public static final String CHAIN_R_1 = "chainR1";
	public static final String CHAIN_R_2 = "chainR2";
	public static final String BOARD = "board";
	private final Map<WoodType, HangingSignRenderer.HangingSignModel> hangingSignModels;

	public HangingSignRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		this.hangingSignModels = (Map<WoodType, HangingSignRenderer.HangingSignModel>)WoodType.values()
			.collect(
				ImmutableMap.toImmutableMap(
					woodType -> woodType, woodType -> new HangingSignRenderer.HangingSignModel(context.bakeLayer(ModelLayers.createHangingSignModelName(woodType)))
				)
			);
	}

	@Override
	public void render(SignBlockEntity signBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		BlockState blockState = signBlockEntity.getBlockState();
		poseStack.pushPose();
		WoodType woodType = SignBlock.getWoodType(blockState.getBlock());
		HangingSignRenderer.HangingSignModel hangingSignModel = (HangingSignRenderer.HangingSignModel)this.hangingSignModels.get(woodType);
		boolean bl = !(blockState.getBlock() instanceof CeilingHangingSignBlock);
		boolean bl2 = blockState.hasProperty(BlockStateProperties.ATTACHED) && (Boolean)blockState.getValue(BlockStateProperties.ATTACHED);
		poseStack.translate(0.5, 0.9375, 0.5);
		if (bl2) {
			float g = -RotationSegment.convertToDegrees((Integer)blockState.getValue(CeilingHangingSignBlock.ROTATION));
			poseStack.mulPose(Vector3f.YP.rotationDegrees(g));
		} else {
			poseStack.mulPose(Vector3f.YP.rotationDegrees(this.getSignAngle(blockState, bl)));
		}

		poseStack.translate(0.0, -0.3125, 0.0);
		hangingSignModel.evaluateVisibleParts(blockState);
		float g = 1.0F;
		this.renderSign(poseStack, multiBufferSource, i, j, 1.0F, woodType, hangingSignModel);
		this.renderSignText(signBlockEntity, poseStack, multiBufferSource, i, 1.0F);
	}

	private float getSignAngle(BlockState blockState, boolean bl) {
		return bl
			? -((Direction)blockState.getValue(WallSignBlock.FACING)).toYRot()
			: -((float)((Integer)blockState.getValue(CeilingHangingSignBlock.ROTATION) * 360) / 16.0F);
	}

	@Override
	Material getSignMaterial(WoodType woodType) {
		return Sheets.getHangingSignMaterial(woodType);
	}

	@Override
	void renderSignModel(PoseStack poseStack, int i, int j, Model model, VertexConsumer vertexConsumer) {
		HangingSignRenderer.HangingSignModel hangingSignModel = (HangingSignRenderer.HangingSignModel)model;
		hangingSignModel.root.render(poseStack, vertexConsumer, i, j);
	}

	@Override
	Vec3 getTextOffset(float f) {
		return new Vec3(0.0, (double)(-0.32F * f), (double)(0.063F * f));
	}

	public static LayerDefinition createHangingSignLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("board", CubeListBuilder.create().texOffs(0, 12).addBox(-7.0F, 0.0F, -1.0F, 14.0F, 10.0F, 2.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("plank", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -6.0F, -2.0F, 16.0F, 2.0F, 4.0F), PartPose.ZERO);
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("normalChains", CubeListBuilder.create(), PartPose.ZERO);
		partDefinition2.addOrReplaceChild(
			"chainL1",
			CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
			PartPose.offsetAndRotation(-5.0F, -6.0F, 0.0F, 0.0F, (float) (-Math.PI / 4), 0.0F)
		);
		partDefinition2.addOrReplaceChild(
			"chainL2",
			CubeListBuilder.create().texOffs(6, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
			PartPose.offsetAndRotation(-5.0F, -6.0F, 0.0F, 0.0F, (float) (Math.PI / 4), 0.0F)
		);
		partDefinition2.addOrReplaceChild(
			"chainR1",
			CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
			PartPose.offsetAndRotation(5.0F, -6.0F, 0.0F, 0.0F, (float) (-Math.PI / 4), 0.0F)
		);
		partDefinition2.addOrReplaceChild(
			"chainR2",
			CubeListBuilder.create().texOffs(6, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
			PartPose.offsetAndRotation(5.0F, -6.0F, 0.0F, 0.0F, (float) (Math.PI / 4), 0.0F)
		);
		partDefinition.addOrReplaceChild("vChains", CubeListBuilder.create().texOffs(14, 6).addBox(-6.0F, -6.0F, 0.0F, 12.0F, 6.0F, 0.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Environment(EnvType.CLIENT)
	public static final class HangingSignModel extends Model {
		public final ModelPart root;
		public final ModelPart plank;
		public final ModelPart vChains;
		public final ModelPart normalChains;

		public HangingSignModel(ModelPart modelPart) {
			super(RenderType::entityCutoutNoCull);
			this.root = modelPart;
			this.plank = modelPart.getChild("plank");
			this.normalChains = modelPart.getChild("normalChains");
			this.vChains = modelPart.getChild("vChains");
		}

		public void evaluateVisibleParts(BlockState blockState) {
			boolean bl = !(blockState.getBlock() instanceof CeilingHangingSignBlock);
			this.plank.visible = bl;
			this.vChains.visible = false;
			this.normalChains.visible = true;
			if (!bl) {
				boolean bl2 = (Boolean)blockState.getValue(BlockStateProperties.ATTACHED);
				this.normalChains.visible = !bl2;
				this.vChains.visible = bl2;
			}
		}

		@Override
		public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
			this.root.render(poseStack, vertexConsumer, i, j, f, g, h, k);
		}
	}
}
