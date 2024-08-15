package net.minecraft.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public abstract class DimensionSpecialEffects {
	private static final Object2ObjectMap<ResourceLocation, DimensionSpecialEffects> EFFECTS = Util.make(new Object2ObjectArrayMap<>(), object2ObjectArrayMap -> {
		DimensionSpecialEffects.OverworldEffects overworldEffects = new DimensionSpecialEffects.OverworldEffects();
		object2ObjectArrayMap.defaultReturnValue(overworldEffects);
		object2ObjectArrayMap.put(BuiltinDimensionTypes.OVERWORLD_EFFECTS, overworldEffects);
		object2ObjectArrayMap.put(BuiltinDimensionTypes.NETHER_EFFECTS, new DimensionSpecialEffects.NetherEffects());
		object2ObjectArrayMap.put(BuiltinDimensionTypes.END_EFFECTS, new DimensionSpecialEffects.EndEffects());
	});
	private final float cloudLevel;
	private final boolean hasGround;
	private final DimensionSpecialEffects.SkyType skyType;
	private final boolean forceBrightLightmap;
	private final boolean constantAmbientLight;

	public DimensionSpecialEffects(float f, boolean bl, DimensionSpecialEffects.SkyType skyType, boolean bl2, boolean bl3) {
		this.cloudLevel = f;
		this.hasGround = bl;
		this.skyType = skyType;
		this.forceBrightLightmap = bl2;
		this.constantAmbientLight = bl3;
	}

	public static DimensionSpecialEffects forType(DimensionType dimensionType) {
		return EFFECTS.get(dimensionType.effectsLocation());
	}

	public boolean isSunriseOrSunset(float f) {
		return false;
	}

	public int getSunriseOrSunsetColor(float f) {
		return 0;
	}

	public float getCloudHeight() {
		return this.cloudLevel;
	}

	public boolean hasGround() {
		return this.hasGround;
	}

	public abstract Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f);

	public abstract boolean isFoggyAt(int i, int j);

	public DimensionSpecialEffects.SkyType skyType() {
		return this.skyType;
	}

	public boolean forceBrightLightmap() {
		return this.forceBrightLightmap;
	}

	public boolean constantAmbientLight() {
		return this.constantAmbientLight;
	}

	@Environment(EnvType.CLIENT)
	public static class EndEffects extends DimensionSpecialEffects {
		public EndEffects() {
			super(Float.NaN, false, DimensionSpecialEffects.SkyType.END, true, false);
		}

		@Override
		public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
			return vec3.scale(0.15F);
		}

		@Override
		public boolean isFoggyAt(int i, int j) {
			return false;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class NetherEffects extends DimensionSpecialEffects {
		public NetherEffects() {
			super(Float.NaN, true, DimensionSpecialEffects.SkyType.NONE, false, true);
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

	@Environment(EnvType.CLIENT)
	public static class OverworldEffects extends DimensionSpecialEffects {
		public static final int CLOUD_LEVEL = 192;
		private static final float SUNRISE_AND_SUNSET_TIMESPAN = 0.4F;

		public OverworldEffects() {
			super(192.0F, true, DimensionSpecialEffects.SkyType.OVERWORLD, false, false);
		}

		@Override
		public boolean isSunriseOrSunset(float f) {
			float g = Mth.cos(f * (float) (Math.PI * 2));
			return g >= -0.4F && g <= 0.4F;
		}

		@Override
		public int getSunriseOrSunsetColor(float f) {
			float g = Mth.cos(f * (float) (Math.PI * 2));
			float h = g / 0.4F * 0.5F + 0.5F;
			float i = Mth.square(1.0F - (1.0F - Mth.sin(h * (float) Math.PI)) * 0.99F);
			return ARGB.colorFromFloat(i, h * 0.3F + 0.7F, h * h * 0.7F + 0.2F, 0.2F);
		}

		@Override
		public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
			return vec3.multiply((double)(f * 0.94F + 0.06F), (double)(f * 0.94F + 0.06F), (double)(f * 0.91F + 0.09F));
		}

		@Override
		public boolean isFoggyAt(int i, int j) {
			return false;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum SkyType {
		NONE,
		OVERWORLD,
		END;
	}
}
