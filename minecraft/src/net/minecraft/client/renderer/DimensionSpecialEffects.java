package net.minecraft.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public abstract class DimensionSpecialEffects {
	private static final Object2ObjectMap<ResourceKey<DimensionType>, DimensionSpecialEffects> EFFECTS = Util.make(
		new Object2ObjectArrayMap<>(), object2ObjectArrayMap -> {
			DimensionSpecialEffects.OverworldEffects overworldEffects = new DimensionSpecialEffects.OverworldEffects();
			object2ObjectArrayMap.defaultReturnValue(overworldEffects);
			object2ObjectArrayMap.put(DimensionType.OVERWORLD_LOCATION, overworldEffects);
			object2ObjectArrayMap.put(DimensionType.NETHER_LOCATION, new DimensionSpecialEffects.NetherEffects());
			object2ObjectArrayMap.put(DimensionType.END_LOCATION, new DimensionSpecialEffects.EndEffects());
		}
	);
	private final float[] sunriseCol = new float[4];
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

	public static DimensionSpecialEffects forType(Optional<ResourceKey<DimensionType>> optional) {
		return EFFECTS.get(optional.orElse(DimensionType.OVERWORLD_LOCATION));
	}

	@Nullable
	public float[] getSunriseColor(float f, float g) {
		float h = 0.4F;
		float i = Mth.cos(f * (float) (Math.PI * 2)) - 0.0F;
		float j = -0.0F;
		if (i >= -0.4F && i <= 0.4F) {
			float k = (i - -0.0F) / 0.4F * 0.5F + 0.5F;
			float l = 1.0F - (1.0F - Mth.sin(k * (float) Math.PI)) * 0.99F;
			l *= l;
			this.sunriseCol[0] = k * 0.3F + 0.7F;
			this.sunriseCol[1] = k * k * 0.7F + 0.2F;
			this.sunriseCol[2] = k * k * 0.0F + 0.2F;
			this.sunriseCol[3] = l;
			return this.sunriseCol;
		} else {
			return null;
		}
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

		@Nullable
		@Override
		public float[] getSunriseColor(float f, float g) {
			return null;
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
		public OverworldEffects() {
			super(128.0F, true, DimensionSpecialEffects.SkyType.NORMAL, false, false);
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
		NORMAL,
		END;
	}
}
