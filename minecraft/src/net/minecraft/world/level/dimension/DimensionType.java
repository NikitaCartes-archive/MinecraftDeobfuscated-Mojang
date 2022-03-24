package net.minecraft.world.level.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public record DimensionType(
	OptionalLong fixedTime,
	boolean hasSkyLight,
	boolean hasCeiling,
	boolean ultraWarm,
	boolean natural,
	double coordinateScale,
	boolean piglinSafe,
	boolean bedWorks,
	boolean respawnAnchorWorks,
	boolean hasRaids,
	int minY,
	int height,
	int logicalHeight,
	TagKey<Block> infiniburn,
	ResourceLocation effectsLocation,
	float ambientLight
) {
	public static final int BITS_FOR_Y = BlockPos.PACKED_Y_LENGTH;
	public static final int MIN_HEIGHT = 16;
	public static final int Y_SIZE = (1 << BITS_FOR_Y) - 32;
	public static final int MAX_Y = (Y_SIZE >> 1) - 1;
	public static final int MIN_Y = MAX_Y - Y_SIZE + 1;
	public static final int WAY_ABOVE_MAX_Y = MAX_Y << 4;
	public static final int WAY_BELOW_MIN_Y = MIN_Y << 4;
	public static final Codec<DimensionType> DIRECT_CODEC = ExtraCodecs.catchDecoderException(
		RecordCodecBuilder.create(
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
	);
	private static final int MOON_PHASES = 8;
	public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
	public static final Codec<Holder<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, DIRECT_CODEC);

	public DimensionType(
		OptionalLong fixedTime,
		boolean hasSkyLight,
		boolean hasCeiling,
		boolean ultraWarm,
		boolean natural,
		double coordinateScale,
		boolean piglinSafe,
		boolean bedWorks,
		boolean respawnAnchorWorks,
		boolean hasRaids,
		int minY,
		int height,
		int logicalHeight,
		TagKey<Block> infiniburn,
		ResourceLocation effectsLocation,
		float ambientLight
	) {
		if (height < 16) {
			throw new IllegalStateException("height has to be at least 16");
		} else if (minY + height > MAX_Y + 1) {
			throw new IllegalStateException("min_y + height cannot be higher than: " + (MAX_Y + 1));
		} else if (logicalHeight > height) {
			throw new IllegalStateException("logical_height cannot be higher than height");
		} else if (height % 16 != 0) {
			throw new IllegalStateException("height has to be multiple of 16");
		} else if (minY % 16 != 0) {
			throw new IllegalStateException("min_y has to be a multiple of 16");
		} else {
			this.fixedTime = fixedTime;
			this.hasSkyLight = hasSkyLight;
			this.hasCeiling = hasCeiling;
			this.ultraWarm = ultraWarm;
			this.natural = natural;
			this.coordinateScale = coordinateScale;
			this.piglinSafe = piglinSafe;
			this.bedWorks = bedWorks;
			this.respawnAnchorWorks = respawnAnchorWorks;
			this.hasRaids = hasRaids;
			this.minY = minY;
			this.height = height;
			this.logicalHeight = logicalHeight;
			this.infiniburn = infiniburn;
			this.effectsLocation = effectsLocation;
			this.ambientLight = ambientLight;
		}
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
}
