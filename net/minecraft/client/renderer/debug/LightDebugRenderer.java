/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

@Environment(value=EnvType.CLIENT)
public class LightDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public LightDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(long l) {
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        MultiPlayerLevel level = this.minecraft.level;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture();
        BlockPos blockPos = new BlockPos(camera.getPosition());
        LongOpenHashSet longSet = new LongOpenHashSet();
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-10, -10, -10), blockPos.offset(10, 10, 10))) {
            int i = level.getBrightness(LightLayer.SKY, blockPos2);
            float f = (float)(15 - i) / 15.0f * 0.5f + 0.16f;
            int j = Mth.hsvToRgb(f, 0.9f, 0.9f);
            long m = SectionPos.blockToSection(blockPos2.asLong());
            if (longSet.add(m)) {
                DebugRenderer.renderFloatingText(((Level)level).getChunkSource().getLightEngine().getDebugData(LightLayer.SKY, SectionPos.of(m)), SectionPos.x(m) * 16 + 8, SectionPos.y(m) * 16 + 8, SectionPos.z(m) * 16 + 8, 0xFF0000, 0.3f);
            }
            if (i == 15) continue;
            DebugRenderer.renderFloatingText(String.valueOf(i), (double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.25, (double)blockPos2.getZ() + 0.5, j);
        }
        GlStateManager.enableTexture();
        GlStateManager.popMatrix();
    }
}

