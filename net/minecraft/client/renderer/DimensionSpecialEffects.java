/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class DimensionSpecialEffects {
    private static final Object2ObjectMap<ResourceLocation, DimensionSpecialEffects> EFFECTS = Util.make(new Object2ObjectArrayMap(), object2ObjectArrayMap -> {
        OverworldEffects overworldEffects = new OverworldEffects();
        object2ObjectArrayMap.defaultReturnValue(overworldEffects);
        object2ObjectArrayMap.put(BuiltinDimensionTypes.OVERWORLD_EFFECTS, overworldEffects);
        object2ObjectArrayMap.put(BuiltinDimensionTypes.NETHER_EFFECTS, new NetherEffects());
        object2ObjectArrayMap.put(BuiltinDimensionTypes.END_EFFECTS, new EndEffects());
    });
    private final float[] sunriseCol = new float[4];
    private final float cloudLevel;
    private final boolean hasGround;
    private final SkyType skyType;
    private final boolean forceBrightLightmap;
    private final boolean constantAmbientLight;

    public DimensionSpecialEffects(float f, boolean bl, SkyType skyType, boolean bl2, boolean bl3) {
        this.cloudLevel = f;
        this.hasGround = bl;
        this.skyType = skyType;
        this.forceBrightLightmap = bl2;
        this.constantAmbientLight = bl3;
    }

    public static DimensionSpecialEffects forType(DimensionType dimensionType) {
        return (DimensionSpecialEffects)EFFECTS.get(dimensionType.effectsLocation());
    }

    @Nullable
    public float[] getSunriseColor(float f, float g) {
        float h = 0.4f;
        float i = Mth.cos(f * ((float)Math.PI * 2)) - 0.0f;
        float j = -0.0f;
        if (i >= -0.4f && i <= 0.4f) {
            float k = (i - -0.0f) / 0.4f * 0.5f + 0.5f;
            float l = 1.0f - (1.0f - Mth.sin(k * (float)Math.PI)) * 0.99f;
            l *= l;
            this.sunriseCol[0] = k * 0.3f + 0.7f;
            this.sunriseCol[1] = k * k * 0.7f + 0.2f;
            this.sunriseCol[2] = k * k * 0.0f + 0.2f;
            this.sunriseCol[3] = l;
            return this.sunriseCol;
        }
        return null;
    }

    public float getCloudHeight() {
        return this.cloudLevel;
    }

    public boolean hasGround() {
        return this.hasGround;
    }

    public abstract Vec3 getBrightnessDependentFogColor(Vec3 var1, float var2);

    public abstract boolean isFoggyAt(int var1, int var2);

    public SkyType skyType() {
        return this.skyType;
    }

    public boolean forceBrightLightmap() {
        return this.forceBrightLightmap;
    }

    public boolean constantAmbientLight() {
        return this.constantAmbientLight;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum SkyType {
        NONE,
        NORMAL,
        END;

    }

    @Environment(value=EnvType.CLIENT)
    public static class OverworldEffects
    extends DimensionSpecialEffects {
        public static final int CLOUD_LEVEL = 192;

        public OverworldEffects() {
            super(192.0f, true, SkyType.NORMAL, false, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
            return vec3.multiply(f * 0.94f + 0.06f, f * 0.94f + 0.06f, f * 0.91f + 0.09f);
        }

        @Override
        public boolean isFoggyAt(int i, int j) {
            return false;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class NetherEffects
    extends DimensionSpecialEffects {
        public NetherEffects() {
            super(Float.NaN, true, SkyType.NONE, false, true);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
            return vec3;
        }

        @Override
        public boolean isFoggyAt(int i, int j) {
            return true;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class EndEffects
    extends DimensionSpecialEffects {
        public EndEffects() {
            super(Float.NaN, false, SkyType.END, true, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
            return vec3.scale(0.15f);
        }

        @Override
        public boolean isFoggyAt(int i, int j) {
            return false;
        }

        @Override
        @Nullable
        public float[] getSunriseColor(float f, float g) {
            return null;
        }
    }
}

