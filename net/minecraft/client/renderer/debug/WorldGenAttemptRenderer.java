/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;

@Environment(value=EnvType.CLIENT)
public class WorldGenAttemptRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final List<BlockPos> toRender = Lists.newArrayList();
    private final List<Float> scales = Lists.newArrayList();
    private final List<Float> alphas = Lists.newArrayList();
    private final List<Float> reds = Lists.newArrayList();
    private final List<Float> greens = Lists.newArrayList();
    private final List<Float> blues = Lists.newArrayList();

    public void addPos(BlockPos blockPos, float f, float g, float h, float i, float j) {
        this.toRender.add(blockPos);
        this.scales.add(Float.valueOf(f));
        this.alphas.add(Float.valueOf(j));
        this.reds.add(Float.valueOf(g));
        this.greens.add(Float.valueOf(h));
        this.blues.add(Float.valueOf(i));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugFilledBox());
        for (int i = 0; i < this.toRender.size(); ++i) {
            BlockPos blockPos = this.toRender.get(i);
            Float float_ = this.scales.get(i);
            float g = float_.floatValue() / 2.0f;
            LevelRenderer.addChainedFilledBoxVertices(poseStack, vertexConsumer, (double)((float)blockPos.getX() + 0.5f - g) - d, (double)((float)blockPos.getY() + 0.5f - g) - e, (double)((float)blockPos.getZ() + 0.5f - g) - f, (double)((float)blockPos.getX() + 0.5f + g) - d, (double)((float)blockPos.getY() + 0.5f + g) - e, (double)((float)blockPos.getZ() + 0.5f + g) - f, this.reds.get(i).floatValue(), this.greens.get(i).floatValue(), this.blues.get(i).floatValue(), this.alphas.get(i).floatValue());
        }
    }
}

