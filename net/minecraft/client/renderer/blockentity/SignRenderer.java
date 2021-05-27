/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.client.Minecraft;
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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class SignRenderer
implements BlockEntityRenderer<SignBlockEntity> {
    public static final int MAX_LINE_WIDTH = 90;
    private static final int LINE_HEIGHT = 10;
    private static final String STICK = "stick";
    private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
    private final Map<WoodType, SignModel> signModels = WoodType.values().collect(ImmutableMap.toImmutableMap(woodType -> woodType, woodType -> new SignModel(context.bakeLayer(ModelLayers.createSignModelName(woodType)))));
    private final Font font;

    public SignRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(SignBlockEntity signBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        int o;
        boolean bl;
        int n;
        BlockState blockState = signBlockEntity.getBlockState();
        poseStack.pushPose();
        float g = 0.6666667f;
        WoodType woodType = SignRenderer.getWoodType(blockState.getBlock());
        SignModel signModel = this.signModels.get(woodType);
        if (blockState.getBlock() instanceof StandingSignBlock) {
            poseStack.translate(0.5, 0.5, 0.5);
            h = -((float)(blockState.getValue(StandingSignBlock.ROTATION) * 360) / 16.0f);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
            signModel.stick.visible = true;
        } else {
            poseStack.translate(0.5, 0.5, 0.5);
            h = -blockState.getValue(WallSignBlock.FACING).toYRot();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
            poseStack.translate(0.0, -0.3125, -0.4375);
            signModel.stick.visible = false;
        }
        poseStack.pushPose();
        poseStack.scale(0.6666667f, -0.6666667f, -0.6666667f);
        Material material = Sheets.getSignMaterial(woodType);
        VertexConsumer vertexConsumer = material.buffer(multiBufferSource, signModel::renderType);
        signModel.root.render(poseStack, vertexConsumer, i, j);
        poseStack.popPose();
        float k = 0.010416667f;
        poseStack.translate(0.0, 0.3333333432674408, 0.046666666865348816);
        poseStack.scale(0.010416667f, -0.010416667f, 0.010416667f);
        int l = SignRenderer.getDarkColor(signBlockEntity);
        int m = 20;
        FormattedCharSequence[] formattedCharSequences = signBlockEntity.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), component -> {
            List<FormattedCharSequence> list = this.font.split((FormattedText)component, 90);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
        });
        if (signBlockEntity.hasGlowingText()) {
            n = signBlockEntity.getColor().getTextColor();
            bl = SignRenderer.isOutlineVisible(signBlockEntity, n);
            o = 0xF000F0;
        } else {
            n = l;
            bl = false;
            o = i;
        }
        for (int p = 0; p < 4; ++p) {
            FormattedCharSequence formattedCharSequence = formattedCharSequences[p];
            float q = -this.font.width(formattedCharSequence) / 2;
            if (bl) {
                this.font.drawInBatch8xOutline(formattedCharSequence, q, p * 10 - 20, n, l, poseStack.last().pose(), multiBufferSource, o);
                continue;
            }
            this.font.drawInBatch(formattedCharSequence, q, (float)(p * 10 - 20), n, false, poseStack.last().pose(), multiBufferSource, false, 0, o);
        }
        poseStack.popPose();
    }

    private static boolean isOutlineVisible(SignBlockEntity signBlockEntity, int i) {
        if (i == DyeColor.BLACK.getTextColor()) {
            return true;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        if (localPlayer != null && minecraft.options.getCameraType().isFirstPerson() && localPlayer.isScoping()) {
            return true;
        }
        Entity entity = minecraft.getCameraEntity();
        return entity != null && entity.distanceToSqr(Vec3.atCenterOf(signBlockEntity.getBlockPos())) < (double)OUTLINE_RENDER_DISTANCE;
    }

    private static int getDarkColor(SignBlockEntity signBlockEntity) {
        int i = signBlockEntity.getColor().getTextColor();
        double d = 0.4;
        int j = (int)((double)NativeImage.getR(i) * 0.4);
        int k = (int)((double)NativeImage.getG(i) * 0.4);
        int l = (int)((double)NativeImage.getB(i) * 0.4);
        if (i == DyeColor.BLACK.getTextColor() && signBlockEntity.hasGlowingText()) {
            return -988212;
        }
        return NativeImage.combine(0, l, k, j);
    }

    public static WoodType getWoodType(Block block) {
        WoodType woodType = block instanceof SignBlock ? ((SignBlock)block).type() : WoodType.OAK;
        return woodType;
    }

    public static SignModel createSignModel(EntityModelSet entityModelSet, WoodType woodType) {
        return new SignModel(entityModelSet.bakeLayer(ModelLayers.createSignModelName(woodType)));
    }

    public static LayerDefinition createSignLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0f, -14.0f, -1.0f, 24.0f, 12.0f, 2.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild(STICK, CubeListBuilder.create().texOffs(0, 14).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 14.0f, 2.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Environment(value=EnvType.CLIENT)
    public static final class SignModel
    extends Model {
        public final ModelPart root;
        public final ModelPart stick;

        public SignModel(ModelPart modelPart) {
            super(RenderType::entityCutoutNoCull);
            this.root = modelPart;
            this.stick = modelPart.getChild(SignRenderer.STICK);
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
            this.root.render(poseStack, vertexConsumer, i, j, f, g, h, k);
        }
    }
}

