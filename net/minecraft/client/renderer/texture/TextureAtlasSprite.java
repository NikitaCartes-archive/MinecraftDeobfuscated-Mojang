/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TextureAtlasSprite
implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final TextureAtlas atlas;
    private final ResourceLocation name;
    private final int width;
    private final int height;
    protected final NativeImage[] mainImage;
    @Nullable
    private final AnimatedTexture animatedTexture;
    private final int x;
    private final int y;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;

    protected TextureAtlasSprite(TextureAtlas textureAtlas, Info info, int i, int j, int k, int l, int m, NativeImage nativeImage) {
        this.atlas = textureAtlas;
        this.width = info.width;
        this.height = info.height;
        this.name = info.name;
        this.x = l;
        this.y = m;
        this.u0 = (float)l / (float)j;
        this.u1 = (float)(l + this.width) / (float)j;
        this.v0 = (float)m / (float)k;
        this.v1 = (float)(m + this.height) / (float)k;
        this.animatedTexture = this.createTicker(info, nativeImage.getWidth(), nativeImage.getHeight(), i);
        try {
            try {
                this.mainImage = MipmapGenerator.generateMipLevels(nativeImage, i);
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Generating mipmaps for frame");
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
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Applying mipmap");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Sprite being mipmapped");
            crashReportCategory.setDetail("Sprite name", this.name::toString);
            crashReportCategory.setDetail("Sprite size", () -> this.width + " x " + this.height);
            crashReportCategory.setDetail("Sprite frames", () -> this.getFrameCount() + " frames");
            crashReportCategory.setDetail("Mipmap levels", i);
            throw new ReportedException(crashReport);
        }
    }

    private int getFrameCount() {
        return this.animatedTexture != null ? this.animatedTexture.frames.size() : 1;
    }

    @Nullable
    private AnimatedTexture createTicker(Info info, int i2, int j2, int k) {
        int o;
        AnimationMetadataSection animationMetadataSection = info.metadata;
        int l = i2 / animationMetadataSection.getFrameWidth(info.width);
        int m = j2 / animationMetadataSection.getFrameHeight(info.height);
        int n = l * m;
        ArrayList<FrameInfo> list = Lists.newArrayList();
        animationMetadataSection.forEachFrame((i, j) -> list.add(new FrameInfo(i, j)));
        if (list.isEmpty()) {
            for (o = 0; o < n; ++o) {
                list.add(new FrameInfo(o, animationMetadataSection.getDefaultFrameTime()));
            }
        } else {
            o = 0;
            IntOpenHashSet intSet = new IntOpenHashSet();
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                FrameInfo frameInfo = (FrameInfo)iterator.next();
                boolean bl = true;
                if (frameInfo.time <= 0) {
                    LOGGER.warn("Invalid frame duration on sprite {} frame {}: {}", (Object)this.name, (Object)o, (Object)frameInfo.time);
                    bl = false;
                }
                if (frameInfo.index < 0 || frameInfo.index >= n) {
                    LOGGER.warn("Invalid frame index on sprite {} frame {}: {}", (Object)this.name, (Object)o, (Object)frameInfo.index);
                    bl = false;
                }
                if (bl) {
                    intSet.add(frameInfo.index);
                } else {
                    iterator.remove();
                }
                ++o;
            }
            int[] is = IntStream.range(0, n).filter(i -> !intSet.contains(i)).toArray();
            if (is.length > 0) {
                LOGGER.warn("Unused frames in sprite {}: {}", (Object)this.name, (Object)Arrays.toString(is));
            }
        }
        if (list.size() <= 1) {
            return null;
        }
        InterpolationData interpolationData = animationMetadataSection.isInterpolatedFrames() ? new InterpolationData(info, k) : null;
        return new AnimatedTexture(ImmutableList.copyOf(list), l, interpolationData);
    }

    private void upload(int i, int j, NativeImage[] nativeImages) {
        for (int k = 0; k < this.mainImage.length; ++k) {
            nativeImages[k].upload(k, this.x >> k, this.y >> k, i >> k, j >> k, this.width >> k, this.height >> k, this.mainImage.length > 1, false);
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
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
        return this.name;
    }

    public TextureAtlas atlas() {
        return this.atlas;
    }

    public IntStream getUniqueFrames() {
        return this.animatedTexture != null ? this.animatedTexture.getUniqueFrames() : IntStream.of(1);
    }

    @Override
    public void close() {
        for (NativeImage nativeImage : this.mainImage) {
            if (nativeImage == null) continue;
            nativeImage.close();
        }
        if (this.animatedTexture != null) {
            this.animatedTexture.close();
        }
    }

    public String toString() {
        return "TextureAtlasSprite{name='" + this.name + '\'' + ", frameCount=" + this.getFrameCount() + ", x=" + this.x + ", y=" + this.y + ", height=" + this.height + ", width=" + this.width + ", u0=" + this.u0 + ", u1=" + this.u1 + ", v0=" + this.v0 + ", v1=" + this.v1 + '}';
    }

    public boolean isTransparent(int i, int j, int k) {
        int l = j;
        int m = k;
        if (this.animatedTexture != null) {
            l += this.animatedTexture.getFrameX(i) * this.width;
            m += this.animatedTexture.getFrameY(i) * this.height;
        }
        return (this.mainImage[0].getPixelRGBA(l, m) >> 24 & 0xFF) == 0;
    }

    public void uploadFirstFrame() {
        if (this.animatedTexture != null) {
            this.animatedTexture.uploadFirstFrame();
        } else {
            this.upload(0, 0, this.mainImage);
        }
    }

    private float atlasSize() {
        float f = (float)this.width / (this.u1 - this.u0);
        float g = (float)this.height / (this.v1 - this.v0);
        return Math.max(g, f);
    }

    public float uvShrinkRatio() {
        return 4.0f / this.atlasSize();
    }

    @Nullable
    public Tickable getAnimationTicker() {
        return this.animatedTexture;
    }

    public VertexConsumer wrap(VertexConsumer vertexConsumer) {
        return new SpriteCoordinateExpander(vertexConsumer, this);
    }

    @Environment(value=EnvType.CLIENT)
    class AnimatedTexture
    implements Tickable,
    AutoCloseable {
        private int frame;
        private int subFrame;
        private final List<FrameInfo> frames;
        private final int frameRowSize;
        @Nullable
        private final InterpolationData interpolationData;

        private AnimatedTexture(List<FrameInfo> list, @Nullable int i, InterpolationData interpolationData) {
            this.frames = list;
            this.frameRowSize = i;
            this.interpolationData = interpolationData;
        }

        private int getFrameX(int i) {
            return i % this.frameRowSize;
        }

        private int getFrameY(int i) {
            return i / this.frameRowSize;
        }

        private void uploadFrame(int i) {
            int j = this.getFrameX(i) * TextureAtlasSprite.this.width;
            int k = this.getFrameY(i) * TextureAtlasSprite.this.height;
            TextureAtlasSprite.this.upload(j, k, TextureAtlasSprite.this.mainImage);
        }

        @Override
        public void close() {
            if (this.interpolationData != null) {
                this.interpolationData.close();
            }
        }

        @Override
        public void tick() {
            ++this.subFrame;
            FrameInfo frameInfo = this.frames.get(this.frame);
            if (this.subFrame >= frameInfo.time) {
                int i = frameInfo.index;
                this.frame = (this.frame + 1) % this.frames.size();
                this.subFrame = 0;
                int j = this.frames.get(this.frame).index;
                if (i != j) {
                    this.uploadFrame(j);
                }
            } else if (this.interpolationData != null) {
                if (!RenderSystem.isOnRenderThread()) {
                    RenderSystem.recordRenderCall(() -> this.interpolationData.uploadInterpolatedFrame(this));
                } else {
                    this.interpolationData.uploadInterpolatedFrame(this);
                }
            }
        }

        public void uploadFirstFrame() {
            this.uploadFrame(this.frames.get(0).index);
        }

        public IntStream getUniqueFrames() {
            return this.frames.stream().mapToInt(frameInfo -> ((FrameInfo)frameInfo).index).distinct();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class FrameInfo {
        private final int index;
        private final int time;

        private FrameInfo(int i, int j) {
            this.index = i;
            this.time = j;
        }
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

        private void uploadInterpolatedFrame(AnimatedTexture animatedTexture) {
            int j;
            FrameInfo frameInfo = (FrameInfo)animatedTexture.frames.get(animatedTexture.frame);
            double d = 1.0 - (double)animatedTexture.subFrame / (double)frameInfo.time;
            int i = frameInfo.index;
            if (i != (j = ((FrameInfo)animatedTexture.frames.get((animatedTexture.frame + 1) % animatedTexture.frames.size())).index)) {
                for (int k = 0; k < this.activeFrame.length; ++k) {
                    int l = TextureAtlasSprite.this.width >> k;
                    int m = TextureAtlasSprite.this.height >> k;
                    for (int n = 0; n < m; ++n) {
                        for (int o = 0; o < l; ++o) {
                            int p = this.getPixel(animatedTexture, i, k, o, n);
                            int q = this.getPixel(animatedTexture, j, k, o, n);
                            int r = this.mix(d, p >> 16 & 0xFF, q >> 16 & 0xFF);
                            int s = this.mix(d, p >> 8 & 0xFF, q >> 8 & 0xFF);
                            int t = this.mix(d, p & 0xFF, q & 0xFF);
                            this.activeFrame[k].setPixelRGBA(o, n, p & 0xFF000000 | r << 16 | s << 8 | t);
                        }
                    }
                }
                TextureAtlasSprite.this.upload(0, 0, this.activeFrame);
            }
        }

        private int getPixel(AnimatedTexture animatedTexture, int i, int j, int k, int l) {
            return TextureAtlasSprite.this.mainImage[j].getPixelRGBA(k + (animatedTexture.getFrameX(i) * TextureAtlasSprite.this.width >> j), l + (animatedTexture.getFrameY(i) * TextureAtlasSprite.this.height >> j));
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

