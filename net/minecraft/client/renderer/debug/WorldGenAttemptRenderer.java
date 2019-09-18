/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;

@Environment(value=EnvType.CLIENT)
public class WorldGenAttemptRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final List<BlockPos> toRender = Lists.newArrayList();
    private final List<Float> scales = Lists.newArrayList();
    private final List<Float> alphas = Lists.newArrayList();
    private final List<Float> reds = Lists.newArrayList();
    private final List<Float> greens = Lists.newArrayList();
    private final List<Float> blues = Lists.newArrayList();

    public WorldGenAttemptRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void addPos(BlockPos blockPos, float f, float g, float h, float i, float j) {
        this.toRender.add(blockPos);
        this.scales.add(Float.valueOf(f));
        this.alphas.add(Float.valueOf(j));
        this.reds.add(Float.valueOf(g));
        this.greens.add(Float.valueOf(h));
        this.blues.add(Float.valueOf(i));
    }
}

