package net.minecraft.world.level.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public record DimensionType(
	OptionalLong fixedTime,
	boolean hasSkyLight,
	boolean hasCeiling,
	boolean ultraWarm,
	boolean natural,
	double coordinateScale,
	boolean bedWorks,
	boolean respawnAnchorWorks,
	int minY,
	int height,
	int logicalHeight,
	TagKey<Block> infiniburn,
	ResourceLocation effectsLocation,
	float ambientLight,
	DimensionType.MonsterSettings monsterSettings
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
						ExtraCodecs.asOptionalLong(Codec.LONG.lenientOptionalFieldOf("fixed_time")).forGetter(DimensionType::fixedTime),
						Codec.BOOL.fieldOf("has_skylight").forGetter(DimensionType::hasSkyLight),
						Codec.BOOL.fieldOf("has_ceiling").forGetter(DimensionType::hasCeiling),
						Codec.BOOL.fieldOf("ultrawarm").forGetter(DimensionType::ultraWarm),
						Codec.BOOL.fieldOf("natural").forGetter(DimensionType::natural),
						Codec.doubleRange(1.0E-5F, 3.0E7).fieldOf("coordinate_scale").forGetter(DimensionType::coordinateScale),
						Codec.BOOL.fieldOf("bed_works").forGetter(DimensionType::bedWorks),
						Codec.BOOL.fieldOf("respawn_anchor_works").forGetter(DimensionType::respawnAnchorWorks),
						Codec.intRange(MIN_Y, MAX_Y).fieldOf("min_y").forGetter(DimensionType::minY),
						Codec.intRange(16, Y_SIZE).fieldOf("height").forGetter(DimensionType::height),
						Codec.intRange(0, Y_SIZE).fieldOf("logical_height").forGetter(DimensionType::logicalHeight),
						TagKey.hashedCodec(Registries.BLOCK).fieldOf("infiniburn").forGetter(DimensionType::infiniburn),
						ResourceLocation.CODEC.fieldOf("effects").orElse(BuiltinDimensionTypes.OVERWORLD_EFFECTS).forGetter(DimensionType::effectsLocation),
						Codec.FLOAT.fieldOf("ambient_light").forGetter(DimensionType::ambientLight),
						DimensionType.MonsterSettings.CODEC.forGetter(DimensionType::monsterSettings)
					)
					.apply(instance, DimensionType::new)
		)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DimensionType>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.DIMENSION_TYPE);
	private static final int MOON_PHASES = 8;
	public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
	public static final Codec<Holder<DimensionType>> CODEC = RegistryFileCodec.create(Registries.DIMENSION_TYPE, DIRECT_CODEC);

	public DimensionType(
		OptionalLong fixedTime,
		boolean hasSkyLight,
		boolean hasCeiling,
		boolean ultraWarm,
		boolean natural,
		double coordinateScale,
		boolean bedWorks,
		boolean respawnAnchorWorks,
		int minY,
		int height,
		int logicalHeight,
		TagKey<Block> infiniburn,
		ResourceLocation effectsLocation,
		float ambientLight,
		DimensionType.MonsterSettings monsterSettings
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
			this.bedWorks = bedWorks;
			this.respawnAnchorWorks = respawnAnchorWorks;
			this.minY = minY;
			this.height = height;
			this.logicalHeight = logicalHeight;
			this.infiniburn = infiniburn;
			this.effectsLocation = effectsLocation;
			this.ambientLight = ambientLight;
			this.monsterSettings = monsterSettings;
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

	public boolean piglinSafe() {
		return this.monsterSettings.piglinSafe();
	}

	public boolean hasRaids() {
		return this.monsterSettings.hasRaids();
	}

	public IntProvider monsterSpawnLightTest() {
		return this.monsterSettings.monsterSpawnLightTest();
	}

	public int monsterSpawnBlockLightLimit() {
		return this.monsterSettings.monsterSpawnBlockLightLimit();
	}

	public static record MonsterSettings(boolean piglinSafe, boolean hasRaids, IntProvider monsterSpawnLightTest, int monsterSpawnBlockLightLimit) {
		public static final MapCodec<DimensionType.MonsterSettings> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						Codec.BOOL.fieldOf("piglin_safe").forGetter(DimensionType.MonsterSettings::piglinSafe),
						Codec.BOOL.fieldOf("has_raids").forGetter(DimensionType.MonsterSettings::hasRaids),
						IntProvider.codec(0, 15).fieldOf("monster_spawn_light_level").forGetter(DimensionType.MonsterSettings::monsterSpawnLightTest),
						Codec.intRange(0, 15).fieldOf("monster_spawn_block_light_limit").forGetter(DimensionType.MonsterSettings::monsterSpawnBlockLightLimit)
					)
					.apply(instance, DimensionType.MonsterSettings::new)
		);
	}
}
