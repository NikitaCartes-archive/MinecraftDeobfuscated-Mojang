package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class DimensionType {
	private static final Codec<ResourceKey<DimensionType>> RESOURCE_KEY_CODEC = ResourceLocation.CODEC
		.xmap(ResourceKey.elementKey(Registry.DIMENSION_TYPE_REGISTRY), ResourceKey::location);
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
					Codec.BOOL.fieldOf("shrunk").forGetter(DimensionType::shrunk),
					Codec.FLOAT.fieldOf("ambient_light").forGetter(dimensionType -> dimensionType.ambientLight)
				)
				.apply(instance, DimensionType::new)
	);
	public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
	public static final ResourceKey<DimensionType> OVERWORLD_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld"));
	public static final ResourceKey<DimensionType> NETHER_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_nether"));
	public static final ResourceKey<DimensionType> END_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_end"));
	private static final DimensionType DEFAULT_OVERWORLD = new DimensionType(
		"", OptionalLong.empty(), true, false, false, true, false, false, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE, Optional.of(OVERWORLD_LOCATION), 0.0F
	);
	private static final DimensionType DEFAULT_NETHER = new DimensionType(
		"_nether", OptionalLong.of(18000L), false, true, true, false, true, false, FuzzyOffsetBiomeZoomer.INSTANCE, Optional.of(NETHER_LOCATION), 0.1F
	);
	private static final DimensionType DEFAULT_END = new DimensionType(
		"_end", OptionalLong.of(6000L), false, false, false, false, false, true, FuzzyOffsetBiomeZoomer.INSTANCE, Optional.of(END_LOCATION), 0.0F
	);
	private static final Map<ResourceKey<DimensionType>, DimensionType> BUILTIN = ImmutableMap.of(
		OVERWORLD_LOCATION, defaultOverworld(), NETHER_LOCATION, DEFAULT_NETHER, END_LOCATION, DEFAULT_END
	);
	private static final Codec<DimensionType> BUILTIN_CODEC = RESOURCE_KEY_CODEC.<DimensionType>flatXmap(
			resourceKey -> (DataResult)Optional.ofNullable(BUILTIN.get(resourceKey))
					.map(DataResult::success)
					.orElseGet(() -> DataResult.error("Unknown builtin dimension: " + resourceKey)),
			dimensionType -> (DataResult)dimensionType.builtinKey
					.map(DataResult::success)
					.orElseGet(() -> DataResult.error("Unknown builtin dimension: " + dimensionType))
		)
		.stable();
	private static final Codec<DimensionType> BUILTIN_OR_DIRECT_CODEC = Codec.either(BUILTIN_CODEC, DIRECT_CODEC)
		.flatXmap(
			either -> either.map(dimensionType -> DataResult.success(dimensionType, Lifecycle.stable()), DataResult::success),
			dimensionType -> dimensionType.builtinKey.isPresent()
					? DataResult.success(Either.left(dimensionType), Lifecycle.stable())
					: DataResult.success(Either.right(dimensionType))
		);
	public static final Codec<Supplier<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, BUILTIN_OR_DIRECT_CODEC);
	private final String fileSuffix;
	private final OptionalLong fixedTime;
	private final boolean hasSkylight;
	private final boolean hasCeiling;
	private final boolean ultraWarm;
	private final boolean natural;
	private final boolean shrunk;
	private final boolean createDragonFight;
	private final BiomeZoomer biomeZoomer;
	private final Optional<ResourceKey<DimensionType>> builtinKey;
	private final float ambientLight;
	private final transient float[] brightnessRamp;

	public static DimensionType defaultOverworld() {
		return DEFAULT_OVERWORLD;
	}

	protected DimensionType(OptionalLong optionalLong, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, float f) {
		this("", optionalLong, bl, bl2, bl3, bl4, bl5, false, FuzzyOffsetBiomeZoomer.INSTANCE, Optional.empty(), f);
	}

	protected DimensionType(
		String string,
		OptionalLong optionalLong,
		boolean bl,
		boolean bl2,
		boolean bl3,
		boolean bl4,
		boolean bl5,
		boolean bl6,
		BiomeZoomer biomeZoomer,
		Optional<ResourceKey<DimensionType>> optional,
		float f
	) {
		this.fileSuffix = string;
		this.fixedTime = optionalLong;
		this.hasSkylight = bl;
		this.hasCeiling = bl2;
		this.ultraWarm = bl3;
		this.natural = bl4;
		this.shrunk = bl5;
		this.createDragonFight = bl6;
		this.biomeZoomer = biomeZoomer;
		this.builtinKey = optional;
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
		DataResult<Number> dataResult = dynamic.asNumber();
		if (dataResult.result().equals(Optional.of(-1))) {
			return DataResult.success(Level.NETHER);
		} else if (dataResult.result().equals(Optional.of(0))) {
			return DataResult.success(Level.OVERWORLD);
		} else {
			return dataResult.result().equals(Optional.of(1)) ? DataResult.success(Level.END) : Level.RESOURCE_KEY_CODEC.parse(dynamic);
		}
	}

	public static RegistryAccess.RegistryHolder registerBuiltin(RegistryAccess.RegistryHolder registryHolder) {
		registryHolder.registerDimension(OVERWORLD_LOCATION, defaultOverworld());
		registryHolder.registerDimension(NETHER_LOCATION, DEFAULT_NETHER);
		registryHolder.registerDimension(END_LOCATION, DEFAULT_END);
		return registryHolder;
	}

	private static ChunkGenerator defaultEndGenerator(long l) {
		return new NoiseBasedChunkGenerator(new TheEndBiomeSource(l), l, NoiseGeneratorSettings.Preset.END.settings());
	}

	private static ChunkGenerator defaultNetherGenerator(long l) {
		return new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.Preset.NETHER.biomeSource(l), l, NoiseGeneratorSettings.Preset.NETHER.settings());
	}

	public static MappedRegistry<LevelStem> defaultDimensions(long l) {
		MappedRegistry<LevelStem> mappedRegistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
		mappedRegistry.register(LevelStem.NETHER, new LevelStem(() -> DEFAULT_NETHER, defaultNetherGenerator(l)));
		mappedRegistry.register(LevelStem.END, new LevelStem(() -> DEFAULT_END, defaultEndGenerator(l)));
		return mappedRegistry;
	}

	public String getFileSuffix() {
		return this.fileSuffix;
	}

	public static File getStorageFolder(ResourceKey<Level> resourceKey, File file) {
		if (resourceKey == Level.OVERWORLD) {
			return file;
		} else if (resourceKey == Level.END) {
			return new File(file, "DIM1");
		} else {
			return resourceKey == Level.NETHER
				? new File(file, "DIM-1")
				: new File(file, "dimensions/" + resourceKey.location().getNamespace() + "/" + resourceKey.location().getPath());
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

	public boolean shrunk() {
		return this.shrunk;
	}

	public boolean createDragonFight() {
		return this.createDragonFight;
	}

	public BiomeZoomer getBiomeZoomer() {
		return this.biomeZoomer;
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

	public boolean isOverworld() {
		return this.builtinKey.equals(Optional.of(OVERWORLD_LOCATION));
	}

	public boolean isNether() {
		return this.builtinKey.equals(Optional.of(NETHER_LOCATION));
	}

	public boolean isEnd() {
		return this.builtinKey.equals(Optional.of(END_LOCATION));
	}
}
