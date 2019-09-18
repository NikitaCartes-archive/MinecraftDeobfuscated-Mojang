/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@Environment(value=EnvType.CLIENT)
public abstract class BlockEntityRenderer<T extends BlockEntity> {
    protected static final List<ResourceLocation> BREAKING_LOCATIONS = ModelBakery.DESTROY_STAGES.stream().map(resourceLocation -> new ResourceLocation("textures/" + resourceLocation.getPath() + ".png")).collect(Collectors.toList());
    protected BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    public void setupAndRender(T blockEntity, double d, double e, double f, float g, int i, BufferBuilder bufferBuilder, RenderType renderType, BlockPos blockPos) {
        Lighting.turnOn();
        int j = ((BlockEntity)blockEntity).getLevel().getLightColor(((BlockEntity)blockEntity).getBlockPos());
        int k = j % 65536;
        int l = j / 65536;
        RenderSystem.glMultiTexCoord2f(33985, k, l);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.render(blockEntity, d, e, f, g, i, renderType);
    }

    public abstract void render(T var1, double var2, double var4, double var6, float var8, int var9, RenderType var10);

    protected void renderNameTag(T blockEntity, double d, double e, double f) {
        HitResult hitResult = this.blockEntityRenderDispatcher.cameraHitResult;
        if (blockEntity instanceof Nameable && hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && ((BlockEntity)blockEntity).getBlockPos().equals(((BlockHitResult)hitResult).getBlockPos())) {
            this.setOverlayRenderState(true);
            this.renderNameTag(blockEntity, ((Nameable)blockEntity).getDisplayName().getColoredString(), d, e, f, 12);
            this.setOverlayRenderState(false);
        }
    }

    protected void setOverlayRenderState(boolean bl) {
        RenderSystem.activeTexture(33985);
        if (bl) {
            RenderSystem.disableTexture();
        } else {
            RenderSystem.enableTexture();
        }
        RenderSystem.activeTexture(33984);
    }

    protected void bindTexture(ResourceLocation resourceLocation) {
        TextureManager textureManager = this.blockEntityRenderDispatcher.textureManager;
        if (textureManager != null) {
            textureManager.bind(resourceLocation);
        }
    }

    protected Level getLevel() {
        return this.blockEntityRenderDispatcher.level;
    }

    public void init(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
    }

    public Font getFont() {
        return this.blockEntityRenderDispatcher.getFont();
    }

    public boolean shouldRenderOffScreen(T blockEntity) {
        return false;
    }

    protected void renderNameTag(T blockEntity, String string, double d, double e, double f, int i) {
        Camera camera = this.blockEntityRenderDispatcher.camera;
        double g = ((BlockEntity)blockEntity).distanceToSqr(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
        if (g > (double)(i * i)) {
            return;
        }
        float h = camera.getYRot();
        float j = camera.getXRot();
        GameRenderer.renderNameTagInWorld(this.getFont(), string, (float)d + 0.5f, (float)e + 1.5f, (float)f + 0.5f, 0, h, j, false);
    }
}

