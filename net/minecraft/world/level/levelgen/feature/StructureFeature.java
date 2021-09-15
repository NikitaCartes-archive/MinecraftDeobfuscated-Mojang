/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.BastionFeature;
import net.minecraft.world.level.levelgen.feature.BuriedTreasureFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.DesertPyramidFeature;
import net.minecraft.world.level.levelgen.feature.EndCityFeature;
import net.minecraft.world.level.levelgen.feature.IglooFeature;
import net.minecraft.world.level.levelgen.feature.JunglePyramidFeature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.NetherFortressFeature;
import net.minecraft.world.level.levelgen.feature.OceanMonumentFeature;
import net.minecraft.world.level.levelgen.feature.PillagerOutpostFeature;
import net.minecraft.world.level.levelgen.feature.RuinedPortalFeature;
import net.minecraft.world.level.levelgen.feature.ShipwreckFeature;
import net.minecraft.world.level.levelgen.feature.StrongholdFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.SwamplandHutFeature;
import net.minecraft.world.level.levelgen.feature.VillageFeature;
import net.minecraft.world.level.levelgen.feature.WoodlandMansionFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class StructureFeature<C extends FeatureConfiguration> {
    public static final BiMap<String, StructureFeature<?>> STRUCTURES_REGISTRY = HashBiMap.create();
    private static final Map<StructureFeature<?>, GenerationStep.Decoration> STEP = Maps.newHashMap();
    private static final Logger LOGGER = LogManager.getLogger();
    public static final StructureFeature<JigsawConfiguration> PILLAGER_OUTPOST = StructureFeature.register("Pillager_Outpost", new PillagerOutpostFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<MineshaftConfiguration> MINESHAFT = StructureFeature.register("Mineshaft", new MineshaftFeature(MineshaftConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> WOODLAND_MANSION = StructureFeature.register("Mansion", new WoodlandMansionFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> JUNGLE_TEMPLE = StructureFeature.register("Jungle_Pyramid", new JunglePyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> DESERT_PYRAMID = StructureFeature.register("Desert_Pyramid", new DesertPyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> IGLOO = StructureFeature.register("Igloo", new IglooFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<RuinedPortalConfiguration> RUINED_PORTAL = StructureFeature.register("Ruined_Portal", new RuinedPortalFeature(RuinedPortalConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<ShipwreckConfiguration> SHIPWRECK = StructureFeature.register("Shipwreck", new ShipwreckFeature(ShipwreckConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final SwamplandHutFeature SWAMP_HUT = StructureFeature.register("Swamp_Hut", new SwamplandHutFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> STRONGHOLD = StructureFeature.register("Stronghold", new StrongholdFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.STRONGHOLDS);
    public static final StructureFeature<NoneFeatureConfiguration> OCEAN_MONUMENT = StructureFeature.register("Monument", new OceanMonumentFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<OceanRuinConfiguration> OCEAN_RUIN = StructureFeature.register("Ocean_Ruin", new OceanRuinFeature(OceanRuinConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> NETHER_BRIDGE = StructureFeature.register("Fortress", new NetherFortressFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION);
    public static final StructureFeature<NoneFeatureConfiguration> END_CITY = StructureFeature.register("EndCity", new EndCityFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<ProbabilityFeatureConfiguration> BURIED_TREASURE = StructureFeature.register("Buried_Treasure", new BuriedTreasureFeature(ProbabilityFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
    public static final StructureFeature<JigsawConfiguration> VILLAGE = StructureFeature.register("Village", new VillageFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<RangeDecoratorConfiguration> NETHER_FOSSIL = StructureFeature.register("Nether_Fossil", new NetherFossilFeature(RangeDecoratorConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION);
    public static final StructureFeature<JigsawConfiguration> BASTION_REMNANT = StructureFeature.register("Bastion_Remnant", new BastionFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final List<StructureFeature<?>> NOISE_AFFECTING_FEATURES = ImmutableList.of(PILLAGER_OUTPOST, VILLAGE, NETHER_FOSSIL, STRONGHOLD);
    private static final ResourceLocation JIGSAW_RENAME = new ResourceLocation("jigsaw");
    private static final Map<ResourceLocation, ResourceLocation> RENAMES = ImmutableMap.builder().put(new ResourceLocation("nvi"), JIGSAW_RENAME).put(new ResourceLocation("pcp"), JIGSAW_RENAME).put(new ResourceLocation("bastionremnant"), JIGSAW_RENAME).put(new ResourceLocation("runtime"), JIGSAW_RENAME).build();
    public static final int MAX_STRUCTURE_RANGE = 8;
    private final Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec;

    private static <F extends StructureFeature<?>> F register(String string, F structureFeature, GenerationStep.Decoration decoration) {
        STRUCTURES_REGISTRY.put(string.toLowerCase(Locale.ROOT), structureFeature);
        STEP.put(structureFeature, decoration);
        return (F)Registry.register(Registry.STRUCTURE_FEATURE, string.toLowerCase(Locale.ROOT), structureFeature);
    }

    public StructureFeature(Codec<C> codec) {
        this.configuredStructureCodec = ((MapCodec)codec.fieldOf("config")).xmap(featureConfiguration -> new ConfiguredStructureFeature<FeatureConfiguration, StructureFeature>(this, (FeatureConfiguration)featureConfiguration), configuredStructureFeature -> configuredStructureFeature.config).codec();
    }

    public GenerationStep.Decoration step() {
        return STEP.get(this);
    }

    public static void bootstrap() {
    }

    @Nullable
    public static StructureStart<?> loadStaticStart(ServerLevel serverLevel, CompoundTag compoundTag, long l) {
        String string = compoundTag.getString("id");
        if ("INVALID".equals(string)) {
            return StructureStart.INVALID_START;
        }
        StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(new ResourceLocation(string.toLowerCase(Locale.ROOT)));
        if (structureFeature == null) {
            LOGGER.error("Unknown feature id: {}", (Object)string);
            return null;
        }
        ChunkPos chunkPos = new ChunkPos(compoundTag.getInt("ChunkX"), compoundTag.getInt("ChunkZ"));
        int i = compoundTag.getInt("references");
        ListTag listTag = compoundTag.getList("Children", 10);
        try {
            StructureStart<?> structureStart = structureFeature.createStart(chunkPos, i, l);
            for (int j = 0; j < listTag.size(); ++j) {
                CompoundTag compoundTag2 = listTag.getCompound(j);
                String string2 = compoundTag2.getString("id").toLowerCase(Locale.ROOT);
                ResourceLocation resourceLocation = new ResourceLocation(string2);
                ResourceLocation resourceLocation2 = RENAMES.getOrDefault(resourceLocation, resourceLocation);
                StructurePieceType structurePieceType = Registry.STRUCTURE_PIECE.get(resourceLocation2);
                if (structurePieceType == null) {
                    LOGGER.error("Unknown structure piece id: {}", (Object)resourceLocation2);
                    continue;
                }
                try {
                    StructurePiece structurePiece = structurePieceType.load(serverLevel, compoundTag2);
                    structureStart.addPiece(structurePiece);
                    continue;
                } catch (Exception exception) {
                    LOGGER.error("Exception loading structure piece with id {}", (Object)resourceLocation2, (Object)exception);
                }
            }
            return structureStart;
        } catch (Exception exception2) {
            LOGGER.error("Failed Start with id {}", (Object)string, (Object)exception2);
            return null;
        }
    }

    public Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec() {
        return this.configuredStructureCodec;
    }

    public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configured(C featureConfiguration) {
        return new ConfiguredStructureFeature<C, StructureFeature>(this, featureConfiguration);
    }

    @Nullable
    public BlockPos getNearestGeneratedFeature(LevelReader levelReader, StructureFeatureManager structureFeatureManager, BlockPos blockPos, int i, boolean bl, long l, StructureFeatureConfiguration structureFeatureConfiguration) {
        int j = structureFeatureConfiguration.spacing();
        int k = SectionPos.blockToSectionCoord(blockPos.getX());
        int m = SectionPos.blockToSectionCoord(blockPos.getZ());
        WorldgenRandom worldgenRandom = new WorldgenRandom();
        block0: for (int n = 0; n <= i; ++n) {
            for (int o = -n; o <= n; ++o) {
                boolean bl2 = o == -n || o == n;
                for (int p = -n; p <= n; ++p) {
                    boolean bl3;
                    boolean bl4 = bl3 = p == -n || p == n;
                    if (!bl2 && !bl3) continue;
                    int q = k + j * o;
                    int r = m + j * p;
                    ChunkPos chunkPos = this.getPotentialFeatureChunk(structureFeatureConfiguration, l, worldgenRandom, q, r);
                    ChunkAccess chunkAccess = levelReader.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
                    StructureStart<?> structureStart = structureFeatureManager.getStartForFeature(SectionPos.bottomOf(chunkAccess), this, chunkAccess);
                    if (structureStart != null && structureStart.isValid()) {
                        if (bl && structureStart.canBeReferenced()) {
                            structureStart.addReference();
                            return structureStart.getLocatePos();
                        }
                        if (!bl) {
                            return structureStart.getLocatePos();
                        }
                    }
                    if (n == 0) break;
                }
                if (n == 0) continue block0;
            }
        }
        return null;
    }

    protected boolean linearSeparation() {
        return true;
    }

    public final ChunkPos getPotentialFeatureChunk(StructureFeatureConfiguration structureFeatureConfiguration, long l, WorldgenRandom worldgenRandom, int i, int j) {
        int q;
        int p;
        int k = structureFeatureConfiguration.spacing();
        int m = structureFeatureConfiguration.separation();
        int n = Math.floorDiv(i, k);
        int o = Math.floorDiv(j, k);
        worldgenRandom.setLargeFeatureWithSalt(l, n, o, structureFeatureConfiguration.salt());
        if (this.linearSeparation()) {
            p = worldgenRandom.nextInt(k - m);
            q = worldgenRandom.nextInt(k - m);
        } else {
            p = (worldgenRandom.nextInt(k - m) + worldgenRandom.nextInt(k - m)) / 2;
            q = (worldgenRandom.nextInt(k - m) + worldgenRandom.nextInt(k - m)) / 2;
        }
        return new ChunkPos(n * k + p, o * k + q);
    }

    protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long l, WorldgenRandom worldgenRandom, ChunkPos chunkPos, ChunkPos chunkPos2, C featureConfiguration, LevelHeightAccessor levelHeightAccessor) {
        return true;
    }

    private StructureStart<C> createStart(ChunkPos chunkPos, int i, long l) {
        return this.getStartFactory().create(this, chunkPos, i, l);
    }

    public StructureStart<?> generate(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, StructureManager structureManager, long l, ChunkPos chunkPos, int i, WorldgenRandom worldgenRandom, StructureFeatureConfiguration structureFeatureConfiguration, C featureConfiguration, LevelHeightAccessor levelHeightAccessor, Predicate<Biome> predicate) {
        ChunkPos chunkPos2 = this.getPotentialFeatureChunk(structureFeatureConfiguration, l, worldgenRandom, chunkPos.x, chunkPos.z);
        if (chunkPos.x == chunkPos2.x && chunkPos.z == chunkPos2.z && this.isFeatureChunk(chunkGenerator, biomeSource, l, worldgenRandom, chunkPos, chunkPos2, featureConfiguration, levelHeightAccessor)) {
            StructureStart<C> structureStart = this.createStart(chunkPos, i, l);
            structureStart.generatePieces(registryAccess, chunkGenerator, structureManager, chunkPos, featureConfiguration, levelHeightAccessor, predicate);
            if (structureStart.isValid()) {
                return structureStart;
            }
        }
        return StructureStart.INVALID_START;
    }

    public abstract StructureStartFactory<C> getStartFactory();

    protected static int getLowestY(ChunkGenerator chunkGenerator, int i, int j, ChunkPos chunkPos, LevelHeightAccessor levelHeightAccessor) {
        int k = chunkPos.getMinBlockX();
        int l = chunkPos.getMinBlockZ();
        int[] is = StructureFeature.getCornerHeights(chunkGenerator, k, i, l, j, levelHeightAccessor);
        return Math.min(Math.min(is[0], is[1]), Math.min(is[2], is[3]));
    }

    protected static int[] getCornerHeights(ChunkGenerator chunkGenerator, int i, int j, int k, int l, LevelHeightAccessor levelHeightAccessor) {
        return new int[]{chunkGenerator.getFirstOccupiedHeight(i, k, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor), chunkGenerator.getFirstOccupiedHeight(i, k + l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor), chunkGenerator.getFirstOccupiedHeight(i + j, k, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor), chunkGenerator.getFirstOccupiedHeight(i + j, k + l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor)};
    }

    public String getFeatureName() {
        return (String)STRUCTURES_REGISTRY.inverse().get(this);
    }

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
        return MobSpawnSettings.EMPTY_MOB_LIST;
    }

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialAnimals() {
        return MobSpawnSettings.EMPTY_MOB_LIST;
    }

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialUndergroundWaterAnimals() {
        return MobSpawnSettings.EMPTY_MOB_LIST;
    }

    protected static boolean validBiomeOnTop(ChunkGenerator chunkGenerator, LevelHeightAccessor levelHeightAccessor, Predicate<Biome> predicate, Heightmap.Types types, int i, int j) {
        int k = chunkGenerator.getFirstOccupiedHeight(i, j, types, levelHeightAccessor);
        Biome biome = chunkGenerator.getNoiseBiome(QuartPos.fromBlock(i), QuartPos.fromBlock(k), QuartPos.fromBlock(j));
        return predicate.test(biome);
    }

    public static interface StructureStartFactory<C extends FeatureConfiguration> {
        public StructureStart<C> create(StructureFeature<C> var1, ChunkPos var2, int var3, long var4);
    }
}

