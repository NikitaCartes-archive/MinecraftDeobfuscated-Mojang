/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class LoadingOverlay
extends Overlay {
    static final ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/mojangstudios.png");
    private static final int LOGO_BACKGROUND_COLOR = FastColor.ARGB32.color(255, 239, 50, 61);
    private static final int LOGO_BACKGROUND_COLOR_DARK = FastColor.ARGB32.color(255, 0, 0, 0);
    private static final IntSupplier BRAND_BACKGROUND = () -> Minecraft.getInstance().options.darkMojangStudiosBackground().get() != false ? LOGO_BACKGROUND_COLOR_DARK : LOGO_BACKGROUND_COLOR;
    private static final int LOGO_SCALE = 240;
    private static final float LOGO_QUARTER_FLOAT = 60.0f;
    private static final int LOGO_QUARTER = 60;
    private static final int LOGO_HALF = 120;
    private static final float LOGO_OVERLAP = 0.0625f;
    private static final float SMOOTHING = 0.95f;
    public static final long FADE_OUT_TIME = 1000L;
    public static final long FADE_IN_TIME = 500L;
    private final Minecraft minecraft;
    private final ReloadInstance reload;
    private final Consumer<Optional<Throwable>> onFinish;
    private final boolean fadeIn;
    private float currentProgress;
    private long fadeOutStart = -1L;
    private long fadeInStart = -1L;

    public LoadingOverlay(Minecraft minecraft, ReloadInstance reloadInstance, Consumer<Optional<Throwable>> consumer, boolean bl) {
        this.minecraft = minecraft;
        this.reload = reloadInstance;
        this.onFinish = consumer;
        this.fadeIn = bl;
    }

    public static void registerTextures(Minecraft minecraft) {
        minecraft.getTextureManager().register(MOJANG_STUDIOS_LOGO_LOCATION, new LogoTexture());
    }

    private static int replaceAlpha(int i, int j) {
        return i & 0xFFFFFF | j << 24;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        float o;
        int n;
        float h;
        int k = this.minecraft.getWindow().getGuiScaledWidth();
        int l = this.minecraft.getWindow().getGuiScaledHeight();
        long m = Util.getMillis();
        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = m;
        }
        float g = this.fadeOutStart > -1L ? (float)(m - this.fadeOutStart) / 1000.0f : -1.0f;
        float f2 = h = this.fadeInStart > -1L ? (float)(m - this.fadeInStart) / 500.0f : -1.0f;
        if (g >= 1.0f) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.render(poseStack, 0, 0, f);
            }
            n = Mth.ceil((1.0f - Mth.clamp(g - 1.0f, 0.0f, 1.0f)) * 255.0f);
            LoadingOverlay.fill(poseStack, 0, 0, k, l, LoadingOverlay.replaceAlpha(BRAND_BACKGROUND.getAsInt(), n));
            o = 1.0f - Mth.clamp(g - 1.0f, 0.0f, 1.0f);
        } else if (this.fadeIn) {
            if (this.minecraft.screen != null && h < 1.0f) {
                this.minecraft.screen.render(poseStack, i, j, f);
            }
            n = Mth.ceil(Mth.clamp((double)h, 0.15, 1.0) * 255.0);
            LoadingOverlay.fill(poseStack, 0, 0, k, l, LoadingOverlay.replaceAlpha(BRAND_BACKGROUND.getAsInt(), n));
            o = Mth.clamp(h, 0.0f, 1.0f);
        } else {
            n = BRAND_BACKGROUND.getAsInt();
            float p = (float)(n >> 16 & 0xFF) / 255.0f;
            float q = (float)(n >> 8 & 0xFF) / 255.0f;
            float r = (float)(n & 0xFF) / 255.0f;
            GlStateManager._clearColor(p, q, r, 1.0f);
            GlStateManager._clear(16384, Minecraft.ON_OSX);
            o = 1.0f;
        }
        n = (int)((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.5);
        int s = (int)((double)this.minecraft.getWindow().getGuiScaledHeight() * 0.5);
        double d = Math.min((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.75, (double)this.minecraft.getWindow().getGuiScaledHeight()) * 0.25;
        int t = (int)(d * 0.5);
        double e = d * 4.0;
        int u = (int)(e * 0.5);
        RenderSystem.setShaderTexture(0, MOJANG_STUDIOS_LOGO_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, o);
        LoadingOverlay.blit(poseStack, n - u, s - t, u, (int)d, -0.0625f, 0.0f, 120, 60, 120, 120);
        LoadingOverlay.blit(poseStack, n, s - t, u, (int)d, 0.0625f, 60.0f, 120, 60, 120, 120);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        int v = (int)((double)this.minecraft.getWindow().getGuiScaledHeight() * 0.8325);
        float w = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95f + w * 0.050000012f, 0.0f, 1.0f);
        if (g < 1.0f) {
            this.drawProgressBar(poseStack, k / 2 - u, v - 5, k / 2 + u, v + 5, 1.0f - Mth.clamp(g, 0.0f, 1.0f));
        }
        if (g >= 2.0f) {
            this.minecraft.setOverlay(null);
        }
        if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || h >= 2.0f)) {
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.onFinish.accept(Optional.of(throwable));
            }
            this.fadeOutStart = Util.getMillis();
            if (this.minecraft.screen != null) {
                this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
            }
        }
    }

    private void drawProgressBar(PoseStack poseStack, int i, int j, int k, int l, float f) {
        int m = Mth.ceil((float)(k - i - 2) * this.currentProgress);
        int n = Math.round(f * 255.0f);
        int o = FastColor.ARGB32.color(n, 255, 255, 255);
        LoadingOverlay.fill(poseStack, i + 2, j + 2, i + m, l - 2, o);
        LoadingOverlay.fill(poseStack, i + 1, j, k - 1, j + 1, o);
        LoadingOverlay.fill(poseStack, i + 1, l, k - 1, l - 1, o);
        LoadingOverlay.fill(poseStack, i, j, i + 1, l, o);
        LoadingOverlay.fill(poseStack, k, j, k - 1, l, o);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Environment(value=EnvType.CLIENT)
    static class LogoTexture
    extends SimpleTexture {
        public LogoTexture() {
            super(MOJANG_STUDIOS_LOGO_LOCATION);
        }

        @Override
        protected SimpleTexture.TextureImage getTextureImage(ResourceManager resourceManager) {
            SimpleTexture.TextureImage textureImage;
            block9: {
                VanillaPackResources vanillaPackResources = Minecraft.getInstance().getVanillaPackResources();
                IoSupplier<InputStream> ioSupplier = vanillaPackResources.getResource(PackType.CLIENT_RESOURCES, MOJANG_STUDIOS_LOGO_LOCATION);
                if (ioSupplier == null) {
                    return new SimpleTexture.TextureImage(new FileNotFoundException(MOJANG_STUDIOS_LOGO_LOCATION.toString()));
                }
                InputStream inputStream = ioSupplier.get();
                try {
                    textureImage = new SimpleTexture.TextureImage(new TextureMetadataSection(true, true), NativeImage.read(inputStream));
                    if (inputStream == null) break block9;
                } catch (Throwable throwable) {
                    try {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    } catch (IOException iOException) {
                        return new SimpleTexture.TextureImage(iOException);
                    }
                }
                inputStream.close();
            }
            return textureImage;
        }
    }
}

