/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

@Environment(value=EnvType.CLIENT)
public abstract class BlockEntityRenderer<T extends BlockEntity> {
    protected final BlockEntityRenderDispatcher renderer;

    public BlockEntityRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        this.renderer = blockEntityRenderDispatcher;
    }

    public abstract void render(T var1, double var2, double var4, double var6, float var8, PoseStack var9, MultiBufferSource var10, int var11, int var12);

    protected TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
        return Minecraft.getInstance().getTextureAtlas().getSprite(resourceLocation);
    }

    public boolean shouldRenderOffScreen(T blockEntity) {
        return false;
    }
}

