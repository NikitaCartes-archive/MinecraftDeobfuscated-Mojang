/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.VoxelShape;

@Environment(value=EnvType.CLIENT)
public class CollisionBoxRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private double lastUpdateTime = Double.MIN_VALUE;
    private List<VoxelShape> shapes = Collections.emptyList();

    public CollisionBoxRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
        double g = Util.getNanos();
        if (g - this.lastUpdateTime > 1.0E8) {
            this.lastUpdateTime = g;
            Entity entity2 = this.minecraft.gameRenderer.getMainCamera().getEntity();
            this.shapes = entity2.level.getCollisions(entity2, entity2.getBoundingBox().inflate(6.0), entity -> true).collect(Collectors.toList());
        }
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
        for (VoxelShape voxelShape : this.shapes) {
            LevelRenderer.renderVoxelShape(poseStack, vertexConsumer, voxelShape, -d, -e, -f, 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}

