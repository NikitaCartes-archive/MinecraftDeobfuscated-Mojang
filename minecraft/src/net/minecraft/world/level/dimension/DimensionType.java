package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
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
	public static final Codec<ResourceKey<DimensionType>> RESOURCE_KEY_CODEC = ResourceLocation.CODEC
		.xmap(ResourceKey.elementKey(Registry.DIMENSION_TYPE_REGISTRY), ResourceKey::location);
	private static final Codec<DimensionType> DIRECT_CODEC = RecordCodecBuilder.create(
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
	private static final LinkedHashSet<ResourceKey<Level>> BUILTIN_ORDER = Sets.newLinkedHashSet(ImmutableList.of(Level.OVERWORLD, Level.NETHER, Level.END));
	private static final Map<ResourceKey<DimensionType>, DimensionType> BUILTIN = ImmutableMap.of(
		OVERWORLD_LOCATION, makeDefaultOverworld(), NETHER_LOCATION, makeDefaultNether(), END_LOCATION, makeDefaultEnd()
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
	public static final Codec<DimensionType> CODEC = Codec.either(BUILTIN_CODEC, DIRECT_CODEC)
		.flatXmap(
			either -> either.map(dimensionType -> DataResult.success(dimensionType, Lifecycle.stable()), DataResult::success),
			dimensionType -> dimensionType.builtinKey.isPresent()
					? DataResult.success(Either.left(dimensionType), Lifecycle.stable())
					: DataResult.success(Either.right(dimensionType))
		);
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

	public static DimensionType makeDefaultOverworld() {
		return new DimensionType(
			"", OptionalLong.empty(), true, false, false, true, false, false, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE, Optional.of(OVERWORLD_LOCATION), 0.0F
		);
	}

	private static DimensionType makeDefaultNether() {
		return new DimensionType(
			"_nether", OptionalLong.of(18000L), false, true, true, false, true, false, FuzzyOffsetBiomeZoomer.INSTANCE, Optional.of(NETHER_LOCATION), 0.1F
		);
	}

	private static DimensionType makeDefaultEnd() {
		return new DimensionType(
			"_end", OptionalLong.of(6000L), false, false, false, false, false, true, FuzzyOffsetBiomeZoomer.INSTANCE, Optional.of(END_LOCATION), 0.0F
		);
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

	@Environment(EnvType.CLIENT)
	public static RegistryAccess.RegistryHolder registerBuiltin(RegistryAccess.RegistryHolder registryHolder) {
		registryHolder.registerDimension(OVERWORLD_LOCATION, makeDefaultOverworld());
		registryHolder.registerDimension(NETHER_LOCATION, makeDefaultNether());
		registryHolder.registerDimension(END_LOCATION, makeDefaultEnd());
		return registryHolder;
	}

	private static ChunkGenerator defaultEndGenerator(long l) {
		return new NoiseBasedChunkGenerator(new TheEndBiomeSource(l), l, NoiseGeneratorSettings.Preset.END.settings());
	}

	private static ChunkGenerator defaultNetherGenerator(long l) {
		return new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.Preset.NETHER.biomeSource(l), l, NoiseGeneratorSettings.Preset.NETHER.settings());
	}

	public static LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> defaultDimensions(long l) {
		LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> linkedHashMap = Maps.newLinkedHashMap();
		linkedHashMap.put(Level.NETHER, Pair.of(makeDefaultNether(), defaultNetherGenerator(l)));
		linkedHashMap.put(Level.END, Pair.of(makeDefaultEnd(), defaultEndGenerator(l)));
		return linkedHashMap;
	}

	public static boolean stable(long l, LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> linkedHashMap) {
		List<Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>>> list = Lists.<Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>>>newArrayList(
			linkedHashMap.entrySet()
		);
		if (list.size() != 3) {
			return false;
		} else {
			Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> entry = (Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>>)list.get(0);
			Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> entry2 = (Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>>)list.get(1);
			Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> entry3 = (Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>>)list.get(2);
			if (entry.getKey() != Level.OVERWORLD || entry2.getKey() != Level.NETHER || entry3.getKey() != Level.END) {
				return false;
			} else if (!((DimensionType)((Pair)entry.getValue()).getFirst()).isOverworld()
				|| !((DimensionType)((Pair)entry2.getValue()).getFirst()).isNether()
				|| !((DimensionType)((Pair)entry3.getValue()).getFirst()).isEnd()) {
				return false;
			} else if (((Pair)entry2.getValue()).getSecond() instanceof NoiseBasedChunkGenerator
				&& ((Pair)entry3.getValue()).getSecond() instanceof NoiseBasedChunkGenerator) {
				NoiseBasedChunkGenerator noiseBasedChunkGenerator = (NoiseBasedChunkGenerator)((Pair)entry2.getValue()).getSecond();
				NoiseBasedChunkGenerator noiseBasedChunkGenerator2 = (NoiseBasedChunkGenerator)((Pair)entry3.getValue()).getSecond();
				if (!noiseBasedChunkGenerator.stable(l, NoiseGeneratorSettings.Preset.NETHER)) {
					return false;
				} else if (!noiseBasedChunkGenerator2.stable(l, NoiseGeneratorSettings.Preset.END)) {
					return false;
				} else if (!(noiseBasedChunkGenerator.getBiomeSource() instanceof MultiNoiseBiomeSource)) {
					return false;
				} else {
					MultiNoiseBiomeSource multiNoiseBiomeSource = (MultiNoiseBiomeSource)noiseBasedChunkGenerator.getBiomeSource();
					if (!multiNoiseBiomeSource.stable(l)) {
						return false;
					} else if (!(noiseBasedChunkGenerator2.getBiomeSource() instanceof TheEndBiomeSource)) {
						return false;
					} else {
						TheEndBiomeSource theEndBiomeSource = (TheEndBiomeSource)noiseBasedChunkGenerator2.getBiomeSource();
						return theEndBiomeSource.stable(l);
					}
				}
			} else {
				return false;
			}
		}
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

	public static LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> sortMap(Map<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> map) {
		LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> linkedHashMap = Maps.newLinkedHashMap();

		for (ResourceKey<Level> resourceKey : BUILTIN_ORDER) {
			Pair<DimensionType, ChunkGenerator> pair = (Pair<DimensionType, ChunkGenerator>)map.get(resourceKey);
			if (pair != null) {
				linkedHashMap.put(resourceKey, pair);
			}
		}

		for (Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> entry : map.entrySet()) {
			if (!BUILTIN_ORDER.contains(entry.getKey())) {
				linkedHashMap.put(entry.getKey(), entry.getValue());
			}
		}

		return linkedHashMap;
	}
}
