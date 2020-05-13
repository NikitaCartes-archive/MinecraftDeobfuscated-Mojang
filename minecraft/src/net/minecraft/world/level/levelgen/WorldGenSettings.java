package net.minecraft.world.level.levelgen;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.DynamicLike;
import com.mojang.datafixers.types.JsonOps;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenSettings {
	private static final Dynamic<?> EMPTY_SETTINGS = new Dynamic<>(NbtOps.INSTANCE, new CompoundTag());
	private static final ChunkGenerator FLAT = new FlatLevelSource(FlatLevelGeneratorSettings.getDefault());
	private static final int DEMO_SEED = "North Carolina".hashCode();
	public static final WorldGenSettings DEMO_SETTINGS = new WorldGenSettings(
		(long)DEMO_SEED,
		true,
		true,
		WorldGenSettings.LevelType.NORMAL,
		EMPTY_SETTINGS,
		new OverworldLevelSource(new OverworldBiomeSource((long)DEMO_SEED, false, 4), (long)DEMO_SEED, new OverworldGeneratorSettings())
	);
	public static final WorldGenSettings TEST_SETTINGS = new WorldGenSettings(0L, false, false, WorldGenSettings.LevelType.FLAT, EMPTY_SETTINGS, FLAT);
	private static final Logger LOGGER = LogManager.getLogger();
	private final long seed;
	private final boolean generateFeatures;
	private final boolean generateBonusChest;
	private final WorldGenSettings.LevelType type;
	private final Dynamic<?> settings;
	private final ChunkGenerator generator;
	@Nullable
	private final String legacyCustomOptions;
	private final boolean isOldCustomizedWorld;
	private static final Map<WorldGenSettings.LevelType, WorldGenSettings.Preset> PRESETS = Maps.<WorldGenSettings.LevelType, WorldGenSettings.Preset>newHashMap();

	public WorldGenSettings(long l, boolean bl, boolean bl2, WorldGenSettings.LevelType levelType, Dynamic<?> dynamic, ChunkGenerator chunkGenerator) {
		this(l, bl, bl2, levelType, dynamic, chunkGenerator, null, false);
	}

	private WorldGenSettings(
		long l,
		boolean bl,
		boolean bl2,
		WorldGenSettings.LevelType levelType,
		Dynamic<?> dynamic,
		ChunkGenerator chunkGenerator,
		@Nullable String string,
		boolean bl3
	) {
		this.seed = l;
		this.generateFeatures = bl;
		this.generateBonusChest = bl2;
		this.legacyCustomOptions = string;
		this.isOldCustomizedWorld = bl3;
		this.type = levelType;
		this.settings = dynamic;
		this.generator = chunkGenerator;
	}

	public static WorldGenSettings readWorldGenSettings(CompoundTag compoundTag, DataFixer dataFixer, int i) {
		long l = compoundTag.getLong("RandomSeed");
		String string = null;
		WorldGenSettings.LevelType levelType;
		Dynamic<?> dynamic3;
		ChunkGenerator chunkGenerator;
		if (compoundTag.contains("generatorName", 8)) {
			String string2 = compoundTag.getString("generatorName");
			levelType = WorldGenSettings.LevelType.byName(string2);
			if (levelType == null) {
				levelType = WorldGenSettings.LevelType.NORMAL;
			} else if (levelType == WorldGenSettings.LevelType.CUSTOMIZED) {
				string = compoundTag.getString("generatorOptions");
			} else if (levelType == WorldGenSettings.LevelType.NORMAL) {
				int j = 0;
				if (compoundTag.contains("generatorVersion", 99)) {
					j = compoundTag.getInt("generatorVersion");
				}

				if (j == 0) {
					levelType = WorldGenSettings.LevelType.NORMAL_1_1;
				}
			}

			CompoundTag compoundTag2 = compoundTag.getCompound("generatorOptions");
			Dynamic<?> dynamic = new Dynamic<>(NbtOps.INSTANCE, compoundTag2);
			int k = Math.max(i, 2501);
			Dynamic<?> dynamic2 = dynamic.merge(dynamic.createString("levelType"), dynamic.createString(levelType.name));
			dynamic3 = dataFixer.update(References.CHUNK_GENERATOR_SETTINGS, dynamic2, k, SharedConstants.getCurrentVersion().getWorldVersion()).remove("levelType");
			chunkGenerator = make(levelType, dynamic3, l);
		} else {
			dynamic3 = EMPTY_SETTINGS;
			chunkGenerator = new OverworldLevelSource(new OverworldBiomeSource(l, false, 4), l, new OverworldGeneratorSettings());
			levelType = WorldGenSettings.LevelType.NORMAL;
		}

		if (compoundTag.contains("legacy_custom_options", 8)) {
			string = compoundTag.getString("legacy_custom_options");
		}

		boolean bl;
		if (compoundTag.contains("MapFeatures", 99)) {
			bl = compoundTag.getBoolean("MapFeatures");
		} else {
			bl = true;
		}

		boolean bl2 = compoundTag.getBoolean("BonusChest");
		boolean bl3 = levelType == WorldGenSettings.LevelType.CUSTOMIZED && i < 1466;
		return new WorldGenSettings(l, bl, bl2, levelType, dynamic3, chunkGenerator, string, bl3);
	}

	private static ChunkGenerator defaultEndGenerator(long l) {
		TheEndBiomeSource theEndBiomeSource = new TheEndBiomeSource(l);
		NoiseGeneratorSettings noiseGeneratorSettings = new NoiseGeneratorSettings(new ChunkGeneratorSettings());
		noiseGeneratorSettings.setDefaultBlock(Blocks.END_STONE.defaultBlockState());
		noiseGeneratorSettings.setDefaultFluid(Blocks.AIR.defaultBlockState());
		return new TheEndLevelSource(theEndBiomeSource, l, noiseGeneratorSettings);
	}

	private static ChunkGenerator defaultNetherGenerator(long l) {
		ImmutableList<Biome> immutableList = ImmutableList.of(
			Biomes.NETHER_WASTES, Biomes.SOUL_SAND_VALLEY, Biomes.CRIMSON_FOREST, Biomes.WARPED_FOREST, Biomes.BASALT_DELTAS
		);
		MultiNoiseBiomeSource multiNoiseBiomeSource = MultiNoiseBiomeSource.of(l, immutableList);
		NetherGeneratorSettings netherGeneratorSettings = new NetherGeneratorSettings(new ChunkGeneratorSettings());
		netherGeneratorSettings.setDefaultBlock(Blocks.NETHERRACK.defaultBlockState());
		netherGeneratorSettings.setDefaultFluid(Blocks.LAVA.defaultBlockState());
		return new NetherLevelSource(multiNoiseBiomeSource, l, netherGeneratorSettings);
	}

	@Environment(EnvType.CLIENT)
	public static WorldGenSettings makeDefault() {
		long l = new Random().nextLong();
		return new WorldGenSettings(
			l,
			true,
			false,
			WorldGenSettings.LevelType.NORMAL,
			EMPTY_SETTINGS,
			new OverworldLevelSource(new OverworldBiomeSource(l, false, 4), l, new OverworldGeneratorSettings())
		);
	}

	public CompoundTag serialize() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putLong("RandomSeed", this.seed());
		WorldGenSettings.LevelType levelType = this.type == WorldGenSettings.LevelType.CUSTOMIZED ? WorldGenSettings.LevelType.NORMAL : this.type;
		compoundTag.putString("generatorName", levelType.name);
		compoundTag.putInt("generatorVersion", this.type == WorldGenSettings.LevelType.NORMAL ? 1 : 0);
		CompoundTag compoundTag2 = (CompoundTag)this.settings.convert(NbtOps.INSTANCE).getValue();
		if (!compoundTag2.isEmpty()) {
			compoundTag.put("generatorOptions", compoundTag2);
		}

		if (this.legacyCustomOptions != null) {
			compoundTag.putString("legacy_custom_options", this.legacyCustomOptions);
		}

		compoundTag.putBoolean("MapFeatures", this.generateFeatures());
		compoundTag.putBoolean("BonusChest", this.generateBonusChest());
		return compoundTag;
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

	public Map<DimensionType, ChunkGenerator> generators() {
		return ImmutableMap.of(
			DimensionType.OVERWORLD, this.generator, DimensionType.NETHER, defaultNetherGenerator(this.seed), DimensionType.THE_END, defaultEndGenerator(this.seed)
		);
	}

	public ChunkGenerator overworld() {
		return this.generator;
	}

	public boolean isDebug() {
		return this.type == WorldGenSettings.LevelType.DEBUG_ALL_BLOCK_STATES;
	}

	public boolean isFlatWorld() {
		return this.type == WorldGenSettings.LevelType.FLAT;
	}

	@Environment(EnvType.CLIENT)
	public boolean isOldCustomizedWorld() {
		return this.isOldCustomizedWorld;
	}

	public WorldGenSettings withBonusChest() {
		return new WorldGenSettings(
			this.seed, this.generateFeatures, true, this.type, this.settings, this.generator, this.legacyCustomOptions, this.isOldCustomizedWorld
		);
	}

	@Environment(EnvType.CLIENT)
	public WorldGenSettings withFeaturesToggled() {
		return new WorldGenSettings(this.seed, !this.generateFeatures, this.generateBonusChest, this.type, this.settings, this.generator);
	}

	@Environment(EnvType.CLIENT)
	public WorldGenSettings withBonusChestToggled() {
		return new WorldGenSettings(this.seed, this.generateFeatures, !this.generateBonusChest, this.type, this.settings, this.generator);
	}

	public static WorldGenSettings read(Properties properties) {
		String string = MoreObjects.firstNonNull((String)properties.get("generator-settings"), "");
		properties.put("generator-settings", string);
		String string2 = MoreObjects.firstNonNull((String)properties.get("level-seed"), "");
		properties.put("level-seed", string2);
		String string3 = (String)properties.get("generate-structures");
		boolean bl = string3 == null || Boolean.parseBoolean(string3);
		properties.put("generate-structures", Objects.toString(bl));
		String string4 = (String)properties.get("level-type");
		WorldGenSettings.LevelType levelType;
		if (string4 != null) {
			levelType = MoreObjects.firstNonNull(WorldGenSettings.LevelType.byName(string4), WorldGenSettings.LevelType.NORMAL);
		} else {
			levelType = WorldGenSettings.LevelType.NORMAL;
		}

		properties.put("level-type", levelType.name);
		JsonObject jsonObject = !string.isEmpty() ? GsonHelper.parse(string) : new JsonObject();
		long l = new Random().nextLong();
		if (!string2.isEmpty()) {
			try {
				long m = Long.parseLong(string2);
				if (m != 0L) {
					l = m;
				}
			} catch (NumberFormatException var12) {
				l = (long)string2.hashCode();
			}
		}

		Dynamic<?> dynamic = new Dynamic<>(JsonOps.INSTANCE, jsonObject);
		return new WorldGenSettings(l, bl, false, levelType, dynamic, make(levelType, dynamic, l));
	}

	@Environment(EnvType.CLIENT)
	public WorldGenSettings withPreset(WorldGenSettings.Preset preset) {
		return this.withProvider(preset.type, EMPTY_SETTINGS, make(preset.type, EMPTY_SETTINGS, this.seed));
	}

	@Environment(EnvType.CLIENT)
	private WorldGenSettings withProvider(WorldGenSettings.LevelType levelType, Dynamic<?> dynamic, ChunkGenerator chunkGenerator) {
		return new WorldGenSettings(this.seed, this.generateFeatures, this.generateBonusChest, levelType, dynamic, chunkGenerator);
	}

	@Environment(EnvType.CLIENT)
	public WorldGenSettings fromFlatSettings(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
		return this.withProvider(
			WorldGenSettings.LevelType.FLAT, flatLevelGeneratorSettings.toObject(NbtOps.INSTANCE), new FlatLevelSource(flatLevelGeneratorSettings)
		);
	}

	@Environment(EnvType.CLIENT)
	public WorldGenSettings fromBuffetSettings(WorldGenSettings.BuffetGeneratorType buffetGeneratorType, Set<Biome> set) {
		Dynamic<?> dynamic = createBuffetSettings(buffetGeneratorType, set);
		return this.withProvider(WorldGenSettings.LevelType.BUFFET, dynamic, make(WorldGenSettings.LevelType.BUFFET, dynamic, this.seed));
	}

	@Environment(EnvType.CLIENT)
	public WorldGenSettings.Preset preset() {
		return this.type == WorldGenSettings.LevelType.CUSTOMIZED
			? WorldGenSettings.Preset.NORMAL
			: (WorldGenSettings.Preset)PRESETS.getOrDefault(this.type, WorldGenSettings.Preset.NORMAL);
	}

	@Environment(EnvType.CLIENT)
	public WorldGenSettings withSeed(boolean bl, OptionalLong optionalLong) {
		long l = optionalLong.orElse(this.seed);
		ChunkGenerator chunkGenerator = optionalLong.isPresent() ? this.generator.withSeed(optionalLong.getAsLong()) : this.generator;
		WorldGenSettings worldGenSettings;
		if (this.isDebug()) {
			worldGenSettings = new WorldGenSettings(l, false, false, this.type, this.settings, chunkGenerator);
		} else {
			worldGenSettings = new WorldGenSettings(l, this.generateFeatures(), this.generateBonusChest() && !bl, this.type, this.settings, chunkGenerator);
		}

		return worldGenSettings;
	}

	private static ChunkGenerator make(WorldGenSettings.LevelType levelType, Dynamic<?> dynamic, long l) {
		if (levelType == WorldGenSettings.LevelType.BUFFET) {
			BiomeSource biomeSource = createBuffetBiomeSource(dynamic.get("biome_source"), l);
			DynamicLike<?> dynamicLike = dynamic.get("chunk_generator");
			WorldGenSettings.BuffetGeneratorType buffetGeneratorType = DataFixUtils.orElse(
				dynamicLike.get("type").asString().flatMap(string -> Optional.ofNullable(WorldGenSettings.BuffetGeneratorType.byName(string))),
				WorldGenSettings.BuffetGeneratorType.SURFACE
			);
			DynamicLike<?> dynamicLike2 = dynamicLike.get("options");
			BlockState blockState = getRegistryValue(dynamicLike2.get("default_block"), Registry.BLOCK, Blocks.STONE).defaultBlockState();
			BlockState blockState2 = getRegistryValue(dynamicLike2.get("default_fluid"), Registry.BLOCK, Blocks.WATER).defaultBlockState();
			switch (buffetGeneratorType) {
				case CAVES:
					NetherGeneratorSettings netherGeneratorSettings = new NetherGeneratorSettings(new ChunkGeneratorSettings());
					netherGeneratorSettings.setDefaultBlock(blockState);
					netherGeneratorSettings.setDefaultFluid(blockState2);
					return new NetherLevelSource(biomeSource, l, netherGeneratorSettings);
				case FLOATING_ISLANDS:
					NoiseGeneratorSettings noiseGeneratorSettings = new NoiseGeneratorSettings(new ChunkGeneratorSettings());
					noiseGeneratorSettings.setDefaultBlock(blockState);
					noiseGeneratorSettings.setDefaultFluid(blockState2);
					return new TheEndLevelSource(biomeSource, l, noiseGeneratorSettings);
				case SURFACE:
				default:
					OverworldGeneratorSettings overworldGeneratorSettings = new OverworldGeneratorSettings();
					overworldGeneratorSettings.setDefaultBlock(blockState);
					overworldGeneratorSettings.setDefaultFluid(blockState2);
					return new OverworldLevelSource(biomeSource, l, overworldGeneratorSettings);
			}
		} else if (levelType == WorldGenSettings.LevelType.FLAT) {
			FlatLevelGeneratorSettings flatLevelGeneratorSettings = FlatLevelGeneratorSettings.fromObject(dynamic);
			return new FlatLevelSource(flatLevelGeneratorSettings);
		} else if (levelType == WorldGenSettings.LevelType.DEBUG_ALL_BLOCK_STATES) {
			return DebugLevelSource.INSTANCE;
		} else {
			boolean bl = levelType == WorldGenSettings.LevelType.NORMAL_1_1;
			int i = levelType == WorldGenSettings.LevelType.LARGE_BIOMES ? 6 : 4;
			boolean bl2 = levelType == WorldGenSettings.LevelType.AMPLIFIED;
			OverworldGeneratorSettings overworldGeneratorSettings2 = new OverworldGeneratorSettings(new ChunkGeneratorSettings(), bl2);
			return new OverworldLevelSource(new OverworldBiomeSource(l, bl, i), l, overworldGeneratorSettings2);
		}
	}

	private static <T> T getRegistryValue(DynamicLike<?> dynamicLike, Registry<T> registry, T object) {
		return (T)dynamicLike.asString().map(ResourceLocation::new).flatMap(registry::getOptional).orElse(object);
	}

	private static BiomeSource createBuffetBiomeSource(DynamicLike<?> dynamicLike, long l) {
		BiomeSourceType biomeSourceType = getRegistryValue(dynamicLike.get("type"), Registry.BIOME_SOURCE_TYPE, BiomeSourceType.FIXED);
		DynamicLike<?> dynamicLike2 = dynamicLike.get("options");
		Stream<Biome> stream = (Stream<Biome>)dynamicLike2.get("biomes")
			.asStreamOpt()
			.map(streamx -> streamx.map(dynamic -> getRegistryValue(dynamic, Registry.BIOME, Biomes.OCEAN)))
			.orElseGet(Stream::empty);
		if (BiomeSourceType.CHECKERBOARD == biomeSourceType) {
			int i = dynamicLike2.get("size").asInt(2);
			Biome[] biomes = (Biome[])stream.toArray(Biome[]::new);
			Biome[] biomes2 = biomes.length > 0 ? biomes : new Biome[]{Biomes.OCEAN};
			return new CheckerboardColumnBiomeSource(biomes2, i);
		} else if (BiomeSourceType.VANILLA_LAYERED == biomeSourceType) {
			return new OverworldBiomeSource(l, false, 4);
		} else {
			Biome biome = (Biome)stream.findFirst().orElse(Biomes.OCEAN);
			return new FixedBiomeSource(biome);
		}
	}

	@Environment(EnvType.CLIENT)
	private static Dynamic<?> createBuffetSettings(WorldGenSettings.BuffetGeneratorType buffetGeneratorType, Set<Biome> set) {
		CompoundTag compoundTag = new CompoundTag();
		CompoundTag compoundTag2 = new CompoundTag();
		compoundTag2.putString("type", Registry.BIOME_SOURCE_TYPE.getKey(BiomeSourceType.FIXED).toString());
		CompoundTag compoundTag3 = new CompoundTag();
		ListTag listTag = new ListTag();

		for (Biome biome : set) {
			listTag.add(StringTag.valueOf(Registry.BIOME.getKey(biome).toString()));
		}

		compoundTag3.put("biomes", listTag);
		compoundTag2.put("options", compoundTag3);
		CompoundTag compoundTag4 = new CompoundTag();
		CompoundTag compoundTag5 = new CompoundTag();
		compoundTag4.putString("type", buffetGeneratorType.getName());
		compoundTag5.putString("default_block", "minecraft:stone");
		compoundTag5.putString("default_fluid", "minecraft:water");
		compoundTag4.put("options", compoundTag5);
		compoundTag.put("biome_source", compoundTag2);
		compoundTag.put("chunk_generator", compoundTag4);
		return new Dynamic<>(NbtOps.INSTANCE, compoundTag);
	}

	@Environment(EnvType.CLIENT)
	public FlatLevelGeneratorSettings parseFlatSettings() {
		return this.type == WorldGenSettings.LevelType.FLAT ? FlatLevelGeneratorSettings.fromObject(this.settings) : FlatLevelGeneratorSettings.getDefault();
	}

	@Environment(EnvType.CLIENT)
	public Pair<WorldGenSettings.BuffetGeneratorType, Set<Biome>> parseBuffetSettings() {
		if (this.type != WorldGenSettings.LevelType.BUFFET) {
			return Pair.of(WorldGenSettings.BuffetGeneratorType.SURFACE, ImmutableSet.of());
		} else {
			WorldGenSettings.BuffetGeneratorType buffetGeneratorType = WorldGenSettings.BuffetGeneratorType.SURFACE;
			Set<Biome> set = Sets.<Biome>newHashSet();
			CompoundTag compoundTag = (CompoundTag)this.settings.convert(NbtOps.INSTANCE).getValue();
			if (compoundTag.contains("chunk_generator", 10) && compoundTag.getCompound("chunk_generator").contains("type", 8)) {
				String string = compoundTag.getCompound("chunk_generator").getString("type");
				buffetGeneratorType = WorldGenSettings.BuffetGeneratorType.byName(string);
			}

			if (compoundTag.contains("biome_source", 10) && compoundTag.getCompound("biome_source").contains("biomes", 9)) {
				ListTag listTag = compoundTag.getCompound("biome_source").getList("biomes", 8);

				for (int i = 0; i < listTag.size(); i++) {
					ResourceLocation resourceLocation = new ResourceLocation(listTag.getString(i));
					Biome biome = Registry.BIOME.get(resourceLocation);
					set.add(biome);
				}
			}

			return Pair.of(buffetGeneratorType, set);
		}
	}

	public static enum BuffetGeneratorType {
		SURFACE("minecraft:surface"),
		CAVES("minecraft:caves"),
		FLOATING_ISLANDS("minecraft:floating_islands");

		private static final Map<String, WorldGenSettings.BuffetGeneratorType> BY_NAME = (Map<String, WorldGenSettings.BuffetGeneratorType>)Arrays.stream(values())
			.collect(Collectors.toMap(WorldGenSettings.BuffetGeneratorType::getName, Function.identity()));
		private final String name;

		private BuffetGeneratorType(String string2) {
			this.name = string2;
		}

		@Environment(EnvType.CLIENT)
		public Component createGeneratorString() {
			return new TranslatableComponent("createWorld.customize.buffet.generatortype")
				.append(" ")
				.append(new TranslatableComponent(Util.makeDescriptionId("generator", new ResourceLocation(this.name))));
		}

		private String getName() {
			return this.name;
		}

		@Nullable
		public static WorldGenSettings.BuffetGeneratorType byName(String string) {
			return (WorldGenSettings.BuffetGeneratorType)BY_NAME.get(string);
		}
	}

	static class LevelType {
		private static final Set<WorldGenSettings.LevelType> TYPES = Sets.<WorldGenSettings.LevelType>newHashSet();
		public static final WorldGenSettings.LevelType NORMAL = new WorldGenSettings.LevelType("default");
		public static final WorldGenSettings.LevelType FLAT = new WorldGenSettings.LevelType("flat");
		public static final WorldGenSettings.LevelType LARGE_BIOMES = new WorldGenSettings.LevelType("largeBiomes");
		public static final WorldGenSettings.LevelType AMPLIFIED = new WorldGenSettings.LevelType("amplified");
		public static final WorldGenSettings.LevelType BUFFET = new WorldGenSettings.LevelType("buffet");
		public static final WorldGenSettings.LevelType DEBUG_ALL_BLOCK_STATES = new WorldGenSettings.LevelType("debug_all_block_states");
		public static final WorldGenSettings.LevelType CUSTOMIZED = new WorldGenSettings.LevelType("customized");
		public static final WorldGenSettings.LevelType NORMAL_1_1 = new WorldGenSettings.LevelType("default_1_1");
		private final String name;

		private LevelType(String string) {
			this.name = string;
			TYPES.add(this);
		}

		@Nullable
		public static WorldGenSettings.LevelType byName(String string) {
			for (WorldGenSettings.LevelType levelType : TYPES) {
				if (levelType.name.equalsIgnoreCase(string)) {
					return levelType;
				}
			}

			return null;
		}
	}

	@Environment(EnvType.CLIENT)
	public static final class Preset {
		public static final WorldGenSettings.Preset NORMAL = new WorldGenSettings.Preset(WorldGenSettings.LevelType.NORMAL);
		public static final WorldGenSettings.Preset FLAT = new WorldGenSettings.Preset(WorldGenSettings.LevelType.FLAT);
		public static final WorldGenSettings.Preset AMPLIFIED = new WorldGenSettings.Preset(WorldGenSettings.LevelType.AMPLIFIED);
		public static final WorldGenSettings.Preset BUFFET = new WorldGenSettings.Preset(WorldGenSettings.LevelType.BUFFET);
		public static final List<WorldGenSettings.Preset> PRESETS = Lists.<WorldGenSettings.Preset>newArrayList(
			NORMAL,
			FLAT,
			new WorldGenSettings.Preset(WorldGenSettings.LevelType.LARGE_BIOMES),
			AMPLIFIED,
			BUFFET,
			new WorldGenSettings.Preset(WorldGenSettings.LevelType.DEBUG_ALL_BLOCK_STATES)
		);
		private final WorldGenSettings.LevelType type;
		private final Component description;

		private Preset(WorldGenSettings.LevelType levelType) {
			this.type = levelType;
			WorldGenSettings.PRESETS.put(levelType, this);
			this.description = new TranslatableComponent("generator." + levelType.name);
		}

		public Component description() {
			return this.description;
		}
	}
}
