/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

@Environment(value=EnvType.CLIENT)
public class BedRenderer
implements BlockEntityRenderer<BedBlockEntity> {
    private final ModelPart headRoot;
    private final ModelPart footRoot;

    public BedRenderer(BlockEntityRendererProvider.Context context) {
        this.headRoot = context.bakeLayer(ModelLayers.BED_HEAD);
        this.footRoot = context.bakeLayer(ModelLayers.BED_FOOT);
    }

    public static LayerDefinition createHeadLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 6.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(50, 6).addBox(0.0f, 6.0f, 0.0f, 3.0f, 3.0f, 3.0f), PartPose.rotation(1.5707964f, 0.0f, 1.5707964f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(50, 18).addBox(-16.0f, 6.0f, 0.0f, 3.0f, 3.0f, 3.0f), PartPose.rotation(1.5707964f, 0.0f, (float)Math.PI));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public static LayerDefinition createFootLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 22).addBox(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 6.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(50, 0).addBox(0.0f, 6.0f, -16.0f, 3.0f, 3.0f, 3.0f), PartPose.rotation(1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(50, 12).addBox(-16.0f, 6.0f, -16.0f, 3.0f, 3.0f, 3.0f), PartPose.rotation(1.5707964f, 0.0f, 4.712389f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void render(BedBlockEntity bedBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        Material material = Sheets.BED_TEXTURES[bedBlockEntity.getColor().getId()];
        Level level = bedBlockEntity.getLevel();
        if (level != null) {
            BlockState blockState = bedBlockEntity.getBlockState();
            DoubleBlockCombiner.NeighborCombineResult<BedBlockEntity> neighborCombineResult = DoubleBlockCombiner.combineWithNeigbour(BlockEntityType.BED, BedBlock::getBlockType, BedBlock::getConnectedDirection, ChestBlock.FACING, blockState, level, bedBlockEntity.getBlockPos(), (levelAccessor, blockPos) -> false);
            int k = ((Int2IntFunction)neighborCombineResult.apply(new BrightnessCombiner())).get(i);
            this.renderPiece(poseStack, multiBufferSource, blockState.getValue(BedBlock.PART) == BedPart.HEAD ? this.headRoot : this.footRoot, blockState.getValue(BedBlock.FACING), material, k, j, false);
        } else {
            this.renderPiece(poseStack, multiBufferSource, this.headRoot, Direction.SOUTH, material, i, j, false);
            this.renderPiece(poseStack, multiBufferSource, this.footRoot, Direction.SOUTH, material, i, j, true);
        }
    }

    private void renderPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, ModelPart modelPart, Direction direction, Material material, int i, int j, boolean bl) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.5625f, bl ? -1.0f : 0.0f);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f + direction.toYRot()));
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        VertexConsumer vertexConsumer = material.buffer(multiBufferSource, RenderType::entitySolid);
        modelPart.render(poseStack, vertexConsumer, i, j);
        poseStack.popPose();
    }
}

