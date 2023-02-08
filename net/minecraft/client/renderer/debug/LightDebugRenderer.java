/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

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
import net.minecraft.world.level.LightLayer;

@Environment(value=EnvType.CLIENT)
public class LightDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int MAX_RENDER_DIST = 10;

    public LightDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
        ClientLevel level = this.minecraft.level;
        BlockPos blockPos = new BlockPos(d, e, f);
        LongOpenHashSet longSet = new LongOpenHashSet();
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-10, -10, -10), blockPos.offset(10, 10, 10))) {
            int i = level.getBrightness(LightLayer.SKY, blockPos2);
            float g = (float)(15 - i) / 15.0f * 0.5f + 0.16f;
            int j = Mth.hsvToRgb(g, 0.9f, 0.9f);
            long l = SectionPos.blockToSection(blockPos2.asLong());
            if (longSet.add(l)) {
                DebugRenderer.renderFloatingText(poseStack, multiBufferSource, level.getChunkSource().getLightEngine().getDebugData(LightLayer.SKY, SectionPos.of(l)), SectionPos.sectionToBlockCoord(SectionPos.x(l), 8), SectionPos.sectionToBlockCoord(SectionPos.y(l), 8), SectionPos.sectionToBlockCoord(SectionPos.z(l), 8), 0xFF0000, 0.3f);
            }
            if (i == 15) continue;
            DebugRenderer.renderFloatingText(poseStack, multiBufferSource, String.valueOf(i), (double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.25, (double)blockPos2.getZ() + 0.5, j);
        }
    }
}

