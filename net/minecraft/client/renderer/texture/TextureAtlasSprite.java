/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.PngInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TextureAtlasSprite {
    private final ResourceLocation name;
    protected final int width;
    protected final int height;
    protected NativeImage[] mainImage;
    @Nullable
    protected int[] framesX;
    @Nullable
    protected int[] framesY;
    protected NativeImage[] activeFrame;
    private AnimationMetadataSection metadata;
    protected int x;
    protected int y;
    private float u0;
    private float u1;
    private float v0;
    private float v1;
    protected int frame;
    protected int subFrame;
    private static final float[] POW22 = Util.make(new float[256], fs -> {
        for (int i = 0; i < ((float[])fs).length; ++i) {
            fs[i] = (float)Math.pow((float)i / 255.0f, 2.2);
        }
    });

    protected TextureAtlasSprite(ResourceLocation resourceLocation, int i, int j) {
        this.name = resourceLocation;
        this.width = i;
        this.height = j;
    }

    protected TextureAtlasSprite(ResourceLocation resourceLocation, PngInfo pngInfo, @Nullable AnimationMetadataSection animationMetadataSection) {
        this.name = resourceLocation;
        if (animationMetadataSection != null) {
            Pair<Integer, Integer> pair = TextureAtlasSprite.getFrameSize(animationMetadataSection.getFrameWidth(), animationMetadataSection.getFrameHeight(), pngInfo.width, pngInfo.height);
            this.width = pair.getFirst();
            this.height = pair.getSecond();
            if (!TextureAtlasSprite.isDivisionInteger(pngInfo.width, this.width) || !TextureAtlasSprite.isDivisionInteger(pngInfo.height, this.height)) {
                throw new IllegalArgumentException(String.format("Image size %s,%s is not multiply of frame size %s,%s", this.width, this.height, pngInfo.width, pngInfo.height));
            }
        } else {
            this.width = pngInfo.width;
            this.height = pngInfo.height;
        }
        this.metadata = animationMetadataSection;
    }

    private static Pair<Integer, Integer> getFrameSize(int i, int j, int k, int l) {
        if (i != -1) {
            if (j != -1) {
                return Pair.of(i, j);
            }
            return Pair.of(i, l);
        }
        if (j != -1) {
            return Pair.of(k, j);
        }
        int m = Math.min(k, l);
        return Pair.of(m, m);
    }

    private static boolean isDivisionInteger(int i, int j) {
        return i / j * j == i;
    }

    private void generateMipLevels(int i) {
        NativeImage[] nativeImages = new NativeImage[i + 1];
        nativeImages[0] = this.mainImage[0];
        if (i > 0) {
            int j;
            boolean bl = false;
            block0: for (j = 0; j < this.mainImage[0].getWidth(); ++j) {
                for (int k = 0; k < this.mainImage[0].getHeight(); ++k) {
                    if (this.mainImage[0].getPixelRGBA(j, k) >> 24 != 0) continue;
                    bl = true;
                    break block0;
                }
            }
            for (j = 1; j <= i; ++j) {
                if (this.mainImage.length > j && this.mainImage[j] != null) {
                    nativeImages[j] = this.mainImage[j];
                    continue;
                }
                NativeImage nativeImage = nativeImages[j - 1];
                NativeImage nativeImage2 = new NativeImage(nativeImage.getWidth() >> 1, nativeImage.getHeight() >> 1, false);
                int l = nativeImage2.getWidth();
                int m = nativeImage2.getHeight();
                for (int n = 0; n < l; ++n) {
                    for (int o = 0; o < m; ++o) {
                        nativeImage2.setPixelRGBA(n, o, TextureAtlasSprite.alphaBlend(nativeImage.getPixelRGBA(n * 2 + 0, o * 2 + 0), nativeImage.getPixelRGBA(n * 2 + 1, o * 2 + 0), nativeImage.getPixelRGBA(n * 2 + 0, o * 2 + 1), nativeImage.getPixelRGBA(n * 2 + 1, o * 2 + 1), bl));
                    }
                }
                nativeImages[j] = nativeImage2;
            }
            for (j = i + 1; j < this.mainImage.length; ++j) {
                if (this.mainImage[j] == null) continue;
                this.mainImage[j].close();
            }
        }
        this.mainImage = nativeImages;
    }

    private static int alphaBlend(int i, int j, int k, int l, boolean bl) {
        if (bl) {
            float f = 0.0f;
            float g = 0.0f;
            float h = 0.0f;
            float m = 0.0f;
            if (i >> 24 != 0) {
                f += TextureAtlasSprite.getPow22(i >> 24);
                g += TextureAtlasSprite.getPow22(i >> 16);
                h += TextureAtlasSprite.getPow22(i >> 8);
                m += TextureAtlasSprite.getPow22(i >> 0);
            }
            if (j >> 24 != 0) {
                f += TextureAtlasSprite.getPow22(j >> 24);
                g += TextureAtlasSprite.getPow22(j >> 16);
                h += TextureAtlasSprite.getPow22(j >> 8);
                m += TextureAtlasSprite.getPow22(j >> 0);
            }
            if (k >> 24 != 0) {
                f += TextureAtlasSprite.getPow22(k >> 24);
                g += TextureAtlasSprite.getPow22(k >> 16);
                h += TextureAtlasSprite.getPow22(k >> 8);
                m += TextureAtlasSprite.getPow22(k >> 0);
            }
            if (l >> 24 != 0) {
                f += TextureAtlasSprite.getPow22(l >> 24);
                g += TextureAtlasSprite.getPow22(l >> 16);
                h += TextureAtlasSprite.getPow22(l >> 8);
                m += TextureAtlasSprite.getPow22(l >> 0);
            }
            int n = (int)(Math.pow(f /= 4.0f, 0.45454545454545453) * 255.0);
            int o = (int)(Math.pow(g /= 4.0f, 0.45454545454545453) * 255.0);
            int p = (int)(Math.pow(h /= 4.0f, 0.45454545454545453) * 255.0);
            int q = (int)(Math.pow(m /= 4.0f, 0.45454545454545453) * 255.0);
            if (n < 96) {
                n = 0;
            }
            return n << 24 | o << 16 | p << 8 | q;
        }
        int r = TextureAtlasSprite.gammaBlend(i, j, k, l, 24);
        int s = TextureAtlasSprite.gammaBlend(i, j, k, l, 16);
        int t = TextureAtlasSprite.gammaBlend(i, j, k, l, 8);
        int u = TextureAtlasSprite.gammaBlend(i, j, k, l, 0);
        return r << 24 | s << 16 | t << 8 | u;
    }

    private static int gammaBlend(int i, int j, int k, int l, int m) {
        float f = TextureAtlasSprite.getPow22(i >> m);
        float g = TextureAtlasSprite.getPow22(j >> m);
        float h = TextureAtlasSprite.getPow22(k >> m);
        float n = TextureAtlasSprite.getPow22(l >> m);
        float o = (float)((double)((float)Math.pow((double)(f + g + h + n) * 0.25, 0.45454545454545453)));
        return (int)((double)o * 255.0);
    }

    private static float getPow22(int i) {
        return POW22[i & 0xFF];
    }

    private void upload(int i) {
        int j = 0;
        int k = 0;
        if (this.framesX != null) {
            j = this.framesX[i] * this.width;
            k = this.framesY[i] * this.height;
        }
        this.upload(j, k, this.mainImage);
    }

    private void upload(int i, int j, NativeImage[] nativeImages) {
        for (int k = 0; k < this.mainImage.length; ++k) {
            nativeImages[k].upload(k, this.x >> k, this.y >> k, i >> k, j >> k, this.width >> k, this.height >> k, this.mainImage.length > 1, false);
        }
    }

    public void init(int i, int j, int k, int l) {
        this.x = k;
        this.y = l;
        this.u0 = (float)k / (float)i;
        this.u1 = (float)(k + this.width) / (float)i;
        this.v0 = (float)l / (float)j;
        this.v1 = (float)(l + this.height) / (float)j;
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
        } else if (this.metadata.isInterpolatedFrames()) {
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(this::uploadInterpolatedFrame);
            } else {
                this.uploadInterpolatedFrame();
            }
        }
    }

    private void uploadInterpolatedFrame() {
        int j;
        int k;
        double d = 1.0 - (double)this.subFrame / (double)this.metadata.getFrameTime(this.frame);
        int i = this.metadata.getFrameIndex(this.frame);
        if (i != (k = this.metadata.getFrameIndex((this.frame + 1) % (j = this.metadata.getFrameCount() == 0 ? this.getFrameCount() : this.metadata.getFrameCount()))) && k >= 0 && k < this.getFrameCount()) {
            if (this.activeFrame == null || this.activeFrame.length != this.mainImage.length) {
                if (this.activeFrame != null) {
                    for (NativeImage nativeImage : this.activeFrame) {
                        if (nativeImage == null) continue;
                        nativeImage.close();
                    }
                }
                this.activeFrame = new NativeImage[this.mainImage.length];
            }
            for (int l = 0; l < this.mainImage.length; ++l) {
                int m = this.width >> l;
                int n = this.height >> l;
                if (this.activeFrame[l] == null) {
                    this.activeFrame[l] = new NativeImage(m, n, false);
                }
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
            this.upload(0, 0, this.activeFrame);
        }
    }

    private int mix(double d, int i, int j) {
        return (int)(d * (double)i + (1.0 - d) * (double)j);
    }

    public int getFrameCount() {
        return this.framesX == null ? 0 : this.framesX.length;
    }

    public void loadData(Resource resource, int i) throws IOException {
        NativeImage nativeImage = NativeImage.read(resource.getInputStream());
        this.mainImage = new NativeImage[i];
        this.mainImage[0] = nativeImage;
        int j = this.metadata != null && this.metadata.getFrameWidth() != -1 ? nativeImage.getWidth() / this.metadata.getFrameWidth() : nativeImage.getWidth() / this.width;
        int k = this.metadata != null && this.metadata.getFrameHeight() != -1 ? nativeImage.getHeight() / this.metadata.getFrameHeight() : nativeImage.getHeight() / this.height;
        if (this.metadata != null && this.metadata.getFrameCount() > 0) {
            int l = (Integer)this.metadata.getUniqueFrameIndices().stream().max(Integer::compareTo).get() + 1;
            this.framesX = new int[l];
            this.framesY = new int[l];
            Arrays.fill(this.framesX, -1);
            Arrays.fill(this.framesY, -1);
            for (int m : this.metadata.getUniqueFrameIndices()) {
                int o;
                if (m >= j * k) {
                    throw new RuntimeException("invalid frameindex " + m);
                }
                int n = m / j;
                this.framesX[m] = o = m % j;
                this.framesY[m] = n;
            }
        } else {
            int m;
            ArrayList<AnimationFrame> list = Lists.newArrayList();
            int p = j * k;
            this.framesX = new int[p];
            this.framesY = new int[p];
            for (m = 0; m < k; ++m) {
                int n = 0;
                while (n < j) {
                    int o = m * j + n;
                    this.framesX[o] = n++;
                    this.framesY[o] = m;
                    list.add(new AnimationFrame(o, -1));
                }
            }
            m = 1;
            boolean bl = false;
            if (this.metadata != null) {
                m = this.metadata.getDefaultFrameTime();
                bl = this.metadata.isInterpolatedFrames();
            }
            this.metadata = new AnimationMetadataSection(list, this.width, this.height, m, bl);
        }
    }

    public void applyMipmapping(int i) {
        try {
            this.generateMipLevels(i);
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Generating mipmaps for frame");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Frame being iterated");
            crashReportCategory.setDetail("Frame sizes", () -> {
                StringBuilder stringBuilder = new StringBuilder();
                for (NativeImage nativeImage : this.mainImage) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append(nativeImage == null ? "null" : nativeImage.getWidth() + "x" + nativeImage.getHeight());
                }
                return stringBuilder.toString();
            });
            throw new ReportedException(crashReport);
        }
    }

    public void wipeFrameData() {
        if (this.mainImage != null) {
            for (NativeImage nativeImage : this.mainImage) {
                if (nativeImage == null) continue;
                nativeImage.close();
            }
        }
        this.mainImage = null;
        if (this.activeFrame != null) {
            for (NativeImage nativeImage : this.activeFrame) {
                if (nativeImage == null) continue;
                nativeImage.close();
            }
        }
        this.activeFrame = null;
    }

    public boolean isAnimation() {
        return this.metadata != null && this.metadata.getFrameCount() > 1;
    }

    public String toString() {
        int i = this.framesX == null ? 0 : this.framesX.length;
        return "TextureAtlasSprite{name='" + this.name + '\'' + ", frameCount=" + i + ", x=" + this.x + ", y=" + this.y + ", height=" + this.height + ", width=" + this.width + ", u0=" + this.u0 + ", u1=" + this.u1 + ", v0=" + this.v0 + ", v1=" + this.v1 + '}';
    }

    private int getPixel(int i, int j, int k, int l) {
        return this.mainImage[j].getPixelRGBA(k + (this.framesX[i] * this.width >> j), l + (this.framesY[i] * this.height >> j));
    }

    public boolean isTransparent(int i, int j, int k) {
        return (this.mainImage[0].getPixelRGBA(j + this.framesX[i] * this.width, k + this.framesY[i] * this.height) >> 24 & 0xFF) == 0;
    }

    public void uploadFirstFrame() {
        this.upload(0);
    }
}

