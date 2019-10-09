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
    public static final ResourceLocation CHEST_LARGE_TRAP_LOCATION = new ResourceLocation("entity/chest/trapped_double");
    public static final ResourceLocation CHEST_LARGE_XMAS_LOCATION = new ResourceLocation("entity/chest/christmas_double");
    public static final ResourceLocation CHEST_LARGE_LOCATION = new ResourceLocation("entity/chest/normal_double");
    public static final ResourceLocation CHEST_TRAP_LOCATION = new ResourceLocation("entity/chest/trapped");
    public static final ResourceLocation CHEST_XMAS_LOCATION = new ResourceLocation("entity/chest/christmas");
    public static final ResourceLocation CHEST_LOCATION = new ResourceLocation("entity/chest/normal");
    public static final ResourceLocation ENDER_CHEST_LOCATION = new ResourceLocation("entity/chest/ender");
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final ModelPart doubleLid;
    private final ModelPart doubleBottom;
    private final ModelPart doubleLock;
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
        this.lock.addBox(7.0f, -2.0f, 15.0f, 2.0f, 4.0f, 1.0f, 0.0f);
        this.lock.y = 9.0f;
        this.doubleBottom = new ModelPart(128, 64, 0, 19);
        this.doubleBottom.addBox(1.0f, 0.0f, 1.0f, 30.0f, 10.0f, 14.0f, 0.0f);
        this.doubleLid = new ModelPart(128, 64, 0, 0);
        this.doubleLid.addBox(1.0f, 0.0f, 0.0f, 30.0f, 5.0f, 14.0f, 0.0f);
        this.doubleLid.y = 9.0f;
        this.doubleLid.z = 1.0f;
        this.doubleLock = new ModelPart(128, 64, 0, 0);
        this.doubleLock.addBox(15.0f, -2.0f, 15.0f, 2.0f, 4.0f, 1.0f, 0.0f);
        this.doubleLock.y = 9.0f;
    }

    @Override
    public void render(T blockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        boolean bl;
        BlockState blockState = ((BlockEntity)blockEntity).hasLevel() ? ((BlockEntity)blockEntity).getBlockState() : (BlockState)Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        ChestType chestType = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        boolean bl2 = bl = chestType != ChestType.SINGLE;
        ResourceLocation resourceLocation = this.xmasTextures ? (bl ? CHEST_LARGE_XMAS_LOCATION : CHEST_XMAS_LOCATION) : (blockEntity instanceof TrappedChestBlockEntity ? (bl ? CHEST_LARGE_TRAP_LOCATION : CHEST_TRAP_LOCATION) : (blockEntity instanceof EnderChestBlockEntity ? ENDER_CHEST_LOCATION : (bl ? CHEST_LARGE_LOCATION : CHEST_LOCATION)));
        poseStack.pushPose();
        float h = blockState.getValue(ChestBlock.FACING).toYRot();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-h));
        poseStack.translate(-0.5, -0.5, -0.5);
        float k = ((LidBlockEntity)blockEntity).getOpenNess(g);
        k = 1.0f - k;
        k = 1.0f - k * k * k;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        TextureAtlasSprite textureAtlasSprite = this.getSprite(resourceLocation);
        if (bl) {
            if (chestType == ChestType.LEFT) {
                poseStack.translate(-1.0, 0.0, 0.0);
            }
            this.render(poseStack, vertexConsumer, this.doubleLid, this.doubleLock, this.doubleBottom, k, i, j, textureAtlasSprite);
        } else {
            this.render(poseStack, vertexConsumer, this.lid, this.lock, this.bottom, k, i, j, textureAtlasSprite);
        }
        poseStack.popPose();
    }

    private void render(PoseStack poseStack, VertexConsumer vertexConsumer, ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3, float f, int i, int j, TextureAtlasSprite textureAtlasSprite) {
        modelPart2.xRot = modelPart.xRot = -(f * 1.5707964f);
        modelPart.render(poseStack, vertexConsumer, 0.0625f, i, j, textureAtlasSprite);
        modelPart2.render(poseStack, vertexConsumer, 0.0625f, i, j, textureAtlasSprite);
        modelPart3.render(poseStack, vertexConsumer, 0.0625f, i, j, textureAtlasSprite);
    }
}

