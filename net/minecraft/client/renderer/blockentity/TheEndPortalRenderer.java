/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;

@Environment(value=EnvType.CLIENT)
public class TheEndPortalRenderer<T extends TheEndPortalBlockEntity>
implements BlockEntityRenderer<T> {
    public static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
    public static final ResourceLocation END_PORTAL_LOCATION = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random RANDOM = new Random(31100L);
    private static final List<RenderType> RENDER_TYPES = IntStream.range(0, 16).mapToObj(i -> RenderType.endPortal(i + 1)).collect(ImmutableList.toImmutableList());
    private final BlockEntityRenderDispatcher renderer;

    public TheEndPortalRenderer(BlockEntityRendererProvider.Context context) {
        this.renderer = context.getBlockEntityRenderDispatcher();
    }

    @Override
    public void render(T theEndPortalBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        RANDOM.setSeed(31100L);
        double d = ((BlockEntity)theEndPortalBlockEntity).getBlockPos().distSqr(this.renderer.camera.getPosition(), true);
        int k = this.getPasses(d);
        float g = this.getOffset();
        Matrix4f matrix4f = poseStack.last().pose();
        this.renderCube(theEndPortalBlockEntity, g, 0.15f, matrix4f, multiBufferSource.getBuffer(RENDER_TYPES.get(0)));
        for (int l = 1; l < k; ++l) {
            this.renderCube(theEndPortalBlockEntity, g, 2.0f / (float)(18 - l), matrix4f, multiBufferSource.getBuffer(RENDER_TYPES.get(l)));
        }
    }

    private void renderCube(T theEndPortalBlockEntity, float f, float g, Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        float h = (RANDOM.nextFloat() * 0.5f + 0.1f) * g;
        float i = (RANDOM.nextFloat() * 0.5f + 0.4f) * g;
        float j = (RANDOM.nextFloat() * 0.5f + 0.5f) * g;
        this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, h, i, j, Direction.SOUTH);
        this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, h, i, j, Direction.NORTH);
        this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, h, i, j, Direction.EAST);
        this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, h, i, j, Direction.WEST);
        this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, h, i, j, Direction.DOWN);
        this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 0.0f, 1.0f, f, f, 1.0f, 1.0f, 0.0f, 0.0f, h, i, j, Direction.UP);
    }

    private void renderFace(T theEndPortalBlockEntity, Matrix4f matrix4f, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, float n, float o, float p, Direction direction) {
        if (((TheEndPortalBlockEntity)theEndPortalBlockEntity).shouldRenderFace(direction)) {
            vertexConsumer.vertex(matrix4f, f, h, j).color(n, o, p, 1.0f).endVertex();
            vertexConsumer.vertex(matrix4f, g, h, k).color(n, o, p, 1.0f).endVertex();
            vertexConsumer.vertex(matrix4f, g, i, l).color(n, o, p, 1.0f).endVertex();
            vertexConsumer.vertex(matrix4f, f, i, m).color(n, o, p, 1.0f).endVertex();
        }
    }

    protected int getPasses(double d) {
        if (d > 36864.0) {
            return 1;
        }
        if (d > 25600.0) {
            return 3;
        }
        if (d > 16384.0) {
            return 5;
        }
        if (d > 9216.0) {
            return 7;
        }
        if (d > 4096.0) {
            return 9;
        }
        if (d > 1024.0) {
            return 11;
        }
        if (d > 576.0) {
            return 13;
        }
        if (d > 256.0) {
            return 14;
        }
        return 15;
    }

    protected float getOffset() {
        return 0.75f;
    }
}

