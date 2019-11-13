/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

@Environment(value=EnvType.CLIENT)
public class BedRenderer
extends BlockEntityRenderer<BedBlockEntity> {
    public static final ResourceLocation[] TEXTURES = (ResourceLocation[])Arrays.stream(DyeColor.values()).sorted(Comparator.comparingInt(DyeColor::getId)).map(dyeColor -> new ResourceLocation("entity/bed/" + dyeColor.getName())).toArray(ResourceLocation[]::new);
    private final ModelPart headPiece;
    private final ModelPart footPiece;
    private final ModelPart[] legs = new ModelPart[4];

    public BedRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);
        this.headPiece = new ModelPart(64, 64, 0, 0);
        this.headPiece.addBox(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 6.0f, 0.0f);
        this.footPiece = new ModelPart(64, 64, 0, 22);
        this.footPiece.addBox(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 6.0f, 0.0f);
        this.legs[0] = new ModelPart(64, 64, 50, 0);
        this.legs[1] = new ModelPart(64, 64, 50, 6);
        this.legs[2] = new ModelPart(64, 64, 50, 12);
        this.legs[3] = new ModelPart(64, 64, 50, 18);
        this.legs[0].addBox(0.0f, 6.0f, -16.0f, 3.0f, 3.0f, 3.0f);
        this.legs[1].addBox(0.0f, 6.0f, 0.0f, 3.0f, 3.0f, 3.0f);
        this.legs[2].addBox(-16.0f, 6.0f, -16.0f, 3.0f, 3.0f, 3.0f);
        this.legs[3].addBox(-16.0f, 6.0f, 0.0f, 3.0f, 3.0f, 3.0f);
        this.legs[0].xRot = 1.5707964f;
        this.legs[1].xRot = 1.5707964f;
        this.legs[2].xRot = 1.5707964f;
        this.legs[3].xRot = 1.5707964f;
        this.legs[0].zRot = 0.0f;
        this.legs[1].zRot = 1.5707964f;
        this.legs[2].zRot = 4.712389f;
        this.legs[3].zRot = (float)Math.PI;
    }

    @Override
    public void render(BedBlockEntity bedBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        ResourceLocation resourceLocation = TEXTURES[bedBlockEntity.getColor().getId()];
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.blockentitySolid());
        if (bedBlockEntity.hasLevel()) {
            BlockState blockState = bedBlockEntity.getBlockState();
            this.renderPiece(poseStack, vertexConsumer, blockState.getValue(BedBlock.PART) == BedPart.HEAD, blockState.getValue(BedBlock.FACING), resourceLocation, i, j, false);
        } else {
            this.renderPiece(poseStack, vertexConsumer, true, Direction.SOUTH, resourceLocation, i, j, false);
            this.renderPiece(poseStack, vertexConsumer, false, Direction.SOUTH, resourceLocation, i, j, true);
        }
    }

    private void renderPiece(PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, Direction direction, ResourceLocation resourceLocation, int i, int j, boolean bl2) {
        this.headPiece.visible = bl;
        this.footPiece.visible = !bl;
        this.legs[0].visible = !bl;
        this.legs[1].visible = bl;
        this.legs[2].visible = !bl;
        this.legs[3].visible = bl;
        poseStack.pushPose();
        poseStack.translate(0.0, 0.5625, bl2 ? -1.0 : 0.0);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0f));
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0f + direction.toYRot()));
        poseStack.translate(-0.5, -0.5, -0.5);
        TextureAtlasSprite textureAtlasSprite = this.getSprite(resourceLocation);
        this.headPiece.render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
        this.footPiece.render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
        this.legs[0].render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
        this.legs[1].render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
        this.legs[2].render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
        this.legs[3].render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
        poseStack.popPose();
    }
}

