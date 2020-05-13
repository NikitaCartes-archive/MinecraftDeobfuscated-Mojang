/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class DimensionSpecialEffects {
    private static final Map<DimensionType, DimensionSpecialEffects> EFFECTS = Maps.newHashMap();
    private final float[] sunriseCol = new float[4];
    private final float cloudLevel;
    private final boolean hasGround;
    private final boolean renderNormalSky;

    public DimensionSpecialEffects(float f, boolean bl, boolean bl2) {
        this.cloudLevel = f;
        this.hasGround = bl;
        this.renderNormalSky = bl2;
    }

    public static DimensionSpecialEffects forType(DimensionType dimensionType) {
        return EFFECTS.getOrDefault(dimensionType, EFFECTS.get(DimensionType.OVERWORLD));
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

    public boolean renderNormalSky() {
        return this.renderNormalSky;
    }

    static {
        EFFECTS.put(DimensionType.OVERWORLD, new OverworldEffects());
        EFFECTS.put(DimensionType.NETHER, new NetherEffects());
        EFFECTS.put(DimensionType.THE_END, new EndEffects());
    }

    @Environment(value=EnvType.CLIENT)
    public static class EndEffects
    extends DimensionSpecialEffects {
        public EndEffects() {
            super(Float.NaN, false, false);
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

    @Environment(value=EnvType.CLIENT)
    public static class OverworldEffects
    extends DimensionSpecialEffects {
        public OverworldEffects() {
            super(128.0f, true, true);
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
            super(Float.NaN, true, false);
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
}

