/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.FloatBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class FogRenderer {
    private static final FloatBuffer BLACK_BUFFER = Util.make(MemoryTracker.createFloatBuffer(16), floatBuffer -> floatBuffer.put(0.0f).put(0.0f).put(0.0f).put(1.0f).flip());
    private static final FloatBuffer COLOR_BUFFER = MemoryTracker.createFloatBuffer(16);
    private float fogRed;
    private float fogGreen;
    private float fogBlue;
    private int targetBiomeFog = -1;
    private int previousBiomeFog = -1;
    private long biomeChangedTime = -1L;

    public void setupClearColor(Camera camera, float f, Level level, int i, float g) {
        float w;
        float v;
        float u;
        int j;
        FluidState fluidState = camera.getFluidInCamera();
        if (fluidState.is(FluidTags.WATER)) {
            long l = Util.getMillis();
            j = level.getBiome(new BlockPos(camera.getPosition())).getWaterFogColor();
            if (this.biomeChangedTime < 0L) {
                this.targetBiomeFog = j;
                this.previousBiomeFog = j;
                this.biomeChangedTime = l;
            }
            int k = this.targetBiomeFog >> 16 & 0xFF;
            int m = this.targetBiomeFog >> 8 & 0xFF;
            int n = this.targetBiomeFog & 0xFF;
            int o = this.previousBiomeFog >> 16 & 0xFF;
            int p = this.previousBiomeFog >> 8 & 0xFF;
            int q = this.previousBiomeFog & 0xFF;
            float h = Mth.clamp((float)(l - this.biomeChangedTime) / 5000.0f, 0.0f, 1.0f);
            float r = Mth.lerp(h, o, k);
            float s = Mth.lerp(h, p, m);
            float t = Mth.lerp(h, q, n);
            u = r / 255.0f;
            v = s / 255.0f;
            w = t / 255.0f;
            if (this.targetBiomeFog != j) {
                this.targetBiomeFog = j;
                this.previousBiomeFog = Mth.floor(r) << 16 | Mth.floor(s) << 8 | Mth.floor(t);
                this.biomeChangedTime = l;
            }
        } else if (fluidState.is(FluidTags.LAVA)) {
            u = 0.6f;
            v = 0.1f;
            w = 0.0f;
            this.biomeChangedTime = -1L;
        } else {
            float ac;
            float x = 0.25f + 0.75f * (float)i / 32.0f;
            x = 1.0f - (float)Math.pow(x, 0.25);
            Vec3 vec3 = level.getSkyColor(camera.getBlockPosition(), f);
            float y = (float)vec3.x;
            float z = (float)vec3.y;
            float aa = (float)vec3.z;
            Vec3 vec32 = level.getFogColor(f);
            u = (float)vec32.x;
            v = (float)vec32.y;
            w = (float)vec32.z;
            if (i >= 4) {
                float[] fs;
                double d = Mth.sin(level.getSunAngle(f)) > 0.0f ? -1.0 : 1.0;
                Vec3 vec33 = new Vec3(d, 0.0, 0.0);
                float h = (float)camera.getLookVector().dot(vec33);
                if (h < 0.0f) {
                    h = 0.0f;
                }
                if (h > 0.0f && (fs = level.dimension.getSunriseColor(level.getTimeOfDay(f), f)) != null) {
                    u = u * (1.0f - (h *= fs[3])) + fs[0] * h;
                    v = v * (1.0f - h) + fs[1] * h;
                    w = w * (1.0f - h) + fs[2] * h;
                }
            }
            u += (y - u) * x;
            v += (z - v) * x;
            w += (aa - w) * x;
            float ab = level.getRainLevel(f);
            if (ab > 0.0f) {
                ac = 1.0f - ab * 0.5f;
                float ad = 1.0f - ab * 0.4f;
                u *= ac;
                v *= ac;
                w *= ad;
            }
            if ((ac = level.getThunderLevel(f)) > 0.0f) {
                float ad = 1.0f - ac * 0.5f;
                u *= ad;
                v *= ad;
                w *= ad;
            }
            this.biomeChangedTime = -1L;
        }
        double e = camera.getPosition().y * level.dimension.getClearColorScale();
        if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
            j = ((LivingEntity)camera.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
            e = j < 20 ? (e *= (double)(1.0f - (float)j / 20.0f)) : 0.0;
        }
        if (e < 1.0) {
            if (e < 0.0) {
                e = 0.0;
            }
            e *= e;
            u = (float)((double)u * e);
            v = (float)((double)v * e);
            w = (float)((double)w * e);
        }
        if (g > 0.0f) {
            u = u * (1.0f - g) + u * 0.7f * g;
            v = v * (1.0f - g) + v * 0.6f * g;
            w = w * (1.0f - g) + w * 0.6f * g;
        }
        if (fluidState.is(FluidTags.WATER)) {
            float y = 0.0f;
            if (camera.getEntity() instanceof LocalPlayer) {
                LocalPlayer localPlayer = (LocalPlayer)camera.getEntity();
                y = localPlayer.getWaterVision();
            }
            float z = Math.min(1.0f / u, Math.min(1.0f / v, 1.0f / w));
            u = u * (1.0f - y) + u * z * y;
            v = v * (1.0f - y) + v * z * y;
            w = w * (1.0f - y) + w * z * y;
        } else if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.NIGHT_VISION)) {
            float y = GameRenderer.getNightVisionScale((LivingEntity)camera.getEntity(), f);
            float z = Math.min(1.0f / u, Math.min(1.0f / v, 1.0f / w));
            u = u * (1.0f - y) + u * z * y;
            v = v * (1.0f - y) + v * z * y;
            w = w * (1.0f - y) + w * z * y;
        }
        RenderSystem.clearColor(u, v, w, 0.0f);
        if (this.fogRed != u || this.fogGreen != v || this.fogBlue != w) {
            COLOR_BUFFER.clear();
            COLOR_BUFFER.put(u).put(v).put(w).put(1.0f);
            COLOR_BUFFER.flip();
            this.fogRed = u;
            this.fogGreen = v;
            this.fogBlue = w;
        }
    }

    public static void setupFog(Camera camera, FogMode fogMode, float f, boolean bl) {
        FogRenderer.resetFogColor(false);
        RenderSystem.normal3f(0.0f, -1.0f, 0.0f);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        FluidState fluidState = camera.getFluidInCamera();
        if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
            float g = 5.0f;
            int i = ((LivingEntity)camera.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
            if (i < 20) {
                g = Mth.lerp(1.0f - (float)i / 20.0f, 5.0f, f);
            }
            RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
            if (fogMode == FogMode.FOG_SKY) {
                RenderSystem.fogStart(0.0f);
                RenderSystem.fogEnd(g * 0.8f);
            } else {
                RenderSystem.fogStart(g * 0.25f);
                RenderSystem.fogEnd(g);
            }
            RenderSystem.setupNvFogDistance();
        } else if (fluidState.is(FluidTags.WATER)) {
            RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
            if (camera.getEntity() instanceof LivingEntity) {
                if (camera.getEntity() instanceof LocalPlayer) {
                    LocalPlayer localPlayer = (LocalPlayer)camera.getEntity();
                    float h = 0.05f - localPlayer.getWaterVision() * localPlayer.getWaterVision() * 0.03f;
                    Biome biome = localPlayer.level.getBiome(new BlockPos(localPlayer));
                    if (biome == Biomes.SWAMP || biome == Biomes.SWAMP_HILLS) {
                        h += 0.005f;
                    }
                    RenderSystem.fogDensity(h);
                } else {
                    RenderSystem.fogDensity(0.05f);
                }
            } else {
                RenderSystem.fogDensity(0.1f);
            }
        } else if (fluidState.is(FluidTags.LAVA)) {
            RenderSystem.fogMode(GlStateManager.FogMode.EXP);
            RenderSystem.fogDensity(2.0f);
        } else {
            RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
            if (fogMode == FogMode.FOG_SKY) {
                RenderSystem.fogStart(0.0f);
                RenderSystem.fogEnd(f);
            } else {
                RenderSystem.fogStart(f * 0.75f);
                RenderSystem.fogEnd(f);
            }
            RenderSystem.setupNvFogDistance();
            if (bl) {
                RenderSystem.fogStart(f * 0.05f);
                RenderSystem.fogEnd(Math.min(f, 192.0f) * 0.5f);
            }
        }
        RenderSystem.enableFog();
    }

    public static void resetFogColor(boolean bl) {
        RenderSystem.fog(2918, bl ? BLACK_BUFFER : COLOR_BUFFER);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum FogMode {
        FOG_SKY,
        FOG_TERRAIN;

    }
}

