/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class FogRenderer {
    private static final int WATER_FOG_DISTANCE = 96;
    public static final float BIOME_FOG_TRANSITION_TIME = 5000.0f;
    private static float fogRed;
    private static float fogGreen;
    private static float fogBlue;
    private static int targetBiomeFog;
    private static int previousBiomeFog;
    private static long biomeChangedTime;

    public static void setupColor(Camera camera, float f, ClientLevel clientLevel, int i2, float g) {
        int j2;
        FogType fogType = camera.getFluidInCamera();
        Entity entity = camera.getEntity();
        if (fogType == FogType.WATER) {
            long l = Util.getMillis();
            j2 = clientLevel.getBiome(new BlockPos(camera.getPosition())).getWaterFogColor();
            if (biomeChangedTime < 0L) {
                targetBiomeFog = j2;
                previousBiomeFog = j2;
                biomeChangedTime = l;
            }
            int k2 = targetBiomeFog >> 16 & 0xFF;
            int m = targetBiomeFog >> 8 & 0xFF;
            int n = targetBiomeFog & 0xFF;
            int o = previousBiomeFog >> 16 & 0xFF;
            int p = previousBiomeFog >> 8 & 0xFF;
            int q = previousBiomeFog & 0xFF;
            float h = Mth.clamp((float)(l - biomeChangedTime) / 5000.0f, 0.0f, 1.0f);
            float r = Mth.lerp(h, o, k2);
            float s = Mth.lerp(h, p, m);
            float t = Mth.lerp(h, q, n);
            fogRed = r / 255.0f;
            fogGreen = s / 255.0f;
            fogBlue = t / 255.0f;
            if (targetBiomeFog != j2) {
                targetBiomeFog = j2;
                previousBiomeFog = Mth.floor(r) << 16 | Mth.floor(s) << 8 | Mth.floor(t);
                biomeChangedTime = l;
            }
        } else if (fogType == FogType.LAVA) {
            fogRed = 0.6f;
            fogGreen = 0.1f;
            fogBlue = 0.0f;
            biomeChangedTime = -1L;
        } else if (fogType == FogType.POWDER_SNOW) {
            fogRed = 0.623f;
            fogGreen = 0.734f;
            fogBlue = 0.785f;
            biomeChangedTime = -1L;
            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0f);
        } else {
            float r;
            float s;
            float h;
            float u = 0.25f + 0.75f * (float)i2 / 32.0f;
            u = 1.0f - (float)Math.pow(u, 0.25);
            Vec3 vec3 = clientLevel.getSkyColor(camera.getPosition(), f);
            float v = (float)vec3.x;
            float w = (float)vec3.y;
            float x = (float)vec3.z;
            float y = Mth.clamp(Mth.cos(clientLevel.getTimeOfDay(f) * ((float)Math.PI * 2)) * 2.0f + 0.5f, 0.0f, 1.0f);
            BiomeManager biomeManager = clientLevel.getBiomeManager();
            Vec3 vec32 = camera.getPosition().subtract(2.0, 2.0, 2.0).scale(0.25);
            Vec3 vec33 = CubicSampler.gaussianSampleVec3(vec32, (i, j, k) -> clientLevel.effects().getBrightnessDependentFogColor(Vec3.fromRGB24(biomeManager.getNoiseBiomeAtQuart(i, j, k).getFogColor()), y));
            fogRed = (float)vec33.x();
            fogGreen = (float)vec33.y();
            fogBlue = (float)vec33.z();
            if (i2 >= 4) {
                float[] fs;
                h = Mth.sin(clientLevel.getSunAngle(f)) > 0.0f ? -1.0f : 1.0f;
                Vector3f vector3f = new Vector3f(h, 0.0f, 0.0f);
                s = camera.getLookVector().dot(vector3f);
                if (s < 0.0f) {
                    s = 0.0f;
                }
                if (s > 0.0f && (fs = clientLevel.effects().getSunriseColor(clientLevel.getTimeOfDay(f), f)) != null) {
                    fogRed = fogRed * (1.0f - (s *= fs[3])) + fs[0] * s;
                    fogGreen = fogGreen * (1.0f - s) + fs[1] * s;
                    fogBlue = fogBlue * (1.0f - s) + fs[2] * s;
                }
            }
            fogRed += (v - fogRed) * u;
            fogGreen += (w - fogGreen) * u;
            fogBlue += (x - fogBlue) * u;
            h = clientLevel.getRainLevel(f);
            if (h > 0.0f) {
                float r2 = 1.0f - h * 0.5f;
                s = 1.0f - h * 0.4f;
                fogRed *= r2;
                fogGreen *= r2;
                fogBlue *= s;
            }
            if ((r = clientLevel.getThunderLevel(f)) > 0.0f) {
                s = 1.0f - r * 0.5f;
                fogRed *= s;
                fogGreen *= s;
                fogBlue *= s;
            }
            biomeChangedTime = -1L;
        }
        double d = (camera.getPosition().y - (double)clientLevel.getMinBuildHeight()) * clientLevel.getLevelData().getClearColorScale();
        if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
            j2 = ((LivingEntity)camera.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
            d = j2 < 20 ? (double)(1.0f - (float)j2 / 20.0f) : 0.0;
        }
        if (d < 1.0 && fogType != FogType.LAVA && fogType != FogType.POWDER_SNOW) {
            if (d < 0.0) {
                d = 0.0;
            }
            d *= d;
            fogRed = (float)((double)fogRed * d);
            fogGreen = (float)((double)fogGreen * d);
            fogBlue = (float)((double)fogBlue * d);
        }
        if (g > 0.0f) {
            fogRed = fogRed * (1.0f - g) + fogRed * 0.7f * g;
            fogGreen = fogGreen * (1.0f - g) + fogGreen * 0.6f * g;
            fogBlue = fogBlue * (1.0f - g) + fogBlue * 0.6f * g;
        }
        float v = fogType == FogType.WATER ? (entity instanceof LocalPlayer ? ((LocalPlayer)entity).getWaterVision() : 1.0f) : (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.NIGHT_VISION) ? GameRenderer.getNightVisionScale((LivingEntity)entity, f) : 0.0f);
        if (fogRed != 0.0f && fogGreen != 0.0f && fogBlue != 0.0f) {
            float w = Math.min(1.0f / fogRed, Math.min(1.0f / fogGreen, 1.0f / fogBlue));
            fogRed = fogRed * (1.0f - v) + fogRed * w * v;
            fogGreen = fogGreen * (1.0f - v) + fogGreen * w * v;
            fogBlue = fogBlue * (1.0f - v) + fogBlue * w * v;
        }
        RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0f);
    }

    public static void setupNoFog() {
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
    }

    public static void setupFog(Camera camera, FogMode fogMode, float f, boolean bl) {
        float h;
        float g;
        FogType fogType = camera.getFluidInCamera();
        Entity entity = camera.getEntity();
        FogShape fogShape = FogShape.SPHERE;
        if (fogType == FogType.LAVA) {
            if (entity.isSpectator()) {
                g = -8.0f;
                h = f * 0.5f;
            } else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.FIRE_RESISTANCE)) {
                g = 0.0f;
                h = 3.0f;
            } else {
                g = 0.25f;
                h = 1.0f;
            }
        } else if (fogType == FogType.POWDER_SNOW) {
            if (entity.isSpectator()) {
                g = -8.0f;
                h = f * 0.5f;
            } else {
                g = 0.0f;
                h = 2.0f;
            }
        } else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.BLINDNESS)) {
            int i = ((LivingEntity)entity).getEffect(MobEffects.BLINDNESS).getDuration();
            float j = Mth.lerp(Math.min(1.0f, (float)i / 20.0f), f, 5.0f);
            if (fogMode == FogMode.FOG_SKY) {
                g = 0.0f;
                h = j * 0.8f;
            } else {
                g = fogType == FogType.WATER ? -4.0f : j * 0.25f;
                h = j;
            }
        } else if (fogType == FogType.WATER) {
            g = -8.0f;
            h = 96.0f;
            if (entity instanceof LocalPlayer) {
                LocalPlayer localPlayer = (LocalPlayer)entity;
                h *= Math.max(0.25f, localPlayer.getWaterVision());
                Biome biome = localPlayer.level.getBiome(localPlayer.blockPosition());
                if (biome.getBiomeCategory() == Biome.BiomeCategory.SWAMP) {
                    h *= 0.85f;
                }
            }
            if (h > f) {
                h = f;
                fogShape = FogShape.CYLINDER;
            }
        } else if (bl) {
            g = f * 0.05f;
            h = Math.min(f, 192.0f) * 0.5f;
        } else if (fogMode == FogMode.FOG_SKY) {
            g = 0.0f;
            h = f;
            fogShape = FogShape.CYLINDER;
        } else {
            float k = Mth.clamp(f / 10.0f, 4.0f, 64.0f);
            g = f - k;
            h = f;
            fogShape = FogShape.CYLINDER;
        }
        RenderSystem.setShaderFogStart(g);
        RenderSystem.setShaderFogEnd(h);
        RenderSystem.setShaderFogShape(fogShape);
    }

    public static void levelFogColor() {
        RenderSystem.setShaderFogColor(fogRed, fogGreen, fogBlue);
    }

    static {
        targetBiomeFog = -1;
        previousBiomeFog = -1;
        biomeChangedTime = -1L;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum FogMode {
        FOG_SKY,
        FOG_TERRAIN;

    }
}

