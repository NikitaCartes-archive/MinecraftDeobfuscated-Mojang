/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;

@Environment(value=EnvType.CLIENT)
public class RaidDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private Collection<BlockPos> raidCenters = Lists.newArrayList();

    public RaidDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void setRaidCenters(Collection<BlockPos> collection) {
        this.raidCenters = collection;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
        BlockPos blockPos = this.getCamera().getBlockPosition();
        for (BlockPos blockPos2 : this.raidCenters) {
            if (!blockPos.closerThan(blockPos2, 160.0)) continue;
            RaidDebugRenderer.highlightRaidCenter(blockPos2);
        }
    }

    private static void highlightRaidCenter(BlockPos blockPos) {
        DebugRenderer.renderFilledBox(blockPos.offset(-0.5, -0.5, -0.5), blockPos.offset(1.5, 1.5, 1.5), 1.0f, 0.0f, 0.0f, 0.15f);
        int i = -65536;
        RaidDebugRenderer.renderTextOverBlock("Raid center", blockPos, -65536);
    }

    private static void renderTextOverBlock(String string, BlockPos blockPos, int i) {
        double d = (double)blockPos.getX() + 0.5;
        double e = (double)blockPos.getY() + 1.3;
        double f = (double)blockPos.getZ() + 0.5;
        DebugRenderer.renderFloatingText(string, d, e, f, i, 0.04f, true, 0.0f, true);
    }

    private Camera getCamera() {
        return this.minecraft.gameRenderer.getMainCamera();
    }
}

