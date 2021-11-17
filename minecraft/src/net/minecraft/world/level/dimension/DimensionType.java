package net.minecraft.world.level.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class DimensionType {
	public static final int BITS_FOR_Y = BlockPos.PACKED_Y_LENGTH;
	public static final int MIN_HEIGHT = 16;
	public static final int Y_SIZE = (1 << BITS_FOR_Y) - 32;
	public static final int MAX_Y = (Y_SIZE >> 1) - 1;
	public static final int MIN_Y = MAX_Y - Y_SIZE + 1;
	public static final int WAY_ABOVE_MAX_Y = MAX_Y << 4;
	public static final int WAY_BELOW_MIN_Y = MIN_Y << 4;
	public static final ResourceLocation OVERWORLD_EFFECTS = new ResourceLocation("overworld");
	public static final ResourceLocation NETHER_EFFECTS = new ResourceLocation("the_nether");
	public static final ResourceLocation END_EFFECTS = new ResourceLocation("the_end");
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
						ResourceLocation.CODEC.fieldOf("infiniburn").forGetter(dimensionType -> dimensionType.infiniburn),
						ResourceLocation.CODEC.fieldOf("effects").orElse(OVERWORLD_EFFECTS).forGetter(dimensionType -> dimensionType.effectsLocation),
						Codec.FLOAT.fieldOf("ambient_light").forGetter(dimensionType -> dimensionType.ambientLight)
					)
					.apply(instance, DimensionType::new)
		)
		.comapFlatMap(DimensionType::guardY, Function.identity());
	private static final int MOON_PHASES = 8;
	public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
	public static final ResourceKey<DimensionType> OVERWORLD_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld"));
	public static final ResourceKey<DimensionType> NETHER_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_nether"));
	public static final ResourceKey<DimensionType> END_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_end"));
	protected static final DimensionType DEFAULT_OVERWORLD = create(
		OptionalLong.empty(),
		true,
		false,
		false,
		true,
		1.0,
		false,
		false,
		true,
		false,
		true,
		-64,
		384,
		384,
		BlockTags.INFINIBURN_OVERWORLD.getName(),
		OVERWORLD_EFFECTS,
		0.0F
	);
	protected static final DimensionType DEFAULT_NETHER = create(
		OptionalLong.of(18000L),
		false,
		true,
		true,
		false,
		8.0,
		false,
		true,
		false,
		true,
		false,
		0,
		256,
		128,
		BlockTags.INFINIBURN_NETHER.getName(),
		NETHER_EFFECTS,
		0.1F
	);
	protected static final DimensionType DEFAULT_END = create(
		OptionalLong.of(6000L), false, false, false, false, 1.0, true, false, false, false, true, 0, 256, 256, BlockTags.INFINIBURN_END.getName(), END_EFFECTS, 0.0F
	);
	public static final ResourceKey<DimensionType> OVERWORLD_CAVES_LOCATION = ResourceKey.create(
		Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld_caves")
	);
	protected static final DimensionType DEFAULT_OVERWORLD_CAVES = create(
		OptionalLong.empty(),
		true,
		true,
		false,
		true,
		1.0,
		false,
		false,
		true,
		false,
		true,
		-64,
		384,
		384,
		BlockTags.INFINIBURN_OVERWORLD.getName(),
		OVERWORLD_EFFECTS,
		0.0F
	);
	public static final Codec<Supplier<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, DIRECT_CODEC);
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
	private final ResourceLocation infiniburn;
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
		ResourceLocation resourceLocation,
		ResourceLocation resourceLocation2,
		float f
	) {
		this(optionalLong, bl, bl2, bl3, bl4, d, false, bl5, bl6, bl7, bl8, i, j, k, resourceLocation, resourceLocation2, f);
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
		ResourceLocation resourceLocation,
		ResourceLocation resourceLocation2,
		float f
	) {
		DimensionType dimensionType = new DimensionType(optionalLong, bl, bl2, bl3, bl4, d, bl5, bl6, bl7, bl8, bl9, i, j, k, resourceLocation, resourceLocation2, f);
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
		ResourceLocation resourceLocation,
		ResourceLocation resourceLocation2,
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
		this.infiniburn = resourceLocation;
		this.effectsLocation = resourceLocation2;
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

	public static RegistryAccess registerBuiltin(RegistryAccess registryAccess) {
		WritableRegistry<DimensionType> writableRegistry = registryAccess.ownedRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		writableRegistry.register(OVERWORLD_LOCATION, DEFAULT_OVERWORLD, Lifecycle.stable());
		writableRegistry.register(OVERWORLD_CAVES_LOCATION, DEFAULT_OVERWORLD_CAVES, Lifecycle.stable());
		writableRegistry.register(NETHER_LOCATION, DEFAULT_NETHER, Lifecycle.stable());
		writableRegistry.register(END_LOCATION, DEFAULT_END, Lifecycle.stable());
		return registryAccess;
	}

	public static MappedRegistry<LevelStem> defaultDimensions(RegistryAccess registryAccess, long l) {
		return defaultDimensions(registryAccess, l, true);
	}

	public static MappedRegistry<LevelStem> defaultDimensions(RegistryAccess registryAccess, long l, boolean bl) {
		MappedRegistry<LevelStem> mappedRegistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
		Registry<DimensionType> registry = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<Biome> registry2 = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<NoiseGeneratorSettings> registry3 = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
		Registry<NormalNoise.NoiseParameters> registry4 = registryAccess.registryOrThrow(Registry.NOISE_REGISTRY);
		mappedRegistry.register(
			LevelStem.NETHER,
			new LevelStem(
				() -> registry.getOrThrow(NETHER_LOCATION),
				new NoiseBasedChunkGenerator(
					registry4, MultiNoiseBiomeSource.Preset.NETHER.biomeSource(registry2, bl), l, () -> registry3.getOrThrow(NoiseGeneratorSettings.NETHER)
				)
			),
			Lifecycle.stable()
		);
		mappedRegistry.register(
			LevelStem.END,
			new LevelStem(
				() -> registry.getOrThrow(END_LOCATION),
				new NoiseBasedChunkGenerator(registry4, new TheEndBiomeSource(registry2, l), l, () -> registry3.getOrThrow(NoiseGeneratorSettings.END))
			),
			Lifecycle.stable()
		);
		return mappedRegistry;
	}

	public static double getTeleportationScale(DimensionType dimensionType, DimensionType dimensionType2) {
		double d = dimensionType.coordinateScale();
		double e = dimensionType2.coordinateScale();
		return d / e;
	}

	@Deprecated
	public String getFileSuffix() {
		return this.equalTo(DEFAULT_END) ? "_end" : "";
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

	public Tag<Block> infiniburn() {
		Tag<Block> tag = BlockTags.getAllTags().getTag(this.infiniburn);
		return (Tag<Block>)(tag != null ? tag : BlockTags.INFINIBURN_OVERWORLD);
	}

	public ResourceLocation effectsLocation() {
		return this.effectsLocation;
	}

	public boolean equalTo(DimensionType dimensionType) {
		return this == dimensionType
			? true
			: this.hasSkylight == dimensionType.hasSkylight
				&& this.hasCeiling == dimensionType.hasCeiling
				&& this.ultraWarm == dimensionType.ultraWarm
				&& this.natural == dimensionType.natural
				&& this.coordinateScale == dimensionType.coordinateScale
				&& this.createDragonFight == dimensionType.createDragonFight
				&& this.piglinSafe == dimensionType.piglinSafe
				&& this.bedWorks == dimensionType.bedWorks
				&& this.respawnAnchorWorks == dimensionType.respawnAnchorWorks
				&& this.hasRaids == dimensionType.hasRaids
				&& this.minY == dimensionType.minY
				&& this.height == dimensionType.height
				&& this.logicalHeight == dimensionType.logicalHeight
				&& Float.compare(dimensionType.ambientLight, this.ambientLight) == 0
				&& this.fixedTime.equals(dimensionType.fixedTime)
				&& this.infiniburn.equals(dimensionType.infiniburn)
				&& this.effectsLocation.equals(dimensionType.effectsLocation);
	}
}
