/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Calendar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.LargeChestModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
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
    private static final ResourceLocation CHEST_LARGE_TRAP_LOCATION = new ResourceLocation("textures/entity/chest/trapped_double.png");
    private static final ResourceLocation CHEST_LARGE_XMAS_LOCATION = new ResourceLocation("textures/entity/chest/christmas_double.png");
    private static final ResourceLocation CHEST_LARGE_LOCATION = new ResourceLocation("textures/entity/chest/normal_double.png");
    private static final ResourceLocation CHEST_TRAP_LOCATION = new ResourceLocation("textures/entity/chest/trapped.png");
    private static final ResourceLocation CHEST_XMAS_LOCATION = new ResourceLocation("textures/entity/chest/christmas.png");
    private static final ResourceLocation CHEST_LOCATION = new ResourceLocation("textures/entity/chest/normal.png");
    private static final ResourceLocation ENDER_CHEST_LOCATION = new ResourceLocation("textures/entity/chest/ender.png");
    private final ChestModel chestModel = new ChestModel();
    private final ChestModel largeChestModel = new LargeChestModel();
    private boolean xmasTextures;

    public ChestRenderer() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) {
            this.xmasTextures = true;
        }
    }

    @Override
    public void render(T blockEntity, double d, double e, double f, float g, int i) {
        ChestType chestType;
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(515);
        RenderSystem.depthMask(true);
        BlockState blockState = ((BlockEntity)blockEntity).hasLevel() ? ((BlockEntity)blockEntity).getBlockState() : (BlockState)Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        ChestType chestType2 = chestType = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        if (chestType == ChestType.LEFT) {
            return;
        }
        boolean bl = chestType != ChestType.SINGLE;
        ChestModel chestModel = this.getChestModelAndBindTexture(blockEntity, i, bl);
        if (i >= 0) {
            RenderSystem.matrixMode(5890);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(bl ? 8.0f : 4.0f, 4.0f, 1.0f);
            RenderSystem.translatef(0.0625f, 0.0625f, 0.0625f);
            RenderSystem.matrixMode(5888);
        } else {
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        }
        RenderSystem.pushMatrix();
        RenderSystem.enableRescaleNormal();
        RenderSystem.translatef((float)d, (float)e + 1.0f, (float)f + 1.0f);
        RenderSystem.scalef(1.0f, -1.0f, -1.0f);
        float h = blockState.getValue(ChestBlock.FACING).toYRot();
        if ((double)Math.abs(h) > 1.0E-5) {
            RenderSystem.translatef(0.5f, 0.5f, 0.5f);
            RenderSystem.rotatef(h, 0.0f, 1.0f, 0.0f);
            RenderSystem.translatef(-0.5f, -0.5f, -0.5f);
        }
        this.rotateLid(blockEntity, g, chestModel);
        chestModel.render();
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        if (i >= 0) {
            RenderSystem.matrixMode(5890);
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(5888);
        }
    }

    private ChestModel getChestModelAndBindTexture(T blockEntity, int i, boolean bl) {
        ResourceLocation resourceLocation = i >= 0 ? BREAKING_LOCATIONS[i] : (this.xmasTextures ? (bl ? CHEST_LARGE_XMAS_LOCATION : CHEST_XMAS_LOCATION) : (blockEntity instanceof TrappedChestBlockEntity ? (bl ? CHEST_LARGE_TRAP_LOCATION : CHEST_TRAP_LOCATION) : (blockEntity instanceof EnderChestBlockEntity ? ENDER_CHEST_LOCATION : (bl ? CHEST_LARGE_LOCATION : CHEST_LOCATION))));
        this.bindTexture(resourceLocation);
        return bl ? this.largeChestModel : this.chestModel;
    }

    private void rotateLid(T blockEntity, float f, ChestModel chestModel) {
        float g = ((LidBlockEntity)blockEntity).getOpenNess(f);
        g = 1.0f - g;
        g = 1.0f - g * g * g;
        chestModel.getLid().xRot = -(g * 1.5707964f);
    }
}

