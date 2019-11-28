/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Calendar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

@Environment(value=EnvType.CLIENT)
public class ChestRenderer<T extends BlockEntity>
extends BlockEntityRenderer<T> {
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

    public ChestRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) {
            this.xmasTextures = true;
        }
        this.bottom = new ModelPart(64, 64, 0, 19);
        this.bottom.addBox(1.0f, 0.0f, 1.0f, 14.0f, 10.0f, 14.0f, 0.0f);
        this.lid = new ModelPart(64, 64, 0, 0);
        this.lid.addBox(1.0f, 0.0f, 0.0f, 14.0f, 5.0f, 14.0f, 0.0f);
        this.lid.y = 9.0f;
        this.lid.z = 1.0f;
        this.lock = new ModelPart(64, 64, 0, 0);
        this.lock.addBox(7.0f, -1.0f, 15.0f, 2.0f, 4.0f, 1.0f, 0.0f);
        this.lock.y = 8.0f;
        this.doubleLeftBottom = new ModelPart(64, 64, 0, 19);
        this.doubleLeftBottom.addBox(1.0f, 0.0f, 1.0f, 15.0f, 10.0f, 14.0f, 0.0f);
        this.doubleLeftLid = new ModelPart(64, 64, 0, 0);
        this.doubleLeftLid.addBox(1.0f, 0.0f, 0.0f, 15.0f, 5.0f, 14.0f, 0.0f);
        this.doubleLeftLid.y = 9.0f;
        this.doubleLeftLid.z = 1.0f;
        this.doubleLeftLock = new ModelPart(64, 64, 0, 0);
        this.doubleLeftLock.addBox(15.0f, -1.0f, 15.0f, 1.0f, 4.0f, 1.0f, 0.0f);
        this.doubleLeftLock.y = 8.0f;
        this.doubleRightBottom = new ModelPart(64, 64, 0, 19);
        this.doubleRightBottom.addBox(0.0f, 0.0f, 1.0f, 15.0f, 10.0f, 14.0f, 0.0f);
        this.doubleRightLid = new ModelPart(64, 64, 0, 0);
        this.doubleRightLid.addBox(0.0f, 0.0f, 0.0f, 15.0f, 5.0f, 14.0f, 0.0f);
        this.doubleRightLid.y = 9.0f;
        this.doubleRightLid.z = 1.0f;
        this.doubleRightLock = new ModelPart(64, 64, 0, 0);
        this.doubleRightLock.addBox(0.0f, -1.0f, 15.0f, 1.0f, 4.0f, 1.0f, 0.0f);
        this.doubleRightLock.y = 8.0f;
    }

    @Override
    public void render(T blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        Level level = ((BlockEntity)blockEntity).getLevel();
        boolean bl = level != null;
        BlockState blockState = bl ? ((BlockEntity)blockEntity).getBlockState() : (BlockState)Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        ChestType chestType = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        Block block = blockState.getBlock();
        if (!(block instanceof AbstractChestBlock)) {
            return;
        }
        AbstractChestBlock abstractChestBlock = (AbstractChestBlock)block;
        boolean bl2 = chestType != ChestType.SINGLE;
        poseStack.pushPose();
        float g = blockState.getValue(ChestBlock.FACING).toYRot();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-g));
        poseStack.translate(-0.5, -0.5, -0.5);
        DoubleBlockCombiner.NeighborCombineResult<Object> neighborCombineResult = bl ? abstractChestBlock.combine(blockState, level, ((BlockEntity)blockEntity).getBlockPos(), true) : DoubleBlockCombiner.Combiner::acceptNone;
        float h = neighborCombineResult.apply(ChestBlock.opennessCombiner((LidBlockEntity)blockEntity)).get(f);
        h = 1.0f - h;
        h = 1.0f - h * h * h;
        int k = ((Int2IntFunction)neighborCombineResult.apply(new BrightnessCombiner())).applyAsInt(i);
        Material material = Sheets.chooseMaterial(blockEntity, chestType, this.xmasTextures);
        VertexConsumer vertexConsumer = material.buffer(multiBufferSource, RenderType::entityCutout);
        if (bl2) {
            if (chestType == ChestType.LEFT) {
                this.render(poseStack, vertexConsumer, this.doubleRightLid, this.doubleRightLock, this.doubleRightBottom, h, k, j);
            } else {
                this.render(poseStack, vertexConsumer, this.doubleLeftLid, this.doubleLeftLock, this.doubleLeftBottom, h, k, j);
            }
        } else {
            this.render(poseStack, vertexConsumer, this.lid, this.lock, this.bottom, h, k, j);
        }
        poseStack.popPose();
    }

    private void render(PoseStack poseStack, VertexConsumer vertexConsumer, ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3, float f, int i, int j) {
        modelPart2.xRot = modelPart.xRot = -(f * 1.5707964f);
        modelPart.render(poseStack, vertexConsumer, i, j);
        modelPart2.render(poseStack, vertexConsumer, i, j);
        modelPart3.render(poseStack, vertexConsumer, i, j);
    }
}

