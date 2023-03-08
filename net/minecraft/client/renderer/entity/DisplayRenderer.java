/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public abstract class DisplayRenderer<T extends Display>
extends EntityRenderer<T> {
    private static final float MAX_SHADOW_RADIUS = 64.0f;
    private final EntityRenderDispatcher entityRenderDispatcher;

    protected DisplayRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.entityRenderDispatcher = context.getEntityRenderDispatcher();
    }

    @Override
    public ResourceLocation getTextureLocation(T display) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(T display, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        float h = ((Display)display).calculateInterpolationProgress(g);
        this.shadowRadius = Math.min(((Display)display).getShadowRadius(h), 64.0f);
        this.shadowStrength = ((Display)display).getShadowStrength(h);
        int j = ((Display)display).getPackedBrightnessOverride();
        int k = j != -1 ? j : i;
        super.render(display, f, g, poseStack, multiBufferSource, k);
        poseStack.pushPose();
        poseStack.mulPose(this.calculateOrientation(display));
        Transformation transformation = ((Display)display).transformation(h);
        poseStack.mulPoseMatrix(transformation.getMatrix());
        poseStack.last().normal().rotate(transformation.getLeftRotation()).rotate(transformation.getRightRotation());
        this.renderInner(display, poseStack, multiBufferSource, k, h);
        poseStack.popPose();
    }

    private Quaternionf calculateOrientation(T display) {
        Camera camera = this.entityRenderDispatcher.camera;
        return switch (((Display)display).getBillboardConstraints()) {
            default -> throw new IncompatibleClassChangeError();
            case Display.BillboardConstraints.FIXED -> ((Display)display).orientation();
            case Display.BillboardConstraints.HORIZONTAL -> new Quaternionf().rotationYXZ((float)(-Math.PI) / 180 * ((Entity)display).getYRot(), (float)(-Math.PI) / 180 * camera.getXRot(), 0.0f);
            case Display.BillboardConstraints.VERTICAL -> new Quaternionf().rotationYXZ((float)Math.PI - (float)Math.PI / 180 * camera.getYRot(), (float)Math.PI / 180 * ((Entity)display).getXRot(), 0.0f);
            case Display.BillboardConstraints.CENTER -> new Quaternionf().rotationYXZ((float)Math.PI - (float)Math.PI / 180 * camera.getYRot(), (float)(-Math.PI) / 180 * camera.getXRot(), 0.0f);
        };
    }

    protected abstract void renderInner(T var1, PoseStack var2, MultiBufferSource var3, int var4, float var5);

    @Environment(value=EnvType.CLIENT)
    public static class TextDisplayRenderer
    extends DisplayRenderer<Display.TextDisplay> {
        private final Font font;

        protected TextDisplayRenderer(EntityRendererProvider.Context context) {
            super(context);
            this.font = context.getFont();
        }

        private Display.TextDisplay.CachedInfo splitLines(Component component, int i) {
            List<FormattedCharSequence> list = this.font.split(component, i);
            ArrayList<Display.TextDisplay.CachedLine> list2 = new ArrayList<Display.TextDisplay.CachedLine>(list.size());
            int j = 0;
            for (FormattedCharSequence formattedCharSequence : list) {
                int k = this.font.width(formattedCharSequence);
                j = Math.max(j, k);
                list2.add(new Display.TextDisplay.CachedLine(formattedCharSequence, k));
            }
            return new Display.TextDisplay.CachedInfo(list2, j);
        }

        @Override
        public void renderInner(Display.TextDisplay textDisplay, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f) {
            int j;
            float g;
            byte b = textDisplay.getFlags();
            boolean bl = (b & 2) != 0;
            boolean bl2 = (b & 4) != 0;
            boolean bl3 = (b & 1) != 0;
            Display.TextDisplay.Align align = Display.TextDisplay.getAlign(b);
            byte c = textDisplay.getTextOpacity(f);
            if (bl2) {
                g = Minecraft.getInstance().options.getBackgroundOpacity(0.25f);
                j = (int)(g * 255.0f) << 24;
            } else {
                j = textDisplay.getBackgroundColor(f);
            }
            g = 0.0f;
            Matrix4f matrix4f = poseStack.last().pose();
            matrix4f.rotate((float)Math.PI, 0.0f, 1.0f, 0.0f);
            matrix4f.scale(-0.025f, -0.025f, -0.025f);
            Display.TextDisplay.CachedInfo cachedInfo = textDisplay.cacheDisplay(this::splitLines);
            int k = this.font.lineHeight + 1;
            int l = cachedInfo.width();
            int m = cachedInfo.lines().size() * k;
            matrix4f.translate(1.0f - (float)l / 2.0f, -m, 0.0f);
            if (j != 0) {
                VertexConsumer vertexConsumer = multiBufferSource.getBuffer(bl ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
                vertexConsumer.vertex(matrix4f, -1.0f, -1.0f, 0.0f).color(j).uv2(i).endVertex();
                vertexConsumer.vertex(matrix4f, -1.0f, m, 0.0f).color(j).uv2(i).endVertex();
                vertexConsumer.vertex(matrix4f, l, m, 0.0f).color(j).uv2(i).endVertex();
                vertexConsumer.vertex(matrix4f, l, -1.0f, 0.0f).color(j).uv2(i).endVertex();
            }
            for (Display.TextDisplay.CachedLine cachedLine : cachedInfo.lines()) {
                float h = switch (align) {
                    default -> throw new IncompatibleClassChangeError();
                    case Display.TextDisplay.Align.LEFT -> 0.0f;
                    case Display.TextDisplay.Align.RIGHT -> l - cachedLine.width();
                    case Display.TextDisplay.Align.CENTER -> (float)l / 2.0f - (float)cachedLine.width() / 2.0f;
                };
                this.font.drawInBatch(cachedLine.contents(), h, g, c << 24 | 0xFFFFFF, bl3, matrix4f, multiBufferSource, bl ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET, 0, i);
                g += (float)k;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ItemDisplayRenderer
    extends DisplayRenderer<Display.ItemDisplay> {
        private final ItemRenderer itemRenderer;

        protected ItemDisplayRenderer(EntityRendererProvider.Context context) {
            super(context);
            this.itemRenderer = context.getItemRenderer();
        }

        @Override
        public void renderInner(Display.ItemDisplay itemDisplay, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f) {
            this.itemRenderer.renderStatic(itemDisplay.getItemStack(), itemDisplay.getItemTransform(), i, OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, itemDisplay.getLevel(), itemDisplay.getId());
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class BlockDisplayRenderer
    extends DisplayRenderer<Display.BlockDisplay> {
        private final BlockRenderDispatcher blockRenderer;

        protected BlockDisplayRenderer(EntityRendererProvider.Context context) {
            super(context);
            this.blockRenderer = context.getBlockRenderDispatcher();
        }

        @Override
        public void renderInner(Display.BlockDisplay blockDisplay, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f) {
            this.blockRenderer.renderSingleBlock(blockDisplay.getBlockState(), poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
        }
    }
}

