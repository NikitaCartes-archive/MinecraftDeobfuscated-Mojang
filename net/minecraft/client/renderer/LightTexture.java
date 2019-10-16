/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

@Environment(value=EnvType.CLIENT)
public class LightTexture
implements AutoCloseable {
    private final DynamicTexture lightTexture;
    private final NativeImage lightPixels;
    private final ResourceLocation lightTextureLocation;
    private boolean updateLightTexture;
    private float blockLightRed;
    private float blockLightRedTotal;
    private final GameRenderer renderer;
    private final Minecraft minecraft;

    public LightTexture(GameRenderer gameRenderer, Minecraft minecraft) {
        this.renderer = gameRenderer;
        this.minecraft = minecraft;
        this.lightTexture = new DynamicTexture(16, 16, false);
        this.lightTextureLocation = this.minecraft.getTextureManager().register("light_map", this.lightTexture);
        this.lightPixels = this.lightTexture.getPixels();
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                this.lightPixels.setPixelRGBA(j, i, -1);
            }
        }
        this.lightTexture.upload();
    }

    @Override
    public void close() {
        this.lightTexture.close();
    }

    public void tick() {
        this.blockLightRedTotal = (float)((double)this.blockLightRedTotal + (Math.random() - Math.random()) * Math.random() * Math.random());
        this.blockLightRedTotal = (float)((double)this.blockLightRedTotal * 0.9);
        this.blockLightRed += this.blockLightRedTotal - this.blockLightRed;
        this.updateLightTexture = true;
    }

    public void turnOffLightLayer() {
        RenderSystem.activeTexture(33986);
        RenderSystem.disableTexture();
        RenderSystem.activeTexture(33984);
    }

    public void turnOnLightLayer() {
        RenderSystem.activeTexture(33986);
        RenderSystem.matrixMode(5890);
        RenderSystem.loadIdentity();
        float f = 0.00390625f;
        RenderSystem.scalef(0.00390625f, 0.00390625f, 0.00390625f);
        RenderSystem.translatef(8.0f, 8.0f, 8.0f);
        RenderSystem.matrixMode(5888);
        this.minecraft.getTextureManager().bind(this.lightTextureLocation);
        RenderSystem.texParameter(3553, 10241, 9729);
        RenderSystem.texParameter(3553, 10240, 9729);
        RenderSystem.texParameter(3553, 10242, 10496);
        RenderSystem.texParameter(3553, 10243, 10496);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableTexture();
        RenderSystem.activeTexture(33984);
    }

    public void updateLightTexture(float f) {
        if (!this.updateLightTexture) {
            return;
        }
        this.minecraft.getProfiler().push("lightTex");
        MultiPlayerLevel level = this.minecraft.level;
        if (level == null) {
            return;
        }
        float g = level.getSkyDarken(1.0f);
        float h = g * 0.95f + 0.05f;
        float i = this.minecraft.player.getWaterVision();
        float j = this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION) ? GameRenderer.getNightVisionScale(this.minecraft.player, f) : (i > 0.0f && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER) ? i : 0.0f);
        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                float x;
                float m = this.getBrightness(level, k) * h;
                float n = this.getBrightness(level, l) * (this.blockLightRed * 0.1f + 1.5f);
                if (level.getSkyFlashTime() > 0) {
                    m = this.getBrightness(level, k);
                }
                float o = m * (g * 0.65f + 0.35f);
                float p = m * (g * 0.65f + 0.35f);
                float q = m;
                float r = n;
                float s = n * ((n * 0.6f + 0.4f) * 0.6f + 0.4f);
                float t = n * (n * n * 0.6f + 0.4f);
                float u = o + r;
                float v = p + s;
                float w = q + t;
                u = u * 0.96f + 0.03f;
                v = v * 0.96f + 0.03f;
                w = w * 0.96f + 0.03f;
                if (this.renderer.getDarkenWorldAmount(f) > 0.0f) {
                    x = this.renderer.getDarkenWorldAmount(f);
                    u = u * (1.0f - x) + u * 0.7f * x;
                    v = v * (1.0f - x) + v * 0.6f * x;
                    w = w * (1.0f - x) + w * 0.6f * x;
                }
                if (level.dimension.getType() == DimensionType.THE_END) {
                    u = 0.22f + r * 0.75f;
                    v = 0.28f + s * 0.75f;
                    w = 0.25f + t * 0.75f;
                }
                if (j > 0.0f) {
                    x = Math.min(1.0f / u, Math.min(1.0f / v, 1.0f / w));
                    u = u * (1.0f - j) + u * x * j;
                    v = v * (1.0f - j) + v * x * j;
                    w = w * (1.0f - j) + w * x * j;
                }
                u = Mth.clamp(u, 0.0f, 1.0f);
                v = Mth.clamp(v, 0.0f, 1.0f);
                w = Mth.clamp(w, 0.0f, 1.0f);
                x = (float)this.minecraft.options.gamma;
                float y = 1.0f - u;
                float z = 1.0f - v;
                float aa = 1.0f - w;
                y = 1.0f - y * y * y * y;
                z = 1.0f - z * z * z * z;
                aa = 1.0f - aa * aa * aa * aa;
                u = u * (1.0f - x) + y * x;
                v = v * (1.0f - x) + z * x;
                w = w * (1.0f - x) + aa * x;
                u = u * 0.96f + 0.03f;
                v = v * 0.96f + 0.03f;
                w = w * 0.96f + 0.03f;
                u = Mth.clamp(u, 0.0f, 1.0f);
                v = Mth.clamp(v, 0.0f, 1.0f);
                w = Mth.clamp(w, 0.0f, 1.0f);
                int ab = 255;
                int ac = (int)(u * 255.0f);
                int ad = (int)(v * 255.0f);
                int ae = (int)(w * 255.0f);
                this.lightPixels.setPixelRGBA(l, k, 0xFF000000 | ae << 16 | ad << 8 | ac);
            }
        }
        this.lightTexture.upload();
        this.updateLightTexture = false;
        this.minecraft.getProfiler().pop();
    }

    private float getBrightness(Level level, int i) {
        return level.dimension.getBrightnessRamp()[i];
    }

    public static int pack(int i, int j) {
        return i | j << 16;
    }

    public static int block(int i) {
        return i & 0xFFFF;
    }

    public static int sky(int i) {
        return i >> 16 & 0xFFFF;
    }
}

