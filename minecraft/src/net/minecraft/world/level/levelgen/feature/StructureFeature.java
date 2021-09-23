package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
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
import net.minecraft.world.level.levelgen.WorldgenRandom;
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
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureFeature<C extends FeatureConfiguration> {
	public static final BiMap<String, StructureFeature<?>> STRUCTURES_REGISTRY = HashBiMap.create();
	private static final Map<StructureFeature<?>, GenerationStep.Decoration> STEP = Maps.<StructureFeature<?>, GenerationStep.Decoration>newHashMap();
	private static final Logger LOGGER = LogManager.getLogger();
	public static final StructureFeature<JigsawConfiguration> PILLAGER_OUTPOST = register(
		"Pillager_Outpost", new PillagerOutpostFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
	);
	public static final StructureFeature<MineshaftConfiguration> MINESHAFT = register(
		"Mineshaft", new MineshaftFeature(MineshaftConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES
	);
	public static final StructureFeature<NoneFeatureConfiguration> WOODLAND_MANSION = register(
		"Mansion", new WoodlandMansionFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
	);
	public static final StructureFeature<NoneFeatureConfiguration> JUNGLE_TEMPLE = register(
		"Jungle_Pyramid", new JunglePyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
	);
	public static final StructureFeature<NoneFeatureConfiguration> DESERT_PYRAMID = register(
		"Desert_Pyramid", new DesertPyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
	);
	public static final StructureFeature<NoneFeatureConfiguration> IGLOO = register(
		"Igloo", new IglooFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
	);
	public static final StructureFeature<RuinedPortalConfiguration> RUINED_PORTAL = register(
		"Ruined_Portal", new RuinedPortalFeature(RuinedPortalConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
	);
	public static final StructureFeature<ShipwreckConfiguration> SHIPWRECK = register(
		"Shipwreck", new ShipwreckFeature(ShipwreckConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
	);
	public static final StructureFeature<NoneFeatureConfiguration> SWAMP_HUT = register(
		"Swamp_Hut", new SwamplandHutFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
	);
	public static final StructureFeature<NoneFeatureConfiguration> STRONGHOLD = register(
		"Stronghold", new StrongholdFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.STRONGHOLDS
	);
	public static final StructureFeature<NoneFeatureConfiguration> OCEAN_MONUMENT = register(
		"Monument", new OceanMonumentFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
	);
	public static final StructureFeature<OceanRuinConfiguration> OCEAN_RUIN = register(
		"Ocean_Ruin", new OceanRuinFeature(OceanRuinConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
	);
	public static final StructureFeature<NoneFeatureConfiguration> NETHER_BRIDGE = register(
		"Fortress", new NetherFortressFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION
	);
	public static final StructureFeature<NoneFeatureConfiguration> END_CITY = register(
		"EndCity", new EndCityFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
	);
	public static final StructureFeature<ProbabilityFeatureConfiguration> BURIED_TREASURE = register(
		"Buried_Treasure", new BuriedTreasureFeature(ProbabilityFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES
	);
	public static final StructureFeature<JigsawConfiguration> VILLAGE = register(
		"Village", new VillageFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
	);
	public static final StructureFeature<RangeDecoratorConfiguration> NETHER_FOSSIL = register(
		"Nether_Fossil", new NetherFossilFeature(RangeDecoratorConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION
	);
	public static final StructureFeature<JigsawConfiguration> BASTION_REMNANT = register(
		"Bastion_Remnant", new BastionFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
	);
	public static final List<StructureFeature<?>> NOISE_AFFECTING_FEATURES = ImmutableList.of(PILLAGER_OUTPOST, VILLAGE, NETHER_FOSSIL, STRONGHOLD);
	public static final int MAX_STRUCTURE_RANGE = 8;
	private final Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec;
	private final PieceGenerator<C> pieceGenerator;
	private final PostPlacementProcessor postPlacementProcessor;

	private static <F extends StructureFeature<?>> F register(String string, F structureFeature, GenerationStep.Decoration decoration) {
		STRUCTURES_REGISTRY.put(string.toLowerCase(Locale.ROOT), structureFeature);
		STEP.put(structureFeature, decoration);
		return Registry.register(Registry.STRUCTURE_FEATURE, string.toLowerCase(Locale.ROOT), structureFeature);
	}

	public StructureFeature(Codec<C> codec, PieceGenerator<C> pieceGenerator) {
		this(codec, pieceGenerator, PostPlacementProcessor.NONE);
	}

	public StructureFeature(Codec<C> codec, PieceGenerator<C> pieceGenerator, PostPlacementProcessor postPlacementProcessor) {
		this.configuredStructureCodec = codec.fieldOf("config")
			.<ConfiguredStructureFeature<C, StructureFeature<C>>>xmap(
				featureConfiguration -> new ConfiguredStructureFeature<>(this, (C)featureConfiguration), configuredStructureFeature -> configuredStructureFeature.config
			)
			.codec();
		this.pieceGenerator = pieceGenerator;
		this.postPlacementProcessor = postPlacementProcessor;
	}

	public GenerationStep.Decoration step() {
		return (GenerationStep.Decoration)STEP.get(this);
	}

	public static void bootstrap() {
	}

	@Nullable
	public static StructureStart<?> loadStaticStart(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag, long l) {
		String string = compoundTag.getString("id");
		if ("INVALID".equals(string)) {
			return StructureStart.INVALID_START;
		} else {
			StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(new ResourceLocation(string.toLowerCase(Locale.ROOT)));
			if (structureFeature == null) {
				LOGGER.error("Unknown feature id: {}", string);
				return null;
			} else {
				ChunkPos chunkPos = new ChunkPos(compoundTag.getInt("ChunkX"), compoundTag.getInt("ChunkZ"));
				int i = compoundTag.getInt("references");
				ListTag listTag = compoundTag.getList("Children", 10);

				try {
					PiecesContainer piecesContainer = PiecesContainer.load(listTag, structurePieceSerializationContext);
					if (structureFeature == OCEAN_MONUMENT) {
						piecesContainer = OceanMonumentFeature.regeneratePiecesAfterLoad(chunkPos, l, piecesContainer);
					}

					return new StructureStart<>(structureFeature, chunkPos, i, piecesContainer);
				} catch (Exception var10) {
					LOGGER.error("Failed Start with id {}", string, var10);
					return null;
				}
			}
		}
	}

	public Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec() {
		return this.configuredStructureCodec;
	}

	public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configured(C featureConfiguration) {
		return new ConfiguredStructureFeature<>(this, featureConfiguration);
	}

	public BlockPos getLocatePos(ChunkPos chunkPos) {
		return new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
	}

	@Nullable
	public BlockPos getNearestGeneratedFeature(
		LevelReader levelReader,
		StructureFeatureManager structureFeatureManager,
		BlockPos blockPos,
		int i,
		boolean bl,
		long l,
		StructureFeatureConfiguration structureFeatureConfiguration
	) {
		int j = structureFeatureConfiguration.spacing();
		int k = SectionPos.blockToSectionCoord(blockPos.getX());
		int m = SectionPos.blockToSectionCoord(blockPos.getZ());
		int n = 0;

		for (WorldgenRandom worldgenRandom = new WorldgenRandom(); n <= i; n++) {
			for (int o = -n; o <= n; o++) {
				boolean bl2 = o == -n || o == n;

				for (int p = -n; p <= n; p++) {
					boolean bl3 = p == -n || p == n;
					if (bl2 || bl3) {
						int q = k + j * o;
						int r = m + j * p;
						ChunkPos chunkPos = this.getPotentialFeatureChunk(structureFeatureConfiguration, l, worldgenRandom, q, r);
						ChunkAccess chunkAccess = levelReader.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
						StructureStart<?> structureStart = structureFeatureManager.getStartForFeature(SectionPos.bottomOf(chunkAccess), this, chunkAccess);
						if (structureStart != null && structureStart.isValid()) {
							if (bl && structureStart.canBeReferenced()) {
								structureStart.addReference();
								return this.getLocatePos(structureStart.getChunkPos());
							}

							if (!bl) {
								return this.getLocatePos(structureStart.getChunkPos());
							}
						}

						if (n == 0) {
							break;
						}
					}
				}

				if (n == 0) {
					break;
				}
			}
		}

		return null;
	}

	protected boolean linearSeparation() {
		return true;
	}

	public final ChunkPos getPotentialFeatureChunk(
		StructureFeatureConfiguration structureFeatureConfiguration, long l, WorldgenRandom worldgenRandom, int i, int j
	) {
		int k = structureFeatureConfiguration.spacing();
		int m = structureFeatureConfiguration.separation();
		int n = Math.floorDiv(i, k);
		int o = Math.floorDiv(j, k);
		worldgenRandom.setLargeFeatureWithSalt(l, n, o, structureFeatureConfiguration.salt());
		int p;
		int q;
		if (this.linearSeparation()) {
			p = worldgenRandom.nextInt(k - m);
			q = worldgenRandom.nextInt(k - m);
		} else {
			p = (worldgenRandom.nextInt(k - m) + worldgenRandom.nextInt(k - m)) / 2;
			q = (worldgenRandom.nextInt(k - m) + worldgenRandom.nextInt(k - m)) / 2;
		}

		return new ChunkPos(n * k + p, o * k + q);
	}

	protected boolean isFeatureChunk(
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		long l,
		WorldgenRandom worldgenRandom,
		ChunkPos chunkPos,
		ChunkPos chunkPos2,
		C featureConfiguration,
		LevelHeightAccessor levelHeightAccessor
	) {
		return true;
	}

	public StructureStart<?> generate(
		RegistryAccess registryAccess,
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		StructureManager structureManager,
		long l,
		ChunkPos chunkPos,
		int i,
		WorldgenRandom worldgenRandom,
		StructureFeatureConfiguration structureFeatureConfiguration,
		C featureConfiguration,
		LevelHeightAccessor levelHeightAccessor,
		Predicate<Biome> predicate
	) {
		ChunkPos chunkPos2 = this.getPotentialFeatureChunk(structureFeatureConfiguration, l, worldgenRandom, chunkPos.x, chunkPos.z);
		if (chunkPos.x == chunkPos2.x
			&& chunkPos.z == chunkPos2.z
			&& this.isFeatureChunk(chunkGenerator, biomeSource, l, worldgenRandom, chunkPos, chunkPos2, featureConfiguration, levelHeightAccessor)) {
			StructurePiecesBuilder structurePiecesBuilder = new StructurePiecesBuilder();
			this.pieceGenerator
				.generatePieces(
					structurePiecesBuilder,
					featureConfiguration,
					new PieceGenerator.Context(
						registryAccess,
						chunkGenerator,
						structureManager,
						chunkPos,
						predicate,
						levelHeightAccessor,
						Util.make(new WorldgenRandom(), worldgenRandomx -> worldgenRandomx.setLargeFeatureSeed(l, chunkPos.x, chunkPos.z)),
						l
					)
				);
			StructureStart<C> structureStart = new StructureStart<>(this, chunkPos, i, structurePiecesBuilder.build());
			if (structureStart.isValid()) {
				return structureStart;
			}
		}

		return StructureStart.INVALID_START;
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
