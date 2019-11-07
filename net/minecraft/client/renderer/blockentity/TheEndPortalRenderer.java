/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;

@Environment(value=EnvType.CLIENT)
public class TheEndPortalRenderer<T extends TheEndPortalBlockEntity>
extends BlockEntityRenderer<T> {
    public static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
    public static final ResourceLocation END_PORTAL_LOCATION = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random RANDOM = new Random(31100L);

    public TheEndPortalRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);
    }

    @Override
    public void render(T theEndPortalBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        RANDOM.setSeed(31100L);
        double d = ((BlockEntity)theEndPortalBlockEntity).getBlockPos().distSqr(this.renderer.camera.getPosition(), true);
        int k = this.getPasses(d);
        float g = this.getOffset();
        Matrix4f matrix4f = poseStack.last().pose();
        this.renderCube(theEndPortalBlockEntity, g, 0.15f, matrix4f, multiBufferSource.getBuffer(RenderType.endPortal(1)));
        for (int l = 1; l < k; ++l) {
            this.renderCube(theEndPortalBlockEntity, g, 2.0f / (float)(18 - l), matrix4f, multiBufferSource.getBuffer(RenderType.endPortal(l + 1)));
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
        int i = d > 36864.0 ? 1 : (d > 25600.0 ? 3 : (d > 16384.0 ? 5 : (d > 9216.0 ? 7 : (d > 4096.0 ? 9 : (d > 1024.0 ? 11 : (d > 576.0 ? 13 : (d > 256.0 ? 14 : 15)))))));
        return i;
    }

    protected float getOffset() {
        return 0.75f;
    }
}

