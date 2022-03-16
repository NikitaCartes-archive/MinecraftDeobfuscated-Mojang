package net.minecraft.world.level.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class DimensionType {
	public static final int BITS_FOR_Y = BlockPos.PACKED_Y_LENGTH;
	public static final int MIN_HEIGHT = 16;
	public static final int Y_SIZE = (1 << BITS_FOR_Y) - 32;
	public static final int MAX_Y = (Y_SIZE >> 1) - 1;
	public static final int MIN_Y = MAX_Y - Y_SIZE + 1;
	public static final int WAY_ABOVE_MAX_Y = MAX_Y << 4;
	public static final int WAY_BELOW_MIN_Y = MIN_Y << 4;
	public static final Codec<DimensionType> DIRECT_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.LONG
							.optionalFieldOf("fixed_time")
							.xmap(
								optional -> (OptionalLong)optional.map(OptionalLong::of).orElseGet(OptionalLong::empty),
								optionalLong -> optionalLong.isPresent() ? Optional.of(optionalLong.getAsLong()) : Optional.empty()
							)
							.forGetter(dimensionType -> dimensionType.fixedTime),
						Codec.BOOL.fieldOf("has_skylight").forGetter(DimensionType::hasSkyLight),
						Codec.BOOL.fieldOf("has_ceiling").forGetter(DimensionType::hasCeiling),
						Codec.BOOL.fieldOf("ultrawarm").forGetter(DimensionType::ultraWarm),
						Codec.BOOL.fieldOf("natural").forGetter(DimensionType::natural),
						Codec.doubleRange(1.0E-5F, 3.0E7).fieldOf("coordinate_scale").forGetter(DimensionType::coordinateScale),
						Codec.BOOL.fieldOf("piglin_safe").forGetter(DimensionType::piglinSafe),
						Codec.BOOL.fieldOf("bed_works").forGetter(DimensionType::bedWorks),
						Codec.BOOL.fieldOf("respawn_anchor_works").forGetter(DimensionType::respawnAnchorWorks),
						Codec.BOOL.fieldOf("has_raids").forGetter(DimensionType::hasRaids),
						Codec.intRange(MIN_Y, MAX_Y).fieldOf("min_y").forGetter(DimensionType::minY),
						Codec.intRange(16, Y_SIZE).fieldOf("height").forGetter(DimensionType::height),
						Codec.intRange(0, Y_SIZE).fieldOf("logical_height").forGetter(DimensionType::logicalHeight),
						TagKey.hashedCodec(Registry.BLOCK_REGISTRY).fieldOf("infiniburn").forGetter(dimensionType -> dimensionType.infiniburn),
						ResourceLocation.CODEC.fieldOf("effects").orElse(BuiltinDimensionTypes.OVERWORLD_EFFECTS).forGetter(dimensionType -> dimensionType.effectsLocation),
						Codec.FLOAT.fieldOf("ambient_light").forGetter(dimensionType -> dimensionType.ambientLight)
					)
					.apply(instance, DimensionType::new)
		)
		.comapFlatMap(DimensionType::guardY, Function.identity());
	private static final int MOON_PHASES = 8;
	public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
	public static final Codec<Holder<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, DIRECT_CODEC);
	private final OptionalLong fixedTime;
	private final boolean hasSkylight;
	private final boolean hasCeiling;
	private final boolean ultraWarm;
	private final boolean natural;
	private final double coordinateScale;
	private final boolean createDragonFight;
	private final boolean piglinSafe;
	private final boolean bedWorks;
	private final boolean respawnAnchorWorks;
	private final boolean hasRaids;
	private final int minY;
	private final int height;
	private final int logicalHeight;
	private final TagKey<Block> infiniburn;
	private final ResourceLocation effectsLocation;
	private final float ambientLight;
	private final transient float[] brightnessRamp;

	private static DataResult<DimensionType> guardY(DimensionType dimensionType) {
		if (dimensionType.height() < 16) {
			return DataResult.error("height has to be at least 16");
		} else if (dimensionType.minY() + dimensionType.height() > MAX_Y + 1) {
			return DataResult.error("min_y + height cannot be higher than: " + (MAX_Y + 1));
		} else if (dimensionType.logicalHeight() > dimensionType.height()) {
			return DataResult.error("logical_height cannot be higher than height");
		} else if (dimensionType.height() % 16 != 0) {
			return DataResult.error("height has to be multiple of 16");
		} else {
			return dimensionType.minY() % 16 != 0 ? DataResult.error("min_y has to be a multiple of 16") : DataResult.success(dimensionType);
		}
	}

	private DimensionType(
		OptionalLong optionalLong,
		boolean bl,
		boolean bl2,
		boolean bl3,
		boolean bl4,
		double d,
		boolean bl5,
		boolean bl6,
		boolean bl7,
		boolean bl8,
		int i,
		int j,
		int k,
		TagKey<Block> tagKey,
		ResourceLocation resourceLocation,
		float f
	) {
		this(optionalLong, bl, bl2, bl3, bl4, d, false, bl5, bl6, bl7, bl8, i, j, k, tagKey, resourceLocation, f);
	}

	public static DimensionType create(
		OptionalLong optionalLong,
		boolean bl,
		boolean bl2,
		boolean bl3,
		boolean bl4,
		double d,
		boolean bl5,
		boolean bl6,
		boolean bl7,
		boolean bl8,
		boolean bl9,
		int i,
		int j,
		int k,
		TagKey<Block> tagKey,
		ResourceLocation resourceLocation,
		float f
	) {
		DimensionType dimensionType = new DimensionType(optionalLong, bl, bl2, bl3, bl4, d, bl5, bl6, bl7, bl8, bl9, i, j, k, tagKey, resourceLocation, f);
		guardY(dimensionType).error().ifPresent(partialResult -> {
			throw new IllegalStateException(partialResult.message());
		});
		return dimensionType;
	}

	@Deprecated
	private DimensionType(
		OptionalLong optionalLong,
		boolean bl,
		boolean bl2,
		boolean bl3,
		boolean bl4,
		double d,
		boolean bl5,
		boolean bl6,
		boolean bl7,
		boolean bl8,
		boolean bl9,
		int i,
		int j,
		int k,
		TagKey<Block> tagKey,
		ResourceLocation resourceLocation,
		float f
	) {
		this.fixedTime = optionalLong;
		this.hasSkylight = bl;
		this.hasCeiling = bl2;
		this.ultraWarm = bl3;
		this.natural = bl4;
		this.coordinateScale = d;
		this.createDragonFight = bl5;
		this.piglinSafe = bl6;
		this.bedWorks = bl7;
		this.respawnAnchorWorks = bl8;
		this.hasRaids = bl9;
		this.minY = i;
		this.height = j;
		this.logicalHeight = k;
		this.infiniburn = tagKey;
		this.effectsLocation = resourceLocation;
		this.ambientLight = f;
		this.brightnessRamp = fillBrightnessRamp(f);
	}

	private static float[] fillBrightnessRamp(float f) {
		float[] fs = new float[16];

		for (int i = 0; i <= 15; i++) {
			float g = (float)i / 15.0F;
			float h = g / (4.0F - 3.0F * g);
			fs[i] = Mth.lerp(f, h, 1.0F);
		}

		return fs;
	}

	@Deprecated
	public static DataResult<ResourceKey<Level>> parseLegacy(Dynamic<?> dynamic) {
		Optional<Number> optional = dynamic.asNumber().result();
		if (optional.isPresent()) {
			int i = ((Number)optional.get()).intValue();
			if (i == -1) {
				return DataResult.success(Level.NETHER);
			}

			if (i == 0) {
				return DataResult.success(Level.OVERWORLD);
			}

			if (i == 1) {
				return DataResult.success(Level.END);
			}
		}

		return Level.RESOURCE_KEY_CODEC.parse(dynamic);
	}

	public static double getTeleportationScale(DimensionType dimensionType, DimensionType dimensionType2) {
		double d = dimensionType.coordinateScale();
		double e = dimensionType2.coordinateScale();
		return d / e;
	}

	public static Path getStorageFolder(ResourceKey<Level> resourceKey, Path path) {
		if (resourceKey == Level.OVERWORLD) {
			return path;
		} else if (resourceKey == Level.END) {
			return path.resolve("DIM1");
		} else {
			return resourceKey == Level.NETHER
				? path.resolve("DIM-1")
				: path.resolve("dimensions").resolve(resourceKey.location().getNamespace()).resolve(resourceKey.location().getPath());
		}
	}

	public boolean hasSkyLight() {
		return this.hasSkylight;
	}

	public boolean hasCeiling() {
		return this.hasCeiling;
	}

	public boolean ultraWarm() {
		return this.ultraWarm;
	}

	public boolean natural() {
		return this.natural;
	}

	public double coordinateScale() {
		return this.coordinateScale;
	}

	public boolean piglinSafe() {
		return this.piglinSafe;
	}

	public boolean bedWorks() {
		return this.bedWorks;
	}

	public boolean respawnAnchorWorks() {
		return this.respawnAnchorWorks;
	}

	public boolean hasRaids() {
		return this.hasRaids;
	}

	public int minY() {
		return this.minY;
	}

	public int height() {
		return this.height;
	}

	public int logicalHeight() {
		return this.logicalHeight;
	}

	public boolean createDragonFight() {
		return this.createDragonFight;
	}

	public boolean hasFixedTime() {
		return this.fixedTime.isPresent();
	}

	public float timeOfDay(long l) {
		double d = Mth.frac((double)this.fixedTime.orElse(l) / 24000.0 - 0.25);
		double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
		return (float)(d * 2.0 + e) / 3.0F;
	}

	public int moonPhase(long l) {
		return (int)(l / 24000L % 8L + 8L) % 8;
	}

	public float brightness(int i) {
		return this.brightnessRamp[i];
	}

	public TagKey<Block> infiniburn() {
		return this.infiniburn;
	}

	public ResourceLocation effectsLocation() {
		return this.effectsLocation;
	}
}
