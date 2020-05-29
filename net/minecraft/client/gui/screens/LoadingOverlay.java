/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPack;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class LoadingOverlay
extends Overlay {
    private static final ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/mojangstudios.png");
    private static final int BRAND_BACKGROUND = FastColor.ARGB32.color(255, 239, 50, 61);
    private static final int BRAND_BACKGROUND_NO_ALPHA = BRAND_BACKGROUND & 0xFFFFFF;
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

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        float o;
        int n;
        float h;
        int k = this.minecraft.getWindow().getGuiScaledWidth();
        int l = this.minecraft.getWindow().getGuiScaledHeight();
        long m = Util.getMillis();
        if (this.fadeIn && (this.reload.isApplying() || this.minecraft.screen != null) && this.fadeInStart == -1L) {
            this.fadeInStart = m;
        }
        float g = this.fadeOutStart > -1L ? (float)(m - this.fadeOutStart) / 1000.0f : -1.0f;
        float f2 = h = this.fadeInStart > -1L ? (float)(m - this.fadeInStart) / 500.0f : -1.0f;
        if (g >= 1.0f) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.render(poseStack, 0, 0, f);
            }
            n = Mth.ceil((1.0f - Mth.clamp(g - 1.0f, 0.0f, 1.0f)) * 255.0f);
            LoadingOverlay.fill(poseStack, 0, 0, k, l, BRAND_BACKGROUND_NO_ALPHA | n << 24);
            o = 1.0f - Mth.clamp(g - 1.0f, 0.0f, 1.0f);
        } else if (this.fadeIn) {
            if (this.minecraft.screen != null && h < 1.0f) {
                this.minecraft.screen.render(poseStack, i, j, f);
            }
            n = Mth.ceil(Mth.clamp((double)h, 0.15, 1.0) * 255.0);
            LoadingOverlay.fill(poseStack, 0, 0, k, l, BRAND_BACKGROUND_NO_ALPHA | n << 24);
            o = Mth.clamp(h, 0.0f, 1.0f);
        } else {
            LoadingOverlay.fill(poseStack, 0, 0, k, l, BRAND_BACKGROUND);
            o = 1.0f;
        }
        n = (int)((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.5);
        int p = (int)((double)this.minecraft.getWindow().getGuiScaledHeight() * 0.5);
        double d = Math.min((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.75, (double)this.minecraft.getWindow().getGuiScaledHeight()) * 0.25;
        int q = (int)(d * 0.5);
        double e = d * 4.0;
        int r = (int)(e * 0.5);
        this.minecraft.getTextureManager().bind(MOJANG_STUDIOS_LOGO_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.alphaFunc(516, 0.0f);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, o);
        LoadingOverlay.blit(poseStack, n - r, p - q, r, (int)d, -0.0625f, 0.0f, 120, 60, 120, 120);
        LoadingOverlay.blit(poseStack, n, p - q, r, (int)d, 0.0625f, 60.0f, 120, 60, 120, 120);
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.disableBlend();
        int s = (int)((double)this.minecraft.getWindow().getGuiScaledHeight() * 0.8325);
        float t = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95f + t * 0.050000012f, 0.0f, 1.0f);
        if (g < 1.0f) {
            this.drawProgressBar(poseStack, k / 2 - r, s - 5, k / 2 + r, s + 5, 1.0f - Mth.clamp(g, 0.0f, 1.0f));
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
        LoadingOverlay.fill(poseStack, i, j, k, j + 1, o);
        LoadingOverlay.fill(poseStack, i, l, k, l - 1, o);
        LoadingOverlay.fill(poseStack, i, j, i + 1, l, o);
        LoadingOverlay.fill(poseStack, k, j, k - 1, l, o);
        LoadingOverlay.fill(poseStack, i + 2, j + 2, i + m, l - 2, o);
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

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        @Override
        protected SimpleTexture.TextureImage getTextureImage(ResourceManager resourceManager) {
            Minecraft minecraft = Minecraft.getInstance();
            VanillaPack vanillaPack = minecraft.getClientPackSource().getVanillaPack();
            try (InputStream inputStream = vanillaPack.getResource(PackType.CLIENT_RESOURCES, MOJANG_STUDIOS_LOGO_LOCATION);){
                SimpleTexture.TextureImage textureImage = new SimpleTexture.TextureImage(new TextureMetadataSection(true, true), NativeImage.read(inputStream));
                return textureImage;
            } catch (IOException iOException) {
                return new SimpleTexture.TextureImage(iOException);
            }
        }
    }
}

