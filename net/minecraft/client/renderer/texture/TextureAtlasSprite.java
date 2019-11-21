/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TextureAtlasSprite
implements AutoCloseable {
    private final TextureAtlas atlas;
    private final Info info;
    private final AnimationMetadataSection metadata;
    protected final NativeImage[] mainImage;
    private final int[] framesX;
    private final int[] framesY;
    @Nullable
    private final InterpolationData interpolationData;
    private final int x;
    private final int y;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    private int frame;
    private int subFrame;

    protected TextureAtlasSprite(TextureAtlas textureAtlas, Info info, int i, int j, int k, int l, int m, NativeImage nativeImage) {
        CrashReport crashReport;
        this.atlas = textureAtlas;
        AnimationMetadataSection animationMetadataSection = info.metadata;
        int n = info.width;
        int o = info.height;
        this.x = l;
        this.y = m;
        this.u0 = (float)l / (float)j;
        this.u1 = (float)(l + n) / (float)j;
        this.v0 = (float)m / (float)k;
        this.v1 = (float)(m + o) / (float)k;
        int p = nativeImage.getWidth() / animationMetadataSection.getFrameWidth(n);
        int q = nativeImage.getHeight() / animationMetadataSection.getFrameHeight(o);
        if (animationMetadataSection.getFrameCount() > 0) {
            int r = (Integer)animationMetadataSection.getUniqueFrameIndices().stream().max(Integer::compareTo).get() + 1;
            this.framesX = new int[r];
            this.framesY = new int[r];
            Arrays.fill(this.framesX, -1);
            Arrays.fill(this.framesY, -1);
            for (int s : animationMetadataSection.getUniqueFrameIndices()) {
                int u;
                if (s >= p * q) {
                    throw new RuntimeException("invalid frameindex " + s);
                }
                int t = s / p;
                this.framesX[s] = u = s % p;
                this.framesY[s] = t;
            }
        } else {
            ArrayList<AnimationFrame> list = Lists.newArrayList();
            int v = p * q;
            this.framesX = new int[v];
            this.framesY = new int[v];
            for (int s = 0; s < q; ++s) {
                int t = 0;
                while (t < p) {
                    int u = s * p + t;
                    this.framesX[u] = t++;
                    this.framesY[u] = s;
                    list.add(new AnimationFrame(u, -1));
                }
            }
            animationMetadataSection = new AnimationMetadataSection(list, n, o, animationMetadataSection.getDefaultFrameTime(), animationMetadataSection.isInterpolatedFrames());
        }
        this.info = new Info(info.name, n, o, animationMetadataSection);
        this.metadata = animationMetadataSection;
        try {
            try {
                this.mainImage = MipmapGenerator.generateMipLevels(nativeImage, i);
            } catch (Throwable throwable) {
                crashReport = CrashReport.forThrowable(throwable, "Generating mipmaps for frame");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Frame being iterated");
                crashReportCategory.setDetail("First frame", () -> {
                    StringBuilder stringBuilder = new StringBuilder();
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append(nativeImage.getWidth()).append("x").append(nativeImage.getHeight());
                    return stringBuilder.toString();
                });
                throw new ReportedException(crashReport);
            }
        } catch (Throwable throwable) {
            crashReport = CrashReport.forThrowable(throwable, "Applying mipmap");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Sprite being mipmapped");
            crashReportCategory.setDetail("Sprite name", () -> this.getName().toString());
            crashReportCategory.setDetail("Sprite size", () -> this.getWidth() + " x " + this.getHeight());
            crashReportCategory.setDetail("Sprite frames", () -> this.getFrameCount() + " frames");
            crashReportCategory.setDetail("Mipmap levels", i);
            throw new ReportedException(crashReport);
        }
        this.interpolationData = animationMetadataSection.isInterpolatedFrames() ? new InterpolationData(info, i) : null;
    }

    private void upload(int i) {
        int j = this.framesX[i] * this.info.width;
        int k = this.framesY[i] * this.info.height;
        this.upload(j, k, this.mainImage);
    }

    private void upload(int i, int j, NativeImage[] nativeImages) {
        for (int k = 0; k < this.mainImage.length; ++k) {
            nativeImages[k].upload(k, this.x >> k, this.y >> k, i >> k, j >> k, this.info.width >> k, this.info.height >> k, this.mainImage.length > 1, false);
        }
    }

    public int getWidth() {
        return this.info.width;
    }

    public int getHeight() {
        return this.info.height;
    }

    public float getU0() {
        return this.u0;
    }

    public float getU1() {
        return this.u1;
    }

    public float getU(double d) {
        float f = this.u1 - this.u0;
        return this.u0 + f * (float)d / 16.0f;
    }

    public float getV0() {
        return this.v0;
    }

    public float getV1() {
        return this.v1;
    }

    public float getV(double d) {
        float f = this.v1 - this.v0;
        return this.v0 + f * (float)d / 16.0f;
    }

    public ResourceLocation getName() {
        return this.info.name;
    }

    public TextureAtlas atlas() {
        return this.atlas;
    }

    public int getFrameCount() {
        return this.framesX.length;
    }

    @Override
    public void close() {
        for (NativeImage nativeImage : this.mainImage) {
            if (nativeImage == null) continue;
            nativeImage.close();
        }
        if (this.interpolationData != null) {
            this.interpolationData.close();
        }
    }

    public String toString() {
        int i = this.framesX.length;
        return "TextureAtlasSprite{name='" + this.info.name + '\'' + ", frameCount=" + i + ", x=" + this.x + ", y=" + this.y + ", height=" + this.info.height + ", width=" + this.info.width + ", u0=" + this.u0 + ", u1=" + this.u1 + ", v0=" + this.v0 + ", v1=" + this.v1 + '}';
    }

    public boolean isTransparent(int i, int j, int k) {
        return (this.mainImage[0].getPixelRGBA(j + this.framesX[i] * this.info.width, k + this.framesY[i] * this.info.height) >> 24 & 0xFF) == 0;
    }

    public void uploadFirstFrame() {
        this.upload(0);
    }

    private float atlasSize() {
        float f = (float)this.info.width / (this.u1 - this.u0);
        float g = (float)this.info.height / (this.v1 - this.v0);
        return Math.max(g, f);
    }

    public float uvShrinkRatio() {
        return 4.0f / this.atlasSize();
    }

    public void cycleFrames() {
        ++this.subFrame;
        if (this.subFrame >= this.metadata.getFrameTime(this.frame)) {
            int i = this.metadata.getFrameIndex(this.frame);
            int j = this.metadata.getFrameCount() == 0 ? this.getFrameCount() : this.metadata.getFrameCount();
            this.frame = (this.frame + 1) % j;
            this.subFrame = 0;
            int k = this.metadata.getFrameIndex(this.frame);
            if (i != k && k >= 0 && k < this.getFrameCount()) {
                this.upload(k);
            }
        } else if (this.interpolationData != null) {
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(() -> this.interpolationData.uploadInterpolatedFrame());
            } else {
                this.interpolationData.uploadInterpolatedFrame();
            }
        }
    }

    public boolean isAnimation() {
        return this.metadata.getFrameCount() > 1;
    }

    public VertexConsumer wrap(VertexConsumer vertexConsumer) {
        return new SpriteCoordinateExpander(vertexConsumer, this);
    }

    @Environment(value=EnvType.CLIENT)
    final class InterpolationData
    implements AutoCloseable {
        private final NativeImage[] activeFrame;

        private InterpolationData(Info info, int i) {
            this.activeFrame = new NativeImage[i + 1];
            for (int j = 0; j < this.activeFrame.length; ++j) {
                int k = info.width >> j;
                int l = info.height >> j;
                if (this.activeFrame[j] != null) continue;
                this.activeFrame[j] = new NativeImage(k, l, false);
            }
        }

        private void uploadInterpolatedFrame() {
            double d = 1.0 - (double)TextureAtlasSprite.this.subFrame / (double)TextureAtlasSprite.this.metadata.getFrameTime(TextureAtlasSprite.this.frame);
            int i = TextureAtlasSprite.this.metadata.getFrameIndex(TextureAtlasSprite.this.frame);
            int j = TextureAtlasSprite.this.metadata.getFrameCount() == 0 ? TextureAtlasSprite.this.getFrameCount() : TextureAtlasSprite.this.metadata.getFrameCount();
            int k = TextureAtlasSprite.this.metadata.getFrameIndex((TextureAtlasSprite.this.frame + 1) % j);
            if (i != k && k >= 0 && k < TextureAtlasSprite.this.getFrameCount()) {
                for (int l = 0; l < this.activeFrame.length; ++l) {
                    int m = TextureAtlasSprite.this.info.width >> l;
                    int n = TextureAtlasSprite.this.info.height >> l;
                    for (int o = 0; o < n; ++o) {
                        for (int p = 0; p < m; ++p) {
                            int q = this.getPixel(i, l, p, o);
                            int r = this.getPixel(k, l, p, o);
                            int s = this.mix(d, q >> 16 & 0xFF, r >> 16 & 0xFF);
                            int t = this.mix(d, q >> 8 & 0xFF, r >> 8 & 0xFF);
                            int u = this.mix(d, q & 0xFF, r & 0xFF);
                            this.activeFrame[l].setPixelRGBA(p, o, q & 0xFF000000 | s << 16 | t << 8 | u);
                        }
                    }
                }
                TextureAtlasSprite.this.upload(0, 0, this.activeFrame);
            }
        }

        private int getPixel(int i, int j, int k, int l) {
            return TextureAtlasSprite.this.mainImage[j].getPixelRGBA(k + (TextureAtlasSprite.this.framesX[i] * TextureAtlasSprite.this.info.width >> j), l + (TextureAtlasSprite.this.framesY[i] * TextureAtlasSprite.this.info.height >> j));
        }

        private int mix(double d, int i, int j) {
            return (int)(d * (double)i + (1.0 - d) * (double)j);
        }

        @Override
        public void close() {
            for (NativeImage nativeImage : this.activeFrame) {
                if (nativeImage == null) continue;
                nativeImage.close();
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class Info {
        private final ResourceLocation name;
        private final int width;
        private final int height;
        private final AnimationMetadataSection metadata;

        public Info(ResourceLocation resourceLocation, int i, int j, AnimationMetadataSection animationMetadataSection) {
            this.name = resourceLocation;
            this.width = i;
            this.height = j;
            this.metadata = animationMetadataSection;
        }

        public ResourceLocation name() {
            return this.name;
        }

        public int width() {
            return this.width;
        }

        public int height() {
            return this.height;
        }
    }
}

