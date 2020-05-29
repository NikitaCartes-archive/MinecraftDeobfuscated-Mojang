package net.minecraft.world.level.levelgen;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenSettings {
	public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.LONG.fieldOf("seed").stable().forGetter(WorldGenSettings::seed),
						Codec.BOOL.fieldOf("generate_features").withDefault(true).stable().forGetter(WorldGenSettings::generateFeatures),
						Codec.BOOL.fieldOf("bonus_chest").withDefault(false).stable().forGetter(WorldGenSettings::generateBonusChest),
						Codec.unboundedMap(
								ResourceLocation.CODEC.xmap(ResourceKey.elementKey(Registry.DIMENSION_REGISTRY), ResourceKey::location),
								Codec.mapPair(DimensionType.CODEC.fieldOf("type"), ChunkGenerator.CODEC.fieldOf("generator")).codec()
							)
							.xmap(DimensionType::sortMap, Function.identity())
							.fieldOf("dimensions")
							.forGetter(WorldGenSettings::dimensions),
						Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter(worldGenSettings -> worldGenSettings.legacyCustomOptions)
					)
					.apply(instance, instance.stable(WorldGenSettings::new))
		)
		.comapFlatMap(WorldGenSettings::guardExperimental, Function.identity());
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int DEMO_SEED = "North Carolina".hashCode();
	public static final WorldGenSettings DEMO_SETTINGS = new WorldGenSettings(
		(long)DEMO_SEED, true, true, withOverworld(DimensionType.defaultDimensions((long)DEMO_SEED), makeDefaultOverworld((long)DEMO_SEED))
	);
	public static final WorldGenSettings TEST_SETTINGS = new WorldGenSettings(
		0L, false, false, withOverworld(DimensionType.defaultDimensions(0L), new FlatLevelSource(FlatLevelGeneratorSettings.getDefault()))
	);
	private final long seed;
	private final boolean generateFeatures;
	private final boolean generateBonusChest;
	private final LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> dimensions;
	private final Optional<String> legacyCustomOptions;

	private DataResult<WorldGenSettings> guardExperimental() {
		return this.stable() ? DataResult.success(this, Lifecycle.stable()) : DataResult.success(this);
	}

	private boolean stable() {
		return DimensionType.stable(this.seed, this.dimensions);
	}

	public WorldGenSettings(long l, boolean bl, boolean bl2, LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> linkedHashMap) {
		this(l, bl, bl2, linkedHashMap, Optional.empty());
	}

	private WorldGenSettings(
		long l, boolean bl, boolean bl2, LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> linkedHashMap, Optional<String> optional
	) {
		this.seed = l;
		this.generateFeatures = bl;
		this.generateBonusChest = bl2;
		this.dimensions = linkedHashMap;
		this.legacyCustomOptions = optional;
	}

	public static WorldGenSettings makeDefault() {
		long l = new Random().nextLong();
		return new WorldGenSettings(l, true, false, withOverworld(DimensionType.defaultDimensions(l), makeDefaultOverworld(l)));
	}

	public static NoiseBasedChunkGenerator makeDefaultOverworld(long l) {
		return new NoiseBasedChunkGenerator(new OverworldBiomeSource(l, false, false), l, NoiseGeneratorSettings.Preset.OVERWORLD.settings());
	}

	public long seed() {
		return this.seed;
	}

	public boolean generateFeatures() {
		return this.generateFeatures;
	}

	public boolean generateBonusChest() {
		return this.generateBonusChest;
	}

	public static LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> withOverworld(
		LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> linkedHashMap, ChunkGenerator chunkGenerator
	) {
		LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> linkedHashMap2 = Maps.newLinkedHashMap();
		Pair<DimensionType, ChunkGenerator> pair = (Pair<DimensionType, ChunkGenerator>)linkedHashMap.get(DimensionType.OVERWORLD_LOCATION);
		DimensionType dimensionType = pair == null ? DimensionType.makeDefaultOverworld() : pair.getFirst();
		linkedHashMap2.put(Level.OVERWORLD, Pair.of(dimensionType, chunkGenerator));

		for (Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> entry : linkedHashMap.entrySet()) {
			if (entry.getKey() != Level.OVERWORLD) {
				linkedHashMap2.put(entry.getKey(), entry.getValue());
			}
		}

		return linkedHashMap2;
	}

	public LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> dimensions() {
		return this.dimensions;
	}

	public ChunkGenerator overworld() {
		Pair<DimensionType, ChunkGenerator> pair = (Pair<DimensionType, ChunkGenerator>)this.dimensions.get(DimensionType.OVERWORLD_LOCATION);
		return (ChunkGenerator)(pair == null ? makeDefaultOverworld(new Random().nextLong()) : pair.getSecond());
	}

	public boolean isDebug() {
		return this.overworld() instanceof DebugLevelSource;
	}

	public boolean isFlatWorld() {
		return this.overworld() instanceof FlatLevelSource;
	}

	@Environment(EnvType.CLIENT)
	public boolean isOldCustomizedWorld() {
		return this.legacyCustomOptions.isPresent();
	}

	public WorldGenSettings withBonusChest() {
		return new WorldGenSettings(this.seed, this.generateFeatures, true, this.dimensions, this.legacyCustomOptions);
	}

	@Environment(EnvType.CLIENT)
	public WorldGenSettings withFeaturesToggled() {
		return new WorldGenSettings(this.seed, !this.generateFeatures, this.generateBonusChest, this.dimensions);
	}

	@Environment(EnvType.CLIENT)
	public WorldGenSettings withBonusChestToggled() {
		return new WorldGenSettings(this.seed, this.generateFeatures, !this.generateBonusChest, this.dimensions);
	}

	public static WorldGenSettings create(Properties properties) {
		String string = MoreObjects.firstNonNull((String)properties.get("generator-settings"), "");
		properties.put("generator-settings", string);
		String string2 = MoreObjects.firstNonNull((String)properties.get("level-seed"), "");
		properties.put("level-seed", string2);
		String string3 = (String)properties.get("generate-structures");
		boolean bl = string3 == null || Boolean.parseBoolean(string3);
		properties.put("generate-structures", Objects.toString(bl));
		String string4 = (String)properties.get("level-type");
		String string5 = (String)Optional.ofNullable(string4).map(stringx -> stringx.toLowerCase(Locale.ROOT)).orElse("default");
		properties.put("level-type", string5);
		long l = new Random().nextLong();
		if (!string2.isEmpty()) {
			try {
				long m = Long.parseLong(string2);
				if (m != 0L) {
					l = m;
				}
			} catch (NumberFormatException var14) {
				l = (long)string2.hashCode();
			}
		}

		LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> linkedHashMap = DimensionType.defaultDimensions(l);
		switch (string5) {
			case "flat":
				JsonObject jsonObject = !string.isEmpty() ? GsonHelper.parse(string) : new JsonObject();
				Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, jsonObject);
				return new WorldGenSettings(
					l,
					bl,
					false,
					withOverworld(
						linkedHashMap,
						new FlatLevelSource(
							(FlatLevelGeneratorSettings)FlatLevelGeneratorSettings.CODEC
								.parse(dynamic)
								.resultOrPartial(LOGGER::error)
								.orElseGet(FlatLevelGeneratorSettings::getDefault)
						)
					)
				);
			case "debug_all_block_states":
				return new WorldGenSettings(l, bl, false, withOverworld(linkedHashMap, DebugLevelSource.INSTANCE));
			default:
				return new WorldGenSettings(l, bl, false, withOverworld(linkedHashMap, makeDefaultOverworld(l)));
		}
	}

	@Environment(EnvType.CLIENT)
	public WorldGenSettings withSeed(boolean bl, OptionalLong optionalLong) {
		long l = optionalLong.orElse(this.seed);
		LinkedHashMap<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> linkedHashMap;
		if (optionalLong.isPresent()) {
			linkedHashMap = Maps.newLinkedHashMap();
			long m = optionalLong.getAsLong();

			for (Entry<ResourceKey<Level>, Pair<DimensionType, ChunkGenerator>> entry : this.dimensions.entrySet()) {
				linkedHashMap.put(entry.getKey(), Pair.of(((Pair)entry.getValue()).getFirst(), ((ChunkGenerator)((Pair)entry.getValue()).getSecond()).withSeed(m)));
			}
		} else {
			linkedHashMap = this.dimensions;
		}

		WorldGenSettings worldGenSettings;
		if (this.isDebug()) {
			worldGenSettings = new WorldGenSettings(l, false, false, linkedHashMap);
		} else {
			worldGenSettings = new WorldGenSettings(l, this.generateFeatures(), this.generateBonusChest() && !bl, linkedHashMap);
		}

		return worldGenSettings;
	}
}
