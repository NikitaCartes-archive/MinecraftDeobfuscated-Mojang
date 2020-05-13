/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import com.mojang.datafixers.OptionalDynamic;
import com.mojang.datafixers.types.JsonOps;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.HashSet;
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
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NetherGeneratorSettings;
import net.minecraft.world.level.levelgen.NetherLevelSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;
import net.minecraft.world.level.levelgen.TheEndLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class WorldGenSettings {
    private static final Dynamic<?> EMPTY_SETTINGS = new Dynamic<CompoundTag>(NbtOps.INSTANCE, new CompoundTag());
    private static final ChunkGenerator FLAT = new FlatLevelSource(FlatLevelGeneratorSettings.getDefault());
    private static final int DEMO_SEED = "North Carolina".hashCode();
    public static final WorldGenSettings DEMO_SETTINGS = new WorldGenSettings(DEMO_SEED, true, true, LevelType.NORMAL, EMPTY_SETTINGS, new OverworldLevelSource(new OverworldBiomeSource(DEMO_SEED, false, 4), DEMO_SEED, new OverworldGeneratorSettings()));
    public static final WorldGenSettings TEST_SETTINGS = new WorldGenSettings(0L, false, false, LevelType.FLAT, EMPTY_SETTINGS, FLAT);
    private static final Logger LOGGER = LogManager.getLogger();
    private final long seed;
    private final boolean generateFeatures;
    private final boolean generateBonusChest;
    private final LevelType type;
    private final Dynamic<?> settings;
    private final ChunkGenerator generator;
    @Nullable
    private final String legacyCustomOptions;
    private final boolean isOldCustomizedWorld;
    private static final Map<LevelType, Preset> PRESETS = Maps.newHashMap();

    public WorldGenSettings(long l, boolean bl, boolean bl2, LevelType levelType, Dynamic<?> dynamic, ChunkGenerator chunkGenerator) {
        this(l, bl, bl2, levelType, dynamic, chunkGenerator, null, false);
    }

    private WorldGenSettings(long l, boolean bl, boolean bl2, LevelType levelType, Dynamic<?> dynamic, ChunkGenerator chunkGenerator, @Nullable String string, boolean bl3) {
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
        ChunkGenerator chunkGenerator;
        Dynamic<Object> dynamic3;
        LevelType levelType;
        long l = compoundTag.getLong("RandomSeed");
        String string = null;
        if (compoundTag.contains("generatorName", 8)) {
            String string2 = compoundTag.getString("generatorName");
            levelType = LevelType.byName(string2);
            if (levelType == null) {
                levelType = LevelType.NORMAL;
            } else if (levelType == LevelType.CUSTOMIZED) {
                string = compoundTag.getString("generatorOptions");
            } else if (levelType == LevelType.NORMAL) {
                int j = 0;
                if (compoundTag.contains("generatorVersion", 99)) {
                    j = compoundTag.getInt("generatorVersion");
                }
                if (j == 0) {
                    levelType = LevelType.NORMAL_1_1;
                }
            }
            CompoundTag compoundTag2 = compoundTag.getCompound("generatorOptions");
            Dynamic<CompoundTag> dynamic = new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag2);
            int k = Math.max(i, 2501);
            Dynamic<CompoundTag> dynamic2 = dynamic.merge(dynamic.createString("levelType"), dynamic.createString(levelType.name));
            dynamic3 = dataFixer.update(References.CHUNK_GENERATOR_SETTINGS, dynamic2, k, SharedConstants.getCurrentVersion().getWorldVersion()).remove("levelType");
            chunkGenerator = WorldGenSettings.make(levelType, dynamic3, l);
        } else {
            dynamic3 = EMPTY_SETTINGS;
            chunkGenerator = new OverworldLevelSource(new OverworldBiomeSource(l, false, 4), l, new OverworldGeneratorSettings());
            levelType = LevelType.NORMAL;
        }
        if (compoundTag.contains("legacy_custom_options", 8)) {
            string = compoundTag.getString("legacy_custom_options");
        }
        boolean bl = compoundTag.contains("MapFeatures", 99) ? compoundTag.getBoolean("MapFeatures") : true;
        boolean bl2 = compoundTag.getBoolean("BonusChest");
        boolean bl3 = levelType == LevelType.CUSTOMIZED && i < 1466;
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
        ImmutableList<Biome> immutableList = ImmutableList.of(Biomes.NETHER_WASTES, Biomes.SOUL_SAND_VALLEY, Biomes.CRIMSON_FOREST, Biomes.WARPED_FOREST, Biomes.BASALT_DELTAS);
        MultiNoiseBiomeSource multiNoiseBiomeSource = MultiNoiseBiomeSource.of(l, immutableList);
        NetherGeneratorSettings netherGeneratorSettings = new NetherGeneratorSettings(new ChunkGeneratorSettings());
        netherGeneratorSettings.setDefaultBlock(Blocks.NETHERRACK.defaultBlockState());
        netherGeneratorSettings.setDefaultFluid(Blocks.LAVA.defaultBlockState());
        return new NetherLevelSource(multiNoiseBiomeSource, l, netherGeneratorSettings);
    }

    @Environment(value=EnvType.CLIENT)
    public static WorldGenSettings makeDefault() {
        long l = new Random().nextLong();
        return new WorldGenSettings(l, true, false, LevelType.NORMAL, EMPTY_SETTINGS, new OverworldLevelSource(new OverworldBiomeSource(l, false, 4), l, new OverworldGeneratorSettings()));
    }

    public CompoundTag serialize() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putLong("RandomSeed", this.seed());
        LevelType levelType = this.type == LevelType.CUSTOMIZED ? LevelType.NORMAL : this.type;
        compoundTag.putString("generatorName", levelType.name);
        compoundTag.putInt("generatorVersion", this.type == LevelType.NORMAL ? 1 : 0);
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
        return ImmutableMap.of(DimensionType.OVERWORLD, this.generator, DimensionType.NETHER, WorldGenSettings.defaultNetherGenerator(this.seed), DimensionType.THE_END, WorldGenSettings.defaultEndGenerator(this.seed));
    }

    public ChunkGenerator overworld() {
        return this.generator;
    }

    public boolean isDebug() {
        return this.type == LevelType.DEBUG_ALL_BLOCK_STATES;
    }

    public boolean isFlatWorld() {
        return this.type == LevelType.FLAT;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isOldCustomizedWorld() {
        return this.isOldCustomizedWorld;
    }

    public WorldGenSettings withBonusChest() {
        return new WorldGenSettings(this.seed, this.generateFeatures, true, this.type, this.settings, this.generator, this.legacyCustomOptions, this.isOldCustomizedWorld);
    }

    @Environment(value=EnvType.CLIENT)
    public WorldGenSettings withFeaturesToggled() {
        return new WorldGenSettings(this.seed, !this.generateFeatures, this.generateBonusChest, this.type, this.settings, this.generator);
    }

    @Environment(value=EnvType.CLIENT)
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
        LevelType levelType = string4 != null ? MoreObjects.firstNonNull(LevelType.byName(string4), LevelType.NORMAL) : LevelType.NORMAL;
        properties.put("level-type", levelType.name);
        JsonObject jsonObject = !string.isEmpty() ? GsonHelper.parse(string) : new JsonObject();
        long l = new Random().nextLong();
        if (!string2.isEmpty()) {
            try {
                long m = Long.parseLong(string2);
                if (m != 0L) {
                    l = m;
                }
            } catch (NumberFormatException numberFormatException) {
                l = string2.hashCode();
            }
        }
        Dynamic<JsonObject> dynamic = new Dynamic<JsonObject>(JsonOps.INSTANCE, jsonObject);
        return new WorldGenSettings(l, bl, false, levelType, dynamic, WorldGenSettings.make(levelType, dynamic, l));
    }

    @Environment(value=EnvType.CLIENT)
    public WorldGenSettings withPreset(Preset preset) {
        return this.withProvider(preset.type, EMPTY_SETTINGS, WorldGenSettings.make(preset.type, EMPTY_SETTINGS, this.seed));
    }

    @Environment(value=EnvType.CLIENT)
    private WorldGenSettings withProvider(LevelType levelType, Dynamic<?> dynamic, ChunkGenerator chunkGenerator) {
        return new WorldGenSettings(this.seed, this.generateFeatures, this.generateBonusChest, levelType, dynamic, chunkGenerator);
    }

    @Environment(value=EnvType.CLIENT)
    public WorldGenSettings fromFlatSettings(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        return this.withProvider(LevelType.FLAT, flatLevelGeneratorSettings.toObject(NbtOps.INSTANCE), new FlatLevelSource(flatLevelGeneratorSettings));
    }

    @Environment(value=EnvType.CLIENT)
    public WorldGenSettings fromBuffetSettings(BuffetGeneratorType buffetGeneratorType, Set<Biome> set) {
        Dynamic<?> dynamic = WorldGenSettings.createBuffetSettings(buffetGeneratorType, set);
        return this.withProvider(LevelType.BUFFET, dynamic, WorldGenSettings.make(LevelType.BUFFET, dynamic, this.seed));
    }

    @Environment(value=EnvType.CLIENT)
    public Preset preset() {
        if (this.type == LevelType.CUSTOMIZED) {
            return Preset.NORMAL;
        }
        return PRESETS.getOrDefault(this.type, Preset.NORMAL);
    }

    @Environment(value=EnvType.CLIENT)
    public WorldGenSettings withSeed(boolean bl, OptionalLong optionalLong) {
        ChunkGenerator chunkGenerator;
        long l = optionalLong.orElse(this.seed);
        ChunkGenerator chunkGenerator2 = chunkGenerator = optionalLong.isPresent() ? this.generator.withSeed(optionalLong.getAsLong()) : this.generator;
        WorldGenSettings worldGenSettings = this.isDebug() ? new WorldGenSettings(l, false, false, this.type, this.settings, chunkGenerator) : new WorldGenSettings(l, this.generateFeatures(), this.generateBonusChest() && !bl, this.type, this.settings, chunkGenerator);
        return worldGenSettings;
    }

    private static ChunkGenerator make(LevelType levelType, Dynamic<?> dynamic, long l) {
        if (levelType == LevelType.BUFFET) {
            BiomeSource biomeSource = WorldGenSettings.createBuffetBiomeSource(dynamic.get("biome_source"), l);
            OptionalDynamic<?> dynamicLike = dynamic.get("chunk_generator");
            BuffetGeneratorType buffetGeneratorType = DataFixUtils.orElse(((DynamicLike)dynamicLike).get("type").asString().flatMap(string -> Optional.ofNullable(BuffetGeneratorType.byName(string))), BuffetGeneratorType.SURFACE);
            OptionalDynamic dynamicLike2 = ((DynamicLike)dynamicLike).get("options");
            BlockState blockState = WorldGenSettings.getRegistryValue(((DynamicLike)dynamicLike2).get("default_block"), Registry.BLOCK, Blocks.STONE).defaultBlockState();
            BlockState blockState2 = WorldGenSettings.getRegistryValue(((DynamicLike)dynamicLike2).get("default_fluid"), Registry.BLOCK, Blocks.WATER).defaultBlockState();
            switch (buffetGeneratorType) {
                case CAVES: {
                    NetherGeneratorSettings netherGeneratorSettings = new NetherGeneratorSettings(new ChunkGeneratorSettings());
                    netherGeneratorSettings.setDefaultBlock(blockState);
                    netherGeneratorSettings.setDefaultFluid(blockState2);
                    return new NetherLevelSource(biomeSource, l, netherGeneratorSettings);
                }
                case FLOATING_ISLANDS: {
                    NoiseGeneratorSettings noiseGeneratorSettings = new NoiseGeneratorSettings(new ChunkGeneratorSettings());
                    noiseGeneratorSettings.setDefaultBlock(blockState);
                    noiseGeneratorSettings.setDefaultFluid(blockState2);
                    return new TheEndLevelSource(biomeSource, l, noiseGeneratorSettings);
                }
            }
            OverworldGeneratorSettings overworldGeneratorSettings = new OverworldGeneratorSettings();
            overworldGeneratorSettings.setDefaultBlock(blockState);
            overworldGeneratorSettings.setDefaultFluid(blockState2);
            return new OverworldLevelSource(biomeSource, l, overworldGeneratorSettings);
        }
        if (levelType == LevelType.FLAT) {
            FlatLevelGeneratorSettings flatLevelGeneratorSettings = FlatLevelGeneratorSettings.fromObject(dynamic);
            return new FlatLevelSource(flatLevelGeneratorSettings);
        }
        if (levelType == LevelType.DEBUG_ALL_BLOCK_STATES) {
            return DebugLevelSource.INSTANCE;
        }
        boolean bl = levelType == LevelType.NORMAL_1_1;
        int i = levelType == LevelType.LARGE_BIOMES ? 6 : 4;
        boolean bl2 = levelType == LevelType.AMPLIFIED;
        OverworldGeneratorSettings overworldGeneratorSettings2 = new OverworldGeneratorSettings(new ChunkGeneratorSettings(), bl2);
        return new OverworldLevelSource(new OverworldBiomeSource(l, bl, i), l, overworldGeneratorSettings2);
    }

    private static <T> T getRegistryValue(DynamicLike<?> dynamicLike, Registry<T> registry, T object) {
        return (T)dynamicLike.asString().map(ResourceLocation::new).flatMap(registry::getOptional).orElse(object);
    }

    private static BiomeSource createBuffetBiomeSource(DynamicLike<?> dynamicLike, long l) {
        BiomeSourceType biomeSourceType = WorldGenSettings.getRegistryValue(dynamicLike.get("type"), Registry.BIOME_SOURCE_TYPE, BiomeSourceType.FIXED);
        OptionalDynamic<?> dynamicLike2 = dynamicLike.get("options");
        Stream stream2 = ((DynamicLike)dynamicLike2).get("biomes").asStreamOpt().map(stream -> stream.map(dynamic -> WorldGenSettings.getRegistryValue(dynamic, Registry.BIOME, Biomes.OCEAN))).orElseGet(Stream::empty);
        if (BiomeSourceType.CHECKERBOARD == biomeSourceType) {
            Biome[] biomeArray;
            int i = ((DynamicLike)dynamicLike2).get("size").asInt(2);
            Biome[] biomes = (Biome[])stream2.toArray(Biome[]::new);
            if (biomes.length > 0) {
                biomeArray = biomes;
            } else {
                Biome[] biomeArray2 = new Biome[1];
                biomeArray = biomeArray2;
                biomeArray2[0] = Biomes.OCEAN;
            }
            Biome[] biomes2 = biomeArray;
            return new CheckerboardColumnBiomeSource(biomes2, i);
        }
        if (BiomeSourceType.VANILLA_LAYERED == biomeSourceType) {
            return new OverworldBiomeSource(l, false, 4);
        }
        Biome biome = stream2.findFirst().orElse(Biomes.OCEAN);
        return new FixedBiomeSource(biome);
    }

    @Environment(value=EnvType.CLIENT)
    private static Dynamic<?> createBuffetSettings(BuffetGeneratorType buffetGeneratorType, Set<Biome> set) {
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
        return new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag);
    }

    @Environment(value=EnvType.CLIENT)
    public FlatLevelGeneratorSettings parseFlatSettings() {
        return this.type == LevelType.FLAT ? FlatLevelGeneratorSettings.fromObject(this.settings) : FlatLevelGeneratorSettings.getDefault();
    }

    @Environment(value=EnvType.CLIENT)
    public Pair<BuffetGeneratorType, Set<Biome>> parseBuffetSettings() {
        if (this.type != LevelType.BUFFET) {
            return Pair.of(BuffetGeneratorType.SURFACE, ImmutableSet.of());
        }
        BuffetGeneratorType buffetGeneratorType = BuffetGeneratorType.SURFACE;
        HashSet<Biome> set = Sets.newHashSet();
        CompoundTag compoundTag = (CompoundTag)this.settings.convert(NbtOps.INSTANCE).getValue();
        if (compoundTag.contains("chunk_generator", 10) && compoundTag.getCompound("chunk_generator").contains("type", 8)) {
            String string = compoundTag.getCompound("chunk_generator").getString("type");
            buffetGeneratorType = BuffetGeneratorType.byName(string);
        }
        if (compoundTag.contains("biome_source", 10) && compoundTag.getCompound("biome_source").contains("biomes", 9)) {
            ListTag listTag = compoundTag.getCompound("biome_source").getList("biomes", 8);
            for (int i = 0; i < listTag.size(); ++i) {
                ResourceLocation resourceLocation = new ResourceLocation(listTag.getString(i));
                Biome biome = Registry.BIOME.get(resourceLocation);
                set.add(biome);
            }
        }
        return Pair.of(buffetGeneratorType, set);
    }

    public static enum BuffetGeneratorType {
        SURFACE("minecraft:surface"),
        CAVES("minecraft:caves"),
        FLOATING_ISLANDS("minecraft:floating_islands");

        private static final Map<String, BuffetGeneratorType> BY_NAME;
        private final String name;

        private BuffetGeneratorType(String string2) {
            this.name = string2;
        }

        @Environment(value=EnvType.CLIENT)
        public Component createGeneratorString() {
            return new TranslatableComponent("createWorld.customize.buffet.generatortype").append(" ").append(new TranslatableComponent(Util.makeDescriptionId("generator", new ResourceLocation(this.name))));
        }

        private String getName() {
            return this.name;
        }

        @Nullable
        public static BuffetGeneratorType byName(String string) {
            return BY_NAME.get(string);
        }

        static {
            BY_NAME = Arrays.stream(BuffetGeneratorType.values()).collect(Collectors.toMap(BuffetGeneratorType::getName, Function.identity()));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class Preset {
        public static final Preset NORMAL = new Preset(LevelType.NORMAL);
        public static final Preset FLAT = new Preset(LevelType.FLAT);
        public static final Preset AMPLIFIED = new Preset(LevelType.AMPLIFIED);
        public static final Preset BUFFET = new Preset(LevelType.BUFFET);
        public static final List<Preset> PRESETS = Lists.newArrayList(NORMAL, FLAT, new Preset(LevelType.LARGE_BIOMES), AMPLIFIED, BUFFET, new Preset(LevelType.DEBUG_ALL_BLOCK_STATES));
        private final LevelType type;
        private final Component description;

        private Preset(LevelType levelType) {
            this.type = levelType;
            PRESETS.put(levelType, this);
            this.description = new TranslatableComponent("generator." + levelType.name);
        }

        public Component description() {
            return this.description;
        }
    }

    static class LevelType {
        private static final Set<LevelType> TYPES = Sets.newHashSet();
        public static final LevelType NORMAL = new LevelType("default");
        public static final LevelType FLAT = new LevelType("flat");
        public static final LevelType LARGE_BIOMES = new LevelType("largeBiomes");
        public static final LevelType AMPLIFIED = new LevelType("amplified");
        public static final LevelType BUFFET = new LevelType("buffet");
        public static final LevelType DEBUG_ALL_BLOCK_STATES = new LevelType("debug_all_block_states");
        public static final LevelType CUSTOMIZED = new LevelType("customized");
        public static final LevelType NORMAL_1_1 = new LevelType("default_1_1");
        private final String name;

        private LevelType(String string) {
            this.name = string;
            TYPES.add(this);
        }

        @Nullable
        public static LevelType byName(String string) {
            for (LevelType levelType : TYPES) {
                if (!levelType.name.equalsIgnoreCase(string)) continue;
                return levelType;
            }
            return null;
        }
    }
}

