/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class FogRenderer {
    private static final int WATER_FOG_DISTANCE = 96;
    private static final List<MobEffectFogFunction> MOB_EFFECT_FOG = Lists.newArrayList(new BlindnessFogFunction(), new DarknessFogFunction());
    public static final float BIOME_FOG_TRANSITION_TIME = 5000.0f;
    private static float fogRed;
    private static float fogGreen;
    private static float fogBlue;
    private static int targetBiomeFog;
    private static int previousBiomeFog;
    private static long biomeChangedTime;

    public static void setupColor(Camera camera, float f, ClientLevel clientLevel, int i2, float g) {
        FogType fogType = camera.getFluidInCamera();
        Entity entity = camera.getEntity();
        if (fogType == FogType.WATER) {
            long l = Util.getMillis();
            int j2 = clientLevel.getBiome(new BlockPos(camera.getPosition())).value().getWaterFogColor();
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
            Vec3 vec33 = CubicSampler.gaussianSampleVec3(vec32, (i, j, k) -> clientLevel.effects().getBrightnessDependentFogColor(Vec3.fromRGB24(biomeManager.getNoiseBiomeAtQuart(i, j, k).value().getFogColor()), y));
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
        float u = ((float)camera.getPosition().y - (float)clientLevel.getMinBuildHeight()) * clientLevel.getLevelData().getClearColorScale();
        MobEffectFogFunction mobEffectFogFunction = FogRenderer.getPriorityFogFunction(entity, f);
        if (mobEffectFogFunction != null) {
            LivingEntity livingEntity = (LivingEntity)entity;
            u = mobEffectFogFunction.getModifiedVoidDarkness(livingEntity, livingEntity.getEffect(mobEffectFogFunction.getMobEffect()), u, f);
        }
        if (u < 1.0f && fogType != FogType.LAVA && fogType != FogType.POWDER_SNOW) {
            if (u < 0.0f) {
                u = 0.0f;
            }
            u *= u;
            fogRed *= u;
            fogGreen *= u;
            fogBlue *= u;
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

    @Nullable
    private static MobEffectFogFunction getPriorityFogFunction(Entity entity, float f) {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            return MOB_EFFECT_FOG.stream().filter(mobEffectFogFunction -> mobEffectFogFunction.isEnabled(livingEntity, f)).findFirst().orElse(null);
        }
        return null;
    }

    public static void setupFog(Camera camera, FogMode fogMode, float f, boolean bl, float g) {
        FogType fogType = camera.getFluidInCamera();
        Entity entity = camera.getEntity();
        FogData fogData = new FogData(fogMode);
        MobEffectFogFunction mobEffectFogFunction = FogRenderer.getPriorityFogFunction(entity, g);
        if (fogType == FogType.LAVA) {
            if (entity.isSpectator()) {
                fogData.start = -8.0f;
                fogData.end = f * 0.5f;
            } else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.FIRE_RESISTANCE)) {
                fogData.start = 0.0f;
                fogData.end = 3.0f;
            } else {
                fogData.start = 0.25f;
                fogData.end = 1.0f;
            }
        } else if (fogType == FogType.POWDER_SNOW) {
            if (entity.isSpectator()) {
                fogData.start = -8.0f;
                fogData.end = f * 0.5f;
            } else {
                fogData.start = 0.0f;
                fogData.end = 2.0f;
            }
        } else if (mobEffectFogFunction != null) {
            LivingEntity livingEntity = (LivingEntity)entity;
            MobEffectInstance mobEffectInstance = livingEntity.getEffect(mobEffectFogFunction.getMobEffect());
            if (mobEffectInstance != null) {
                mobEffectFogFunction.setupFog(fogData, livingEntity, mobEffectInstance, f, g);
            }
        } else if (fogType == FogType.WATER) {
            fogData.start = -8.0f;
            fogData.end = 96.0f;
            if (entity instanceof LocalPlayer) {
                LocalPlayer localPlayer = (LocalPlayer)entity;
                fogData.end *= Math.max(0.25f, localPlayer.getWaterVision());
                Holder<Biome> holder = localPlayer.level.getBiome(localPlayer.blockPosition());
                if (holder.is(BiomeTags.HAS_CLOSER_WATER_FOG)) {
                    fogData.end *= 0.85f;
                }
            }
            if (fogData.end > f) {
                fogData.end = f;
                fogData.shape = FogShape.CYLINDER;
            }
        } else if (bl) {
            fogData.start = f * 0.05f;
            fogData.end = Math.min(f, 192.0f) * 0.5f;
        } else if (fogMode == FogMode.FOG_SKY) {
            fogData.start = 0.0f;
            fogData.end = f;
            fogData.shape = FogShape.CYLINDER;
        } else {
            float h = Mth.clamp(f / 10.0f, 4.0f, 64.0f);
            fogData.start = f - h;
            fogData.end = f;
            fogData.shape = FogShape.CYLINDER;
        }
        RenderSystem.setShaderFogStart(fogData.start);
        RenderSystem.setShaderFogEnd(fogData.end);
        RenderSystem.setShaderFogShape(fogData.shape);
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
    static interface MobEffectFogFunction {
        public MobEffect getMobEffect();

        public void setupFog(FogData var1, LivingEntity var2, MobEffectInstance var3, float var4, float var5);

        default public boolean isEnabled(LivingEntity livingEntity, float f) {
            return livingEntity.hasEffect(this.getMobEffect());
        }

        default public float getModifiedVoidDarkness(LivingEntity livingEntity, MobEffectInstance mobEffectInstance, float f, float g) {
            MobEffectInstance mobEffectInstance2 = livingEntity.getEffect(this.getMobEffect());
            if (mobEffectInstance2 != null) {
                f = mobEffectInstance2.getDuration() < 20 ? 1.0f - (float)mobEffectInstance2.getDuration() / 20.0f : 0.0f;
            }
            return f;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class FogData {
        public final FogMode mode;
        public float start;
        public float end;
        public FogShape shape = FogShape.SPHERE;

        public FogData(FogMode fogMode) {
            this.mode = fogMode;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum FogMode {
        FOG_SKY,
        FOG_TERRAIN;

    }

    @Environment(value=EnvType.CLIENT)
    static class BlindnessFogFunction
    implements MobEffectFogFunction {
        BlindnessFogFunction() {
        }

        @Override
        public MobEffect getMobEffect() {
            return MobEffects.BLINDNESS;
        }

        @Override
        public void setupFog(FogData fogData, LivingEntity livingEntity, MobEffectInstance mobEffectInstance, float f, float g) {
            float h = Mth.lerp(Math.min(1.0f, (float)mobEffectInstance.getDuration() / 20.0f), f, 5.0f);
            if (fogData.mode == FogMode.FOG_SKY) {
                fogData.start = 0.0f;
                fogData.end = h * 0.8f;
            } else {
                fogData.start = h * 0.25f;
                fogData.end = h;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DarknessFogFunction
    implements MobEffectFogFunction {
        DarknessFogFunction() {
        }

        @Override
        public MobEffect getMobEffect() {
            return MobEffects.DARKNESS;
        }

        @Override
        public void setupFog(FogData fogData, LivingEntity livingEntity, MobEffectInstance mobEffectInstance, float f, float g) {
            if (mobEffectInstance.getFactorData().isEmpty()) {
                return;
            }
            float h = Mth.lerp(mobEffectInstance.getFactorData().get().getFactor(g), f, 15.0f);
            fogData.start = fogData.mode == FogMode.FOG_SKY ? 0.0f : h * 0.75f;
            fogData.end = h;
        }

        @Override
        public float getModifiedVoidDarkness(LivingEntity livingEntity, MobEffectInstance mobEffectInstance, float f, float g) {
            if (mobEffectInstance.getFactorData().isEmpty()) {
                return 0.0f;
            }
            return 1.0f - mobEffectInstance.getFactorData().get().getFactor(g);
        }
    }
}

