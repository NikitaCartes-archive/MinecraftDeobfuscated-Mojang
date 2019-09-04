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
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class FogRenderer {
    private final FloatBuffer blackBuffer = MemoryTracker.createFloatBuffer(16);
    private final FloatBuffer colorBuffer = MemoryTracker.createFloatBuffer(16);
    private float fogRed;
    private float fogGreen;
    private float fogBlue;
    private float oldRed = -1.0f;
    private float oldGreen = -1.0f;
    private float oldBlue = -1.0f;
    private int targetBiomeFog = -1;
    private int previousBiomeFog = -1;
    private long biomeChangedTime = -1L;
    private final GameRenderer renderer;
    private final Minecraft minecraft;

    public FogRenderer(GameRenderer gameRenderer) {
        this.renderer = gameRenderer;
        this.minecraft = gameRenderer.getMinecraft();
        this.blackBuffer.put(0.0f).put(0.0f).put(0.0f).put(1.0f).flip();
    }

    public void setupClearColor(Camera camera, float f) {
        MultiPlayerLevel level = this.minecraft.level;
        FluidState fluidState = camera.getFluidInCamera();
        if (fluidState.is(FluidTags.WATER)) {
            this.setWaterFogColor(camera, level);
        } else if (fluidState.is(FluidTags.LAVA)) {
            this.fogRed = 0.6f;
            this.fogGreen = 0.1f;
            this.fogBlue = 0.0f;
            this.biomeChangedTime = -1L;
        } else {
            this.setLandFogColor(camera, level, f);
            this.biomeChangedTime = -1L;
        }
        double d = camera.getPosition().y * level.dimension.getClearColorScale();
        if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
            int i = ((LivingEntity)camera.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
            d = i < 20 ? (d *= (double)(1.0f - (float)i / 20.0f)) : 0.0;
        }
        if (d < 1.0) {
            if (d < 0.0) {
                d = 0.0;
            }
            d *= d;
            this.fogRed = (float)((double)this.fogRed * d);
            this.fogGreen = (float)((double)this.fogGreen * d);
            this.fogBlue = (float)((double)this.fogBlue * d);
        }
        if (this.renderer.getDarkenWorldAmount(f) > 0.0f) {
            float g = this.renderer.getDarkenWorldAmount(f);
            this.fogRed = this.fogRed * (1.0f - g) + this.fogRed * 0.7f * g;
            this.fogGreen = this.fogGreen * (1.0f - g) + this.fogGreen * 0.6f * g;
            this.fogBlue = this.fogBlue * (1.0f - g) + this.fogBlue * 0.6f * g;
        }
        if (fluidState.is(FluidTags.WATER)) {
            float h;
            float g = 0.0f;
            if (camera.getEntity() instanceof LocalPlayer) {
                LocalPlayer localPlayer = (LocalPlayer)camera.getEntity();
                g = localPlayer.getWaterVision();
            }
            if ((h = 1.0f / this.fogRed) > 1.0f / this.fogGreen) {
                h = 1.0f / this.fogGreen;
            }
            if (h > 1.0f / this.fogBlue) {
                h = 1.0f / this.fogBlue;
            }
            this.fogRed = this.fogRed * (1.0f - g) + this.fogRed * h * g;
            this.fogGreen = this.fogGreen * (1.0f - g) + this.fogGreen * h * g;
            this.fogBlue = this.fogBlue * (1.0f - g) + this.fogBlue * h * g;
        } else if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.NIGHT_VISION)) {
            float g = this.renderer.getNightVisionScale((LivingEntity)camera.getEntity(), f);
            float h = 1.0f / this.fogRed;
            if (h > 1.0f / this.fogGreen) {
                h = 1.0f / this.fogGreen;
            }
            if (h > 1.0f / this.fogBlue) {
                h = 1.0f / this.fogBlue;
            }
            this.fogRed = this.fogRed * (1.0f - g) + this.fogRed * h * g;
            this.fogGreen = this.fogGreen * (1.0f - g) + this.fogGreen * h * g;
            this.fogBlue = this.fogBlue * (1.0f - g) + this.fogBlue * h * g;
        }
        RenderSystem.clearColor(this.fogRed, this.fogGreen, this.fogBlue, 0.0f);
    }

    private void setLandFogColor(Camera camera, Level level, float f) {
        float m;
        float g = 0.25f + 0.75f * (float)this.minecraft.options.renderDistance / 32.0f;
        g = 1.0f - (float)Math.pow(g, 0.25);
        Vec3 vec3 = level.getSkyColor(camera.getBlockPosition(), f);
        float h = (float)vec3.x;
        float i = (float)vec3.y;
        float j = (float)vec3.z;
        Vec3 vec32 = level.getFogColor(f);
        this.fogRed = (float)vec32.x;
        this.fogGreen = (float)vec32.y;
        this.fogBlue = (float)vec32.z;
        if (this.minecraft.options.renderDistance >= 4) {
            float[] fs;
            double d = Mth.sin(level.getSunAngle(f)) > 0.0f ? -1.0 : 1.0;
            Vec3 vec33 = new Vec3(d, 0.0, 0.0);
            float k = (float)camera.getLookVector().dot(vec33);
            if (k < 0.0f) {
                k = 0.0f;
            }
            if (k > 0.0f && (fs = level.dimension.getSunriseColor(level.getTimeOfDay(f), f)) != null) {
                this.fogRed = this.fogRed * (1.0f - (k *= fs[3])) + fs[0] * k;
                this.fogGreen = this.fogGreen * (1.0f - k) + fs[1] * k;
                this.fogBlue = this.fogBlue * (1.0f - k) + fs[2] * k;
            }
        }
        this.fogRed += (h - this.fogRed) * g;
        this.fogGreen += (i - this.fogGreen) * g;
        this.fogBlue += (j - this.fogBlue) * g;
        float l = level.getRainLevel(f);
        if (l > 0.0f) {
            m = 1.0f - l * 0.5f;
            float n = 1.0f - l * 0.4f;
            this.fogRed *= m;
            this.fogGreen *= m;
            this.fogBlue *= n;
        }
        if ((m = level.getThunderLevel(f)) > 0.0f) {
            float n = 1.0f - m * 0.5f;
            this.fogRed *= n;
            this.fogGreen *= n;
            this.fogBlue *= n;
        }
    }

    private void setWaterFogColor(Camera camera, LevelReader levelReader) {
        long l = Util.getMillis();
        int i = levelReader.getBiome(new BlockPos(camera.getPosition())).getWaterFogColor();
        if (this.biomeChangedTime < 0L) {
            this.targetBiomeFog = i;
            this.previousBiomeFog = i;
            this.biomeChangedTime = l;
        }
        int j = this.targetBiomeFog >> 16 & 0xFF;
        int k = this.targetBiomeFog >> 8 & 0xFF;
        int m = this.targetBiomeFog & 0xFF;
        int n = this.previousBiomeFog >> 16 & 0xFF;
        int o = this.previousBiomeFog >> 8 & 0xFF;
        int p = this.previousBiomeFog & 0xFF;
        float f = Mth.clamp((float)(l - this.biomeChangedTime) / 5000.0f, 0.0f, 1.0f);
        float g = Mth.lerp(f, n, j);
        float h = Mth.lerp(f, o, k);
        float q = Mth.lerp(f, p, m);
        this.fogRed = g / 255.0f;
        this.fogGreen = h / 255.0f;
        this.fogBlue = q / 255.0f;
        if (this.targetBiomeFog != i) {
            this.targetBiomeFog = i;
            this.previousBiomeFog = Mth.floor(g) << 16 | Mth.floor(h) << 8 | Mth.floor(q);
            this.biomeChangedTime = l;
        }
    }

    public void setupFog(Camera camera, int i) {
        this.resetFogColor(false);
        RenderSystem.normal3f(0.0f, -1.0f, 0.0f);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        FluidState fluidState = camera.getFluidInCamera();
        if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
            float f = 5.0f;
            int j = ((LivingEntity)camera.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
            if (j < 20) {
                f = Mth.lerp(1.0f - (float)j / 20.0f, 5.0f, this.renderer.getRenderDistance());
            }
            RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
            if (i == -1) {
                RenderSystem.fogStart(0.0f);
                RenderSystem.fogEnd(f * 0.8f);
            } else {
                RenderSystem.fogStart(f * 0.25f);
                RenderSystem.fogEnd(f);
            }
            RenderSystem.setupNvFogDistance();
        } else if (fluidState.is(FluidTags.WATER)) {
            RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
            if (camera.getEntity() instanceof LivingEntity) {
                if (camera.getEntity() instanceof LocalPlayer) {
                    LocalPlayer localPlayer = (LocalPlayer)camera.getEntity();
                    float g = 0.05f - localPlayer.getWaterVision() * localPlayer.getWaterVision() * 0.03f;
                    Biome biome = localPlayer.level.getBiome(new BlockPos(localPlayer));
                    if (biome == Biomes.SWAMP || biome == Biomes.SWAMP_HILLS) {
                        g += 0.005f;
                    }
                    RenderSystem.fogDensity(g);
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
            float f = this.renderer.getRenderDistance();
            RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
            if (i == -1) {
                RenderSystem.fogStart(0.0f);
                RenderSystem.fogEnd(f);
            } else {
                RenderSystem.fogStart(f * 0.75f);
                RenderSystem.fogEnd(f);
            }
            RenderSystem.setupNvFogDistance();
            if (this.minecraft.level.dimension.isFoggyAt(Mth.floor(camera.getPosition().x), Mth.floor(camera.getPosition().z)) || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog()) {
                RenderSystem.fogStart(f * 0.05f);
                RenderSystem.fogEnd(Math.min(f, 192.0f) * 0.5f);
            }
        }
        RenderSystem.enableColorMaterial();
        RenderSystem.enableFog();
        RenderSystem.colorMaterial(1028, 4608);
    }

    public void resetFogColor(boolean bl) {
        if (bl) {
            RenderSystem.fog(2918, this.blackBuffer);
        } else {
            RenderSystem.fog(2918, this.updateColorBuffer());
        }
    }

    private FloatBuffer updateColorBuffer() {
        if (this.oldRed != this.fogRed || this.oldGreen != this.fogGreen || this.oldBlue != this.fogBlue) {
            this.colorBuffer.clear();
            this.colorBuffer.put(this.fogRed).put(this.fogGreen).put(this.fogBlue).put(1.0f);
            this.colorBuffer.flip();
            this.oldRed = this.fogRed;
            this.oldGreen = this.fogGreen;
            this.oldBlue = this.fogBlue;
        }
        return this.colorBuffer;
    }
}

