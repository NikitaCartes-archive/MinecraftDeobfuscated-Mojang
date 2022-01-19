/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
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
import net.minecraft.world.level.levelgen.feature.SwamplandHutFeature;
import net.minecraft.world.level.levelgen.feature.VillageFeature;
import net.minecraft.world.level.levelgen.feature.WoodlandMansionFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class StructureFeature<C extends FeatureConfiguration> {
    public static final BiMap<String, StructureFeature<?>> STRUCTURES_REGISTRY = HashBiMap.create();
    private static final Map<StructureFeature<?>, GenerationStep.Decoration> STEP = Maps.newHashMap();
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final StructureFeature<JigsawConfiguration> PILLAGER_OUTPOST = StructureFeature.register("Pillager_Outpost", new PillagerOutpostFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<MineshaftConfiguration> MINESHAFT = StructureFeature.register("Mineshaft", new MineshaftFeature(MineshaftConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> WOODLAND_MANSION = StructureFeature.register("Mansion", new WoodlandMansionFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> JUNGLE_TEMPLE = StructureFeature.register("Jungle_Pyramid", new JunglePyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> DESERT_PYRAMID = StructureFeature.register("Desert_Pyramid", new DesertPyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> IGLOO = StructureFeature.register("Igloo", new IglooFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<RuinedPortalConfiguration> RUINED_PORTAL = StructureFeature.register("Ruined_Portal", new RuinedPortalFeature(RuinedPortalConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<ShipwreckConfiguration> SHIPWRECK = StructureFeature.register("Shipwreck", new ShipwreckFeature(ShipwreckConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> SWAMP_HUT = StructureFeature.register("Swamp_Hut", new SwamplandHutFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> STRONGHOLD = StructureFeature.register("Stronghold", new StrongholdFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.STRONGHOLDS);
    public static final StructureFeature<NoneFeatureConfiguration> OCEAN_MONUMENT = StructureFeature.register("Monument", new OceanMonumentFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<OceanRuinConfiguration> OCEAN_RUIN = StructureFeature.register("Ocean_Ruin", new OceanRuinFeature(OceanRuinConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> NETHER_BRIDGE = StructureFeature.register("Fortress", new NetherFortressFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION);
    public static final StructureFeature<NoneFeatureConfiguration> END_CITY = StructureFeature.register("EndCity", new EndCityFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<ProbabilityFeatureConfiguration> BURIED_TREASURE = StructureFeature.register("Buried_Treasure", new BuriedTreasureFeature(ProbabilityFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
    public static final StructureFeature<JigsawConfiguration> VILLAGE = StructureFeature.register("Village", new VillageFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<RangeConfiguration> NETHER_FOSSIL = StructureFeature.register("Nether_Fossil", new NetherFossilFeature(RangeConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION);
    public static final StructureFeature<JigsawConfiguration> BASTION_REMNANT = StructureFeature.register("Bastion_Remnant", new BastionFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final List<StructureFeature<?>> NOISE_AFFECTING_FEATURES = ImmutableList.of(PILLAGER_OUTPOST, VILLAGE, NETHER_FOSSIL, STRONGHOLD);
    public static final int MAX_STRUCTURE_RANGE = 8;
    private final Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec;
    private final PieceGeneratorSupplier<C> pieceGenerator;
    private final PostPlacementProcessor postPlacementProcessor;

    private static <F extends StructureFeature<?>> F register(String string, F structureFeature, GenerationStep.Decoration decoration) {
        STRUCTURES_REGISTRY.put(string.toLowerCase(Locale.ROOT), structureFeature);
        STEP.put(structureFeature, decoration);
        return (F)Registry.register(Registry.STRUCTURE_FEATURE, string.toLowerCase(Locale.ROOT), structureFeature);
    }

    public StructureFeature(Codec<C> codec, PieceGeneratorSupplier<C> pieceGeneratorSupplier) {
        this(codec, pieceGeneratorSupplier, PostPlacementProcessor.NONE);
    }

    public StructureFeature(Codec<C> codec, PieceGeneratorSupplier<C> pieceGeneratorSupplier, PostPlacementProcessor postPlacementProcessor) {
        this.configuredStructureCodec = ((MapCodec)codec.fieldOf("config")).xmap(featureConfiguration -> new ConfiguredStructureFeature<FeatureConfiguration, StructureFeature>(this, (FeatureConfiguration)featureConfiguration), configuredStructureFeature -> configuredStructureFeature.config).codec();
        this.pieceGenerator = pieceGeneratorSupplier;
        this.postPlacementProcessor = postPlacementProcessor;
    }

    public GenerationStep.Decoration step() {
        return STEP.get(this);
    }

    public static void bootstrap() {
    }

    @Nullable
    public static StructureStart<?> loadStaticStart(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag, long l) {
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
            PiecesContainer piecesContainer = PiecesContainer.load(listTag, structurePieceSerializationContext);
            if (structureFeature == OCEAN_MONUMENT) {
                piecesContainer = OceanMonumentFeature.regeneratePiecesAfterLoad(chunkPos, l, piecesContainer);
            }
            return new StructureStart(structureFeature, chunkPos, i, piecesContainer);
        } catch (Exception exception) {
            LOGGER.error("Failed Start with id {}", (Object)string, (Object)exception);
            return null;
        }
    }

    public Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec() {
        return this.configuredStructureCodec;
    }

    public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configured(C featureConfiguration) {
        return new ConfiguredStructureFeature<C, StructureFeature>(this, featureConfiguration);
    }

    public BlockPos getLocatePos(ChunkPos chunkPos) {
        return new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
    }

    @Nullable
    public BlockPos getNearestGeneratedFeature(LevelReader levelReader, StructureFeatureManager structureFeatureManager, BlockPos blockPos, int i, boolean bl, long l, StructureFeatureConfiguration structureFeatureConfiguration) {
        int j = structureFeatureConfiguration.spacing();
        int k = SectionPos.blockToSectionCoord(blockPos.getX());
        int m = SectionPos.blockToSectionCoord(blockPos.getZ());
        block0: for (int n = 0; n <= i; ++n) {
            for (int o = -n; o <= n; ++o) {
                boolean bl2 = o == -n || o == n;
                for (int p = -n; p <= n; ++p) {
                    int r;
                    int q;
                    ChunkPos chunkPos;
                    StructureCheckResult structureCheckResult;
                    boolean bl3;
                    boolean bl4 = bl3 = p == -n || p == n;
                    if (!bl2 && !bl3 || (structureCheckResult = structureFeatureManager.checkStructurePresence(chunkPos = this.getPotentialFeatureChunk(structureFeatureConfiguration, l, q = k + j * o, r = m + j * p), this, bl)) == StructureCheckResult.START_NOT_PRESENT) continue;
                    if (!bl && structureCheckResult == StructureCheckResult.START_PRESENT) {
                        return this.getLocatePos(chunkPos);
                    }
                    ChunkAccess chunkAccess = levelReader.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
                    StructureStart<?> structureStart = structureFeatureManager.getStartForFeature(SectionPos.bottomOf(chunkAccess), this, chunkAccess);
                    if (structureStart != null && structureStart.isValid()) {
                        if (bl && structureStart.canBeReferenced()) {
                            structureFeatureManager.addReference(structureStart);
                            return this.getLocatePos(structureStart.getChunkPos());
                        }
                        if (!bl) {
                            return this.getLocatePos(structureStart.getChunkPos());
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

    public final ChunkPos getPotentialFeatureChunk(StructureFeatureConfiguration structureFeatureConfiguration, long l, int i, int j) {
        int q;
        int p;
        int k = structureFeatureConfiguration.spacing();
        int m = structureFeatureConfiguration.separation();
        int n = Math.floorDiv(i, k);
        int o = Math.floorDiv(j, k);
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
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

    public StructureStart<?> generate(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, StructureManager structureManager, long l, ChunkPos chunkPos, int i, StructureFeatureConfiguration structureFeatureConfiguration, C featureConfiguration, LevelHeightAccessor levelHeightAccessor, Predicate<Biome> predicate) {
        Optional<PieceGenerator<C>> optional;
        ChunkPos chunkPos2 = this.getPotentialFeatureChunk(structureFeatureConfiguration, l, chunkPos.x, chunkPos.z);
        if (chunkPos.x == chunkPos2.x && chunkPos.z == chunkPos2.z && (optional = this.pieceGenerator.createGenerator(new PieceGeneratorSupplier.Context<C>(chunkGenerator, biomeSource, l, chunkPos, featureConfiguration, levelHeightAccessor, predicate, structureManager, registryAccess))).isPresent()) {
            StructurePiecesBuilder structurePiecesBuilder = new StructurePiecesBuilder();
            WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
            worldgenRandom.setLargeFeatureSeed(l, chunkPos.x, chunkPos.z);
            optional.get().generatePieces(structurePiecesBuilder, new PieceGenerator.Context<C>(featureConfiguration, chunkGenerator, structureManager, chunkPos, levelHeightAccessor, worldgenRandom, l));
            StructureStart structureStart = new StructureStart(this, chunkPos, i, structurePiecesBuilder.build());
            if (structureStart.isValid()) {
                return structureStart;
            }
        }
        return StructureStart.INVALID_START;
    }

    public boolean canGenerate(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, StructureManager structureManager, long l, ChunkPos chunkPos, C featureConfiguration, LevelHeightAccessor levelHeightAccessor, Predicate<Biome> predicate) {
        return this.pieceGenerator.createGenerator(new PieceGeneratorSupplier.Context<C>(chunkGenerator, biomeSource, l, chunkPos, featureConfiguration, levelHeightAccessor, predicate, structureManager, registryAccess)).isPresent();
    }

    public PostPlacementProcessor getPostPlacementProcessor() {
        return this.postPlacementProcessor;
    }

    public String getFeatureName() {
        return (String)STRUCTURES_REGISTRY.inverse().get(this);
    }

    public BoundingBox adjustBoundingBox(BoundingBox boundingBox) {
        return boundingBox;
    }
}

