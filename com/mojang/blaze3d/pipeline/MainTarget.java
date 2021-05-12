/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.pipeline;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class MainTarget
extends RenderTarget {
    public static final int DEFAULT_WIDTH = 854;
    public static final int DEFAULT_HEIGHT = 480;
    static final Dimension DEFAULT_DIMENSIONS = new Dimension(854, 480);

    public MainTarget(int i, int j) {
        super(true);
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this.createFrameBuffer(i, j));
        } else {
            this.createFrameBuffer(i, j);
        }
    }

    private void createFrameBuffer(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        Dimension dimension = this.allocateAttachments(i, j);
        this.frameBufferId = GlStateManager.glGenFramebuffers();
        GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
        GlStateManager._bindTexture(this.colorTextureId);
        GlStateManager._texParameter(3553, 10241, 9728);
        GlStateManager._texParameter(3553, 10240, 9728);
        GlStateManager._texParameter(3553, 10242, 33071);
        GlStateManager._texParameter(3553, 10243, 33071);
        GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, this.colorTextureId, 0);
        GlStateManager._bindTexture(this.depthBufferId);
        GlStateManager._texParameter(3553, 34892, 0);
        GlStateManager._texParameter(3553, 10241, 9728);
        GlStateManager._texParameter(3553, 10240, 9728);
        GlStateManager._texParameter(3553, 10242, 33071);
        GlStateManager._texParameter(3553, 10243, 33071);
        GlStateManager._glFramebufferTexture2D(36160, 36096, 3553, this.depthBufferId, 0);
        GlStateManager._bindTexture(0);
        this.viewWidth = dimension.width;
        this.viewHeight = dimension.height;
        this.width = dimension.width;
        this.height = dimension.height;
        this.checkStatus();
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    private Dimension allocateAttachments(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        this.colorTextureId = TextureUtil.generateTextureId();
        this.depthBufferId = TextureUtil.generateTextureId();
        AttachmentState attachmentState = AttachmentState.NONE;
        for (Dimension dimension : Dimension.listWithFallback(i, j)) {
            attachmentState = AttachmentState.NONE;
            if (this.allocateColorAttachment(dimension)) {
                attachmentState = attachmentState.with(AttachmentState.COLOR);
            }
            if (this.allocateDepthAttachment(dimension)) {
                attachmentState = attachmentState.with(AttachmentState.DEPTH);
            }
            if (attachmentState != AttachmentState.COLOR_DEPTH) continue;
            return dimension;
        }
        throw new RuntimeException("Unrecoverable GL_OUT_OF_MEMORY (allocated attachments = " + attachmentState.name() + ")");
    }

    private boolean allocateColorAttachment(Dimension dimension) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager._getError();
        GlStateManager._bindTexture(this.colorTextureId);
        GlStateManager._texImage2D(3553, 0, 32856, dimension.width, dimension.height, 0, 6408, 5121, null);
        return GlStateManager._getError() != 1285;
    }

    private boolean allocateDepthAttachment(Dimension dimension) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager._getError();
        GlStateManager._bindTexture(this.depthBufferId);
        GlStateManager._texImage2D(3553, 0, 6402, dimension.width, dimension.height, 0, 6402, 5126, null);
        return GlStateManager._getError() != 1285;
    }

    @Environment(value=EnvType.CLIENT)
    static class Dimension {
        public final int width;
        public final int height;

        Dimension(int i, int j) {
            this.width = i;
            this.height = j;
        }

        static List<Dimension> listWithFallback(int i, int j) {
            RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
            int k = RenderSystem.maxSupportedTextureSize();
            if (i <= 0 || i > k || j <= 0 || j > k) {
                return ImmutableList.of(DEFAULT_DIMENSIONS);
            }
            return ImmutableList.of(new Dimension(i, j), DEFAULT_DIMENSIONS);
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            Dimension dimension = (Dimension)object;
            return this.width == dimension.width && this.height == dimension.height;
        }

        public int hashCode() {
            return Objects.hash(this.width, this.height);
        }

        public String toString() {
            return this.width + "x" + this.height;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum AttachmentState {
        NONE,
        COLOR,
        DEPTH,
        COLOR_DEPTH;

        private static final AttachmentState[] VALUES;

        AttachmentState with(AttachmentState attachmentState) {
            return VALUES[this.ordinal() | attachmentState.ordinal()];
        }

        static {
            VALUES = AttachmentState.values();
        }
    }
}

