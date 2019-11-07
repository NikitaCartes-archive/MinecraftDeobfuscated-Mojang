/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
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
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f, long l) {
        ClientLevel level = this.minecraft.level;
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        BlockPos blockPos = new BlockPos(d, e, f);
        LongOpenHashSet longSet = new LongOpenHashSet();
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-10, -10, -10), blockPos.offset(10, 10, 10))) {
            int i = level.getBrightness(LightLayer.SKY, blockPos2);
            float g = (float)(15 - i) / 15.0f * 0.5f + 0.16f;
            int j = Mth.hsvToRgb(g, 0.9f, 0.9f);
            long m = SectionPos.blockToSection(blockPos2.asLong());
            if (longSet.add(m)) {
                DebugRenderer.renderFloatingText(((Level)level).getChunkSource().getLightEngine().getDebugData(LightLayer.SKY, SectionPos.of(m)), SectionPos.x(m) * 16 + 8, SectionPos.y(m) * 16 + 8, SectionPos.z(m) * 16 + 8, 0xFF0000, 0.3f);
            }
            if (i == 15) continue;
            DebugRenderer.renderFloatingText(String.valueOf(i), (double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.25, (double)blockPos2.getZ() + 0.5, j);
        }
        RenderSystem.enableTexture();
        RenderSystem.popMatrix();
    }
}

