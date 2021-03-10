/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

@Environment(value=EnvType.CLIENT)
public class WaterDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public WaterDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
        FluidState fluidState;
        BlockPos blockPos = this.minecraft.player.blockPosition();
        Level levelReader = this.minecraft.player.level;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(0.0f, 1.0f, 0.0f, 0.75f);
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(6.0f);
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-10, -10, -10), blockPos.offset(10, 10, 10))) {
            fluidState = levelReader.getFluidState(blockPos2);
            if (!fluidState.is(FluidTags.WATER)) continue;
            double g = (float)blockPos2.getY() + fluidState.getHeight(levelReader, blockPos2);
            DebugRenderer.renderFilledBox(new AABB((float)blockPos2.getX() + 0.01f, (float)blockPos2.getY() + 0.01f, (float)blockPos2.getZ() + 0.01f, (float)blockPos2.getX() + 0.99f, g, (float)blockPos2.getZ() + 0.99f).move(-d, -e, -f), 1.0f, 1.0f, 1.0f, 0.2f);
        }
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-10, -10, -10), blockPos.offset(10, 10, 10))) {
            fluidState = levelReader.getFluidState(blockPos2);
            if (!fluidState.is(FluidTags.WATER)) continue;
            DebugRenderer.renderFloatingText(String.valueOf(fluidState.getAmount()), (double)blockPos2.getX() + 0.5, (float)blockPos2.getY() + fluidState.getHeight(levelReader, blockPos2), (double)blockPos2.getZ() + 0.5, -16777216);
        }
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}

