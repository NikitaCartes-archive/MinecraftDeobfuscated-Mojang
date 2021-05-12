package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.Calendar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;

@Environment(EnvType.CLIENT)
public class ChestRenderer<T extends BlockEntity & LidBlockEntity> implements BlockEntityRenderer<T> {
	private static final String BOTTOM = "bottom";
	private static final String LID = "lid";
	private static final String LOCK = "lock";
	private final ModelPart lid;
	private final ModelPart bottom;
	private final ModelPart lock;
	private final ModelPart doubleLeftLid;
	private final ModelPart doubleLeftBottom;
	private final ModelPart doubleLeftLock;
	private final ModelPart doubleRightLid;
	private final ModelPart doubleRightBottom;
	private final ModelPart doubleRightLock;
	private boolean xmasTextures;

	public ChestRenderer(BlockEntityRendererProvider.Context context) {
		Calendar calendar = Calendar.getInstance();
		if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) {
			this.xmasTextures = true;
		}

		ModelPart modelPart = context.bakeLayer(ModelLayers.CHEST);
		this.bottom = modelPart.getChild("bottom");
		this.lid = modelPart.getChild("lid");
		this.lock = modelPart.getChild("lock");
		ModelPart modelPart2 = context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT);
		this.doubleLeftBottom = modelPart2.getChild("bottom");
		this.doubleLeftLid = modelPart2.getChild("lid");
		this.doubleLeftLock = modelPart2.getChild("lock");
		ModelPart modelPart3 = context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT);
		this.doubleRightBottom = modelPart3.getChild("bottom");
		this.doubleRightLid = modelPart3.getChild("lid");
		this.doubleRightLock = modelPart3.getChild("lock");
	}

	public static LayerDefinition createSingleBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild(
			"lid", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F)
		);
		partDefinition.addOrReplaceChild(
			"lock", CubeListBuilder.create().texOffs(0, 0).addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public static LayerDefinition createDoubleBodyRightLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild(
			"lid", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F)
		);
		partDefinition.addOrReplaceChild(
			"lock", CubeListBuilder.create().texOffs(0, 0).addBox(15.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public static LayerDefinition createDoubleBodyLeftLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild(
			"lid", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F)
		);
		partDefinition.addOrReplaceChild(
			"lock", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	@Override
	public void render(T blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		Level level = blockEntity.getLevel();
		boolean bl = level != null;
		BlockState blockState = bl ? blockEntity.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
		ChestType chestType = blockState.hasProperty((Property<T>)ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
		if (blockState.getBlock() instanceof AbstractChestBlock<?> abstractChestBlock) {
			boolean bl2 = chestType != ChestType.SINGLE;
			poseStack.pushPose();
			float g = ((Direction)blockState.getValue(ChestBlock.FACING)).toYRot();
			poseStack.translate(0.5, 0.5, 0.5);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(-g));
			poseStack.translate(-0.5, -0.5, -0.5);
			DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> neighborCombineResult;
			if (bl) {
				neighborCombineResult = abstractChestBlock.combine(blockState, level, blockEntity.getBlockPos(), true);
			} else {
				neighborCombineResult = DoubleBlockCombiner.Combiner::acceptNone;
			}

			float h = neighborCombineResult.apply(ChestBlock.opennessCombiner(blockEntity)).get(f);
			h = 1.0F - h;
			h = 1.0F - h * h * h;
			int k = neighborCombineResult.apply(new BrightnessCombiner<>()).applyAsInt(i);
			Material material = Sheets.chooseMaterial(blockEntity, chestType, this.xmasTextures);
			VertexConsumer vertexConsumer = material.buffer(multiBufferSource, RenderType::entityCutout);
			if (bl2) {
				if (chestType == ChestType.LEFT) {
					this.render(poseStack, vertexConsumer, this.doubleLeftLid, this.doubleLeftLock, this.doubleLeftBottom, h, k, j);
				} else {
					this.render(poseStack, vertexConsumer, this.doubleRightLid, this.doubleRightLock, this.doubleRightBottom, h, k, j);
				}
			} else {
				this.render(poseStack, vertexConsumer, this.lid, this.lock, this.bottom, h, k, j);
			}

			poseStack.popPose();
		}
	}

	private void render(PoseStack poseStack, VertexConsumer vertexConsumer, ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3, float f, int i, int j) {
		modelPart.xRot = -(f * (float) (Math.PI / 2));
		modelPart2.xRot = modelPart.xRot;
		modelPart.render(poseStack, vertexConsumer, i, j);
		modelPart2.render(poseStack, vertexConsumer, i, j);
		modelPart3.render(poseStack, vertexConsumer, i, j);
	}
}
