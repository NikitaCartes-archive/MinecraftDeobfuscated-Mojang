/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPack;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class LoadingOverlay
extends Overlay {
    private static final ResourceLocation MOJANG_LOGO_LOCATION = new ResourceLocation("textures/gui/title/mojang.png");
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
        minecraft.getTextureManager().register(MOJANG_LOGO_LOCATION, new LogoTexture());
    }

    @Override
    public void render(int i, int j, float f) {
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
                this.minecraft.screen.render(0, 0, f);
            }
            n = Mth.ceil((1.0f - Mth.clamp(g - 1.0f, 0.0f, 1.0f)) * 255.0f);
            LoadingOverlay.fill(0, 0, k, l, 0xFFFFFF | n << 24);
            o = 1.0f - Mth.clamp(g - 1.0f, 0.0f, 1.0f);
        } else if (this.fadeIn) {
            if (this.minecraft.screen != null && h < 1.0f) {
                this.minecraft.screen.render(i, j, f);
            }
            n = Mth.ceil(Mth.clamp((double)h, 0.15, 1.0) * 255.0);
            LoadingOverlay.fill(0, 0, k, l, 0xFFFFFF | n << 24);
            o = Mth.clamp(h, 0.0f, 1.0f);
        } else {
            LoadingOverlay.fill(0, 0, k, l, -1);
            o = 1.0f;
        }
        n = (this.minecraft.getWindow().getGuiScaledWidth() - 256) / 2;
        int p = (this.minecraft.getWindow().getGuiScaledHeight() - 256) / 2;
        this.minecraft.getTextureManager().bind(MOJANG_LOGO_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, o);
        this.blit(n, p, 0, 0, 256, 256);
        float q = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95f + q * 0.050000012f, 0.0f, 1.0f);
        if (g < 1.0f) {
            this.drawProgressBar(k / 2 - 150, l / 4 * 3, k / 2 + 150, l / 4 * 3 + 10, 1.0f - Mth.clamp(g, 0.0f, 1.0f));
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

    private void drawProgressBar(int i, int j, int k, int l, float f) {
        int m = Mth.ceil((float)(k - i - 1) * this.currentProgress);
        LoadingOverlay.fill(i - 1, j - 1, k + 1, l + 1, 0xFF000000 | Math.round((1.0f - f) * 255.0f) << 16 | Math.round((1.0f - f) * 255.0f) << 8 | Math.round((1.0f - f) * 255.0f));
        LoadingOverlay.fill(i, j, k, l, -1);
        LoadingOverlay.fill(i + 1, j + 1, i + m, l - 1, 0xFF000000 | (int)Mth.lerp(1.0f - f, 226.0f, 255.0f) << 16 | (int)Mth.lerp(1.0f - f, 40.0f, 255.0f) << 8 | (int)Mth.lerp(1.0f - f, 55.0f, 255.0f));
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Environment(value=EnvType.CLIENT)
    static class LogoTexture
    extends SimpleTexture {
        public LogoTexture() {
            super(MOJANG_LOGO_LOCATION);
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
            try (InputStream inputStream = vanillaPack.getResource(PackType.CLIENT_RESOURCES, MOJANG_LOGO_LOCATION);){
                SimpleTexture.TextureImage textureImage = new SimpleTexture.TextureImage(null, NativeImage.read(inputStream));
                return textureImage;
            } catch (IOException iOException) {
                return new SimpleTexture.TextureImage(iOException);
            }
        }
    }
}

