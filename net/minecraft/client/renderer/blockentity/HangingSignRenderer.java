/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class HangingSignRenderer
extends SignRenderer {
    private static final String PLANK = "plank";
    private static final String V_CHAINS = "vChains";
    public static final String NORMAL_CHAINS = "normalChains";
    public static final String CHAIN_L_1 = "chainL1";
    public static final String CHAIN_L_2 = "chainL2";
    public static final String CHAIN_R_1 = "chainR1";
    public static final String CHAIN_R_2 = "chainR2";
    public static final String BOARD = "board";
    private final Map<WoodType, HangingSignModel> hangingSignModels = WoodType.values().collect(ImmutableMap.toImmutableMap(woodType -> woodType, woodType -> new HangingSignModel(context.bakeLayer(ModelLayers.createHangingSignModelName(woodType)))));

    public HangingSignRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SignBlockEntity signBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        float g;
        BlockState blockState = signBlockEntity.getBlockState();
        poseStack.pushPose();
        WoodType woodType = SignBlock.getWoodType(blockState.getBlock());
        HangingSignModel hangingSignModel = this.hangingSignModels.get(woodType);
        boolean bl = !(blockState.getBlock() instanceof CeilingHangingSignBlock);
        boolean bl2 = blockState.hasProperty(BlockStateProperties.ATTACHED) && blockState.getValue(BlockStateProperties.ATTACHED) != false;
        poseStack.translate(0.5, 0.9375, 0.5);
        if (bl2) {
            g = -RotationSegment.convertToDegrees(blockState.getValue(CeilingHangingSignBlock.ROTATION));
            poseStack.mulPose(Axis.YP.rotationDegrees(g));
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(this.getSignAngle(blockState, bl)));
        }
        poseStack.translate(0.0f, -0.3125f, 0.0f);
        hangingSignModel.evaluateVisibleParts(blockState);
        g = 1.0f;
        this.renderSign(poseStack, multiBufferSource, i, j, 1.0f, woodType, hangingSignModel);
        this.renderSignText(signBlockEntity, poseStack, multiBufferSource, i, 1.0f);
    }

    private float getSignAngle(BlockState blockState, boolean bl) {
        return bl ? -blockState.getValue(WallSignBlock.FACING).toYRot() : -((float)(blockState.getValue(CeilingHangingSignBlock.ROTATION) * 360) / 16.0f);
    }

    @Override
    Material getSignMaterial(WoodType woodType) {
        return Sheets.getHangingSignMaterial(woodType);
    }

    @Override
    void renderSignModel(PoseStack poseStack, int i, int j, Model model, VertexConsumer vertexConsumer) {
        HangingSignModel hangingSignModel = (HangingSignModel)model;
        hangingSignModel.root.render(poseStack, vertexConsumer, i, j);
    }

    @Override
    Vec3 getTextOffset(float f) {
        return new Vec3(0.0, -0.32f * f, 0.063f * f);
    }

    public static LayerDefinition createHangingSignLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild(BOARD, CubeListBuilder.create().texOffs(0, 12).addBox(-7.0f, 0.0f, -1.0f, 14.0f, 10.0f, 2.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild(PLANK, CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -6.0f, -2.0f, 16.0f, 2.0f, 4.0f), PartPose.ZERO);
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(NORMAL_CHAINS, CubeListBuilder.create(), PartPose.ZERO);
        partDefinition2.addOrReplaceChild(CHAIN_L_1, CubeListBuilder.create().texOffs(0, 6).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), PartPose.offsetAndRotation(-5.0f, -6.0f, 0.0f, 0.0f, -0.7853982f, 0.0f));
        partDefinition2.addOrReplaceChild(CHAIN_L_2, CubeListBuilder.create().texOffs(6, 6).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), PartPose.offsetAndRotation(-5.0f, -6.0f, 0.0f, 0.0f, 0.7853982f, 0.0f));
        partDefinition2.addOrReplaceChild(CHAIN_R_1, CubeListBuilder.create().texOffs(0, 6).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), PartPose.offsetAndRotation(5.0f, -6.0f, 0.0f, 0.0f, -0.7853982f, 0.0f));
        partDefinition2.addOrReplaceChild(CHAIN_R_2, CubeListBuilder.create().texOffs(6, 6).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), PartPose.offsetAndRotation(5.0f, -6.0f, 0.0f, 0.0f, 0.7853982f, 0.0f));
        partDefinition.addOrReplaceChild(V_CHAINS, CubeListBuilder.create().texOffs(14, 6).addBox(-6.0f, -6.0f, 0.0f, 12.0f, 6.0f, 0.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Environment(value=EnvType.CLIENT)
    public static final class HangingSignModel
    extends Model {
        public final ModelPart root;
        public final ModelPart plank;
        public final ModelPart vChains;
        public final ModelPart normalChains;

        public HangingSignModel(ModelPart modelPart) {
            super(RenderType::entityCutoutNoCull);
            this.root = modelPart;
            this.plank = modelPart.getChild(HangingSignRenderer.PLANK);
            this.normalChains = modelPart.getChild(HangingSignRenderer.NORMAL_CHAINS);
            this.vChains = modelPart.getChild(HangingSignRenderer.V_CHAINS);
        }

        public void evaluateVisibleParts(BlockState blockState) {
            boolean bl;
            this.plank.visible = bl = !(blockState.getBlock() instanceof CeilingHangingSignBlock);
            this.vChains.visible = false;
            this.normalChains.visible = true;
            if (!bl) {
                boolean bl2 = blockState.getValue(BlockStateProperties.ATTACHED);
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

