/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.Calendar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

@Environment(value=EnvType.CLIENT)
public class ChestRenderer<T extends BlockEntity>
extends BlockEntityRenderer<T> {
    public static final ResourceLocation CHEST_TRAP_LOCATION = new ResourceLocation("entity/chest/trapped");
    public static final ResourceLocation CHEST_TRAP_LOCATION_LEFT = new ResourceLocation("entity/chest/trapped_left");
    public static final ResourceLocation CHEST_TRAP_LOCATION_RIGHT = new ResourceLocation("entity/chest/trapped_right");
    public static final ResourceLocation CHEST_XMAS_LOCATION = new ResourceLocation("entity/chest/christmas");
    public static final ResourceLocation CHEST_XMAS_LOCATION_LEFT = new ResourceLocation("entity/chest/christmas_left");
    public static final ResourceLocation CHEST_XMAS_LOCATION_RIGHT = new ResourceLocation("entity/chest/christmas_right");
    public static final ResourceLocation CHEST_LOCATION = new ResourceLocation("entity/chest/normal");
    public static final ResourceLocation CHEST_LOCATION_LEFT = new ResourceLocation("entity/chest/normal_left");
    public static final ResourceLocation CHEST_LOCATION_RIGHT = new ResourceLocation("entity/chest/normal_right");
    public static final ResourceLocation ENDER_CHEST_LOCATION = new ResourceLocation("entity/chest/ender");
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
        boolean bl;
        BlockState blockState = ((BlockEntity)blockEntity).hasLevel() ? ((BlockEntity)blockEntity).getBlockState() : (BlockState)Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        ChestType chestType = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        boolean bl2 = bl = chestType != ChestType.SINGLE;
        ResourceLocation resourceLocation = this.xmasTextures ? this.chooseTexture(chestType, CHEST_XMAS_LOCATION, CHEST_XMAS_LOCATION_LEFT, CHEST_XMAS_LOCATION_RIGHT) : (blockEntity instanceof TrappedChestBlockEntity ? this.chooseTexture(chestType, CHEST_TRAP_LOCATION, CHEST_TRAP_LOCATION_LEFT, CHEST_TRAP_LOCATION_RIGHT) : (blockEntity instanceof EnderChestBlockEntity ? ENDER_CHEST_LOCATION : this.chooseTexture(chestType, CHEST_LOCATION, CHEST_LOCATION_LEFT, CHEST_LOCATION_RIGHT)));
        poseStack.pushPose();
        float g = blockState.getValue(ChestBlock.FACING).toYRot();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-g));
        poseStack.translate(-0.5, -0.5, -0.5);
        float h = ((LidBlockEntity)blockEntity).getOpenNess(f);
        h = 1.0f - h;
        h = 1.0f - h * h * h;
        TextureAtlasSprite textureAtlasSprite = this.getSprite(resourceLocation);
        if (bl) {
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
            if (chestType == ChestType.LEFT) {
                this.render(poseStack, vertexConsumer, this.doubleRightLid, this.doubleRightLock, this.doubleRightBottom, h, i, j, textureAtlasSprite);
            } else {
                this.render(poseStack, vertexConsumer, this.doubleLeftLid, this.doubleLeftLock, this.doubleLeftBottom, h, i, j, textureAtlasSprite);
            }
        } else {
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
            this.render(poseStack, vertexConsumer, this.lid, this.lock, this.bottom, h, i, j, textureAtlasSprite);
        }
        poseStack.popPose();
    }

    private ResourceLocation chooseTexture(ChestType chestType, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3) {
        switch (chestType) {
            case LEFT: {
                return resourceLocation3;
            }
            case RIGHT: {
                return resourceLocation2;
            }
        }
        return resourceLocation;
    }

    private void render(PoseStack poseStack, VertexConsumer vertexConsumer, ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3, float f, int i, int j, TextureAtlasSprite textureAtlasSprite) {
        modelPart2.xRot = modelPart.xRot = -(f * 1.5707964f);
        modelPart.render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
        modelPart2.render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
        modelPart3.render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
    }
}

