/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Calendar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BatchedBlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
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
extends BatchedBlockEntityRenderer<T> {
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

    public ChestRenderer() {
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
    protected void renderToBuffer(T blockEntity, double d, double e, double f, float g, int i, RenderType renderType, BufferBuilder bufferBuilder, int j, int k) {
        boolean bl;
        ChestType chestType;
        BlockState blockState = ((BlockEntity)blockEntity).hasLevel() ? ((BlockEntity)blockEntity).getBlockState() : (BlockState)Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        ChestType chestType2 = chestType = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        if (chestType == ChestType.LEFT) {
            return;
        }
        boolean bl2 = bl = chestType != ChestType.SINGLE;
        ResourceLocation resourceLocation = i >= 0 ? ModelBakery.DESTROY_STAGES.get(i) : (this.xmasTextures ? (bl ? CHEST_LARGE_XMAS_LOCATION : CHEST_XMAS_LOCATION) : (blockEntity instanceof TrappedChestBlockEntity ? (bl ? CHEST_LARGE_TRAP_LOCATION : CHEST_TRAP_LOCATION) : (blockEntity instanceof EnderChestBlockEntity ? ENDER_CHEST_LOCATION : (bl ? CHEST_LARGE_LOCATION : CHEST_LOCATION))));
        bufferBuilder.pushPose();
        float h = blockState.getValue(ChestBlock.FACING).toYRot();
        bufferBuilder.translate(0.5, 0.5, 0.5);
        bufferBuilder.multiplyPose(new Quaternion(Vector3f.YP, -h, true));
        bufferBuilder.translate(-0.5, -0.5, -0.5);
        float l = ((LidBlockEntity)blockEntity).getOpenNess(g);
        l = 1.0f - l;
        l = 1.0f - l * l * l;
        TextureAtlasSprite textureAtlasSprite = this.getSprite(resourceLocation);
        if (bl) {
            this.render(bufferBuilder, this.doubleLid, this.doubleLock, this.doubleBottom, l, j, k, textureAtlasSprite);
        } else {
            this.render(bufferBuilder, this.lid, this.lock, this.bottom, l, j, k, textureAtlasSprite);
        }
        bufferBuilder.popPose();
    }

    private void render(BufferBuilder bufferBuilder, ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3, float f, int i, int j, TextureAtlasSprite textureAtlasSprite) {
        modelPart2.xRot = modelPart.xRot = -(f * 1.5707964f);
        modelPart.render(bufferBuilder, 0.0625f, i, j, textureAtlasSprite);
        modelPart2.render(bufferBuilder, 0.0625f, i, j, textureAtlasSprite);
        modelPart3.render(bufferBuilder, 0.0625f, i, j, textureAtlasSprite);
    }
}

