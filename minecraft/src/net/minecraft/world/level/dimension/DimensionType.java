package net.minecraft.world.level.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class DimensionType {
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
					Codec.intRange(0, 256).fieldOf("logical_height").forGetter(DimensionType::logicalHeight),
					ResourceLocation.CODEC.fieldOf("infiniburn").forGetter(dimensionType -> dimensionType.infiniburn),
					ResourceLocation.CODEC.fieldOf("effects").orElse(OVERWORLD_EFFECTS).forGetter(dimensionType -> dimensionType.effectsLocation),
					Codec.FLOAT.fieldOf("ambient_light").forGetter(dimensionType -> dimensionType.ambientLight)
				)
				.apply(instance, DimensionType::new)
	);
	public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
	public static final ResourceKey<DimensionType> OVERWORLD_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld"));
	public static final ResourceKey<DimensionType> NETHER_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_nether"));
	public static final ResourceKey<DimensionType> END_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_end"));
	protected static final DimensionType DEFAULT_OVERWORLD = new DimensionType(
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
		256,
		FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE,
		BlockTags.INFINIBURN_OVERWORLD.getName(),
		OVERWORLD_EFFECTS,
		0.0F
	);
	protected static final DimensionType DEFAULT_NETHER = new DimensionType(
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
		128,
		FuzzyOffsetBiomeZoomer.INSTANCE,
		BlockTags.INFINIBURN_NETHER.getName(),
		NETHER_EFFECTS,
		0.1F
	);
	protected static final DimensionType DEFAULT_END = new DimensionType(
		OptionalLong.of(6000L),
		false,
		false,
		false,
		false,
		1.0,
		true,
		false,
		false,
		false,
		true,
		256,
		FuzzyOffsetBiomeZoomer.INSTANCE,
		BlockTags.INFINIBURN_END.getName(),
		END_EFFECTS,
		0.0F
	);
	public static final ResourceKey<DimensionType> OVERWORLD_CAVES_LOCATION = ResourceKey.create(
		Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld_caves")
	);
	protected static final DimensionType DEFAULT_OVERWORLD_CAVES = new DimensionType(
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
		256,
		FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE,
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
	private final int logicalHeight;
	private final BiomeZoomer biomeZoomer;
	private final ResourceLocation infiniburn;
	private final ResourceLocation effectsLocation;
	private final float ambientLight;
	private final transient float[] brightnessRamp;

	protected DimensionType(
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
		ResourceLocation resourceLocation,
		ResourceLocation resourceLocation2,
		float f
	) {
		this(optionalLong, bl, bl2, bl3, bl4, d, false, bl5, bl6, bl7, bl8, i, FuzzyOffsetBiomeZoomer.INSTANCE, resourceLocation, resourceLocation2, f);
	}

	protected DimensionType(
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
		BiomeZoomer biomeZoomer,
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
		this.logicalHeight = i;
		this.biomeZoomer = biomeZoomer;
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

	public static RegistryAccess.RegistryHolder registerBuiltin(RegistryAccess.RegistryHolder registryHolder) {
		WritableRegistry<DimensionType> writableRegistry = registryHolder.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		writableRegistry.register(OVERWORLD_LOCATION, DEFAULT_OVERWORLD, Lifecycle.stable());
		writableRegistry.register(OVERWORLD_CAVES_LOCATION, DEFAULT_OVERWORLD_CAVES, Lifecycle.stable());
		writableRegistry.register(NETHER_LOCATION, DEFAULT_NETHER, Lifecycle.stable());
		writableRegistry.register(END_LOCATION, DEFAULT_END, Lifecycle.stable());
		return registryHolder;
	}

	private static ChunkGenerator defaultEndGenerator(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long l) {
		return new NoiseBasedChunkGenerator(new TheEndBiomeSource(registry, l), l, () -> registry2.getOrThrow(NoiseGeneratorSettings.END));
	}

	private static ChunkGenerator defaultNetherGenerator(Registry<Biome> registry, Registry<NoiseGeneratorSettings> registry2, long l) {
		return new NoiseBasedChunkGenerator(
			MultiNoiseBiomeSource.Preset.NETHER.biomeSource(registry, l), l, () -> registry2.getOrThrow(NoiseGeneratorSettings.NETHER)
		);
	}

	public static MappedRegistry<LevelStem> defaultDimensions(
		Registry<DimensionType> registry, Registry<Biome> registry2, Registry<NoiseGeneratorSettings> registry3, long l
	) {
		MappedRegistry<LevelStem> mappedRegistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
		mappedRegistry.register(
			LevelStem.NETHER, new LevelStem(() -> registry.getOrThrow(NETHER_LOCATION), defaultNetherGenerator(registry2, registry3, l)), Lifecycle.stable()
		);
		mappedRegistry.register(
			LevelStem.END, new LevelStem(() -> registry.getOrThrow(END_LOCATION), defaultEndGenerator(registry2, registry3, l)), Lifecycle.stable()
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

	public int logicalHeight() {
		return this.logicalHeight;
	}

	public boolean createDragonFight() {
		return this.createDragonFight;
	}

	public BiomeZoomer getBiomeZoomer() {
		return this.biomeZoomer;
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

	@Environment(EnvType.CLIENT)
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
				&& this.logicalHeight == dimensionType.logicalHeight
				&& Float.compare(dimensionType.ambientLight, this.ambientLight) == 0
				&& this.fixedTime.equals(dimensionType.fixedTime)
				&& this.biomeZoomer.equals(dimensionType.biomeZoomer)
				&& this.infiniburn.equals(dimensionType.infiniburn)
				&& this.effectsLocation.equals(dimensionType.effectsLocation);
	}
}
