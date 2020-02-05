/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
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
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class FogRenderer {
    private static float fogRed;
    private static float fogGreen;
    private static float fogBlue;
    private static int targetBiomeFog;
    private static int previousBiomeFog;
    private static long biomeChangedTime;

    public static void setupColor(Camera camera, float f, ClientLevel clientLevel, int i, float g) {
        int j;
        FluidState fluidState = camera.getFluidInCamera();
        if (fluidState.is(FluidTags.WATER)) {
            long l = Util.getMillis();
            j = clientLevel.getBiome(new BlockPos(camera.getPosition())).getWaterFogColor();
            if (biomeChangedTime < 0L) {
                targetBiomeFog = j;
                previousBiomeFog = j;
                biomeChangedTime = l;
            }
            int k = targetBiomeFog >> 16 & 0xFF;
            int m = targetBiomeFog >> 8 & 0xFF;
            int n = targetBiomeFog & 0xFF;
            int o = previousBiomeFog >> 16 & 0xFF;
            int p = previousBiomeFog >> 8 & 0xFF;
            int q = previousBiomeFog & 0xFF;
            float h = Mth.clamp((float)(l - biomeChangedTime) / 5000.0f, 0.0f, 1.0f);
            float r = Mth.lerp(h, o, k);
            float s = Mth.lerp(h, p, m);
            float t = Mth.lerp(h, q, n);
            fogRed = r / 255.0f;
            fogGreen = s / 255.0f;
            fogBlue = t / 255.0f;
            if (targetBiomeFog != j) {
                targetBiomeFog = j;
                previousBiomeFog = Mth.floor(r) << 16 | Mth.floor(s) << 8 | Mth.floor(t);
                biomeChangedTime = l;
            }
        } else if (fluidState.is(FluidTags.LAVA)) {
            fogRed = 0.6f;
            fogGreen = 0.1f;
            fogBlue = 0.0f;
            biomeChangedTime = -1L;
        } else {
            float h;
            float r;
            float z;
            float u = 0.25f + 0.75f * (float)i / 32.0f;
            u = 1.0f - (float)Math.pow(u, 0.25);
            Vec3 vec3 = clientLevel.getSkyColor(camera.getBlockPosition(), f);
            float v = (float)vec3.x;
            float w = (float)vec3.y;
            float x = (float)vec3.z;
            int n = clientLevel.getBiome(camera.getBlockPosition()).getFogColor();
            float y = Mth.cos(clientLevel.getTimeOfDay(f) * ((float)Math.PI * 2)) * 2.0f + 0.5f;
            Vec3 vec32 = clientLevel.getDimension().getBrightnessDependentFogColor(n, Mth.clamp(y, 0.0f, 1.0f));
            fogRed = (float)vec32.x;
            fogGreen = (float)vec32.y;
            fogBlue = (float)vec32.z;
            if (i >= 4) {
                float[] fs;
                z = Mth.sin(clientLevel.getSunAngle(f)) > 0.0f ? -1.0f : 1.0f;
                Vector3f vector3f = new Vector3f(z, 0.0f, 0.0f);
                r = camera.getLookVector().dot(vector3f);
                if (r < 0.0f) {
                    r = 0.0f;
                }
                if (r > 0.0f && (fs = clientLevel.dimension.getSunriseColor(clientLevel.getTimeOfDay(f), f)) != null) {
                    fogRed = fogRed * (1.0f - (r *= fs[3])) + fs[0] * r;
                    fogGreen = fogGreen * (1.0f - r) + fs[1] * r;
                    fogBlue = fogBlue * (1.0f - r) + fs[2] * r;
                }
            }
            fogRed += (v - fogRed) * u;
            fogGreen += (w - fogGreen) * u;
            fogBlue += (x - fogBlue) * u;
            z = clientLevel.getRainLevel(f);
            if (z > 0.0f) {
                float h2 = 1.0f - z * 0.5f;
                r = 1.0f - z * 0.4f;
                fogRed *= h2;
                fogGreen *= h2;
                fogBlue *= r;
            }
            if ((h = clientLevel.getThunderLevel(f)) > 0.0f) {
                r = 1.0f - h * 0.5f;
                fogRed *= r;
                fogGreen *= r;
                fogBlue *= r;
            }
            biomeChangedTime = -1L;
        }
        double d = camera.getPosition().y * clientLevel.dimension.getClearColorScale();
        if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
            j = ((LivingEntity)camera.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
            d = j < 20 ? (d *= (double)(1.0f - (float)j / 20.0f)) : 0.0;
        }
        if (d < 1.0) {
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
        if (fluidState.is(FluidTags.WATER)) {
            float v = 0.0f;
            if (camera.getEntity() instanceof LocalPlayer) {
                LocalPlayer localPlayer = (LocalPlayer)camera.getEntity();
                v = localPlayer.getWaterVision();
            }
            float w = Math.min(1.0f / fogRed, Math.min(1.0f / fogGreen, 1.0f / fogBlue));
            fogRed = fogRed * (1.0f - v) + fogRed * w * v;
            fogGreen = fogGreen * (1.0f - v) + fogGreen * w * v;
            fogBlue = fogBlue * (1.0f - v) + fogBlue * w * v;
        } else if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.NIGHT_VISION)) {
            float v = GameRenderer.getNightVisionScale((LivingEntity)camera.getEntity(), f);
            float w = Math.min(1.0f / fogRed, Math.min(1.0f / fogGreen, 1.0f / fogBlue));
            fogRed = fogRed * (1.0f - v) + fogRed * w * v;
            fogGreen = fogGreen * (1.0f - v) + fogGreen * w * v;
            fogBlue = fogBlue * (1.0f - v) + fogBlue * w * v;
        }
        RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0f);
    }

    public static void setupNoFog() {
        RenderSystem.fogDensity(0.0f);
        RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
    }

    public static void setupFog(Camera camera, FogMode fogMode, float f, boolean bl) {
        boolean bl2;
        FluidState fluidState = camera.getFluidInCamera();
        Entity entity = camera.getEntity();
        boolean bl3 = bl2 = fluidState.getType() != Fluids.EMPTY;
        if (bl2) {
            float g = 1.0f;
            if (fluidState.is(FluidTags.WATER)) {
                g = 0.05f;
                if (entity instanceof LocalPlayer) {
                    LocalPlayer localPlayer = (LocalPlayer)entity;
                    g -= localPlayer.getWaterVision() * localPlayer.getWaterVision() * 0.03f;
                    Biome biome = localPlayer.level.getBiome(new BlockPos(localPlayer));
                    if (biome == Biomes.SWAMP || biome == Biomes.SWAMP_HILLS) {
                        g += 0.005f;
                    }
                }
            } else if (fluidState.is(FluidTags.LAVA)) {
                g = 2.0f;
            }
            RenderSystem.fogDensity(g);
            RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
        } else {
            float j;
            float g;
            if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.BLINDNESS)) {
                int i = ((LivingEntity)entity).getEffect(MobEffects.BLINDNESS).getDuration();
                float h = Mth.lerp(Math.min(1.0f, (float)i / 20.0f), f, 5.0f);
                if (fogMode == FogMode.FOG_SKY) {
                    g = 0.0f;
                    j = h * 0.8f;
                } else {
                    g = h * 0.25f;
                    j = h;
                }
            } else if (bl) {
                g = f * 0.05f;
                j = Math.min(f, 192.0f) * 0.5f;
            } else if (fogMode == FogMode.FOG_SKY) {
                g = 0.0f;
                j = f;
            } else {
                g = f * 0.75f;
                j = f;
            }
            RenderSystem.fogStart(g);
            RenderSystem.fogEnd(j);
            RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
            RenderSystem.setupNvFogDistance();
        }
    }

    public static void levelFogColor() {
        RenderSystem.fog(2918, fogRed, fogGreen, fogBlue, 1.0f);
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

