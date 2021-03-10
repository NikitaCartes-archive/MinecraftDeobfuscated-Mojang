package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StructureFeature<C extends FeatureConfiguration> {
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
	public static final SwamplandHutFeature SWAMP_HUT = register(
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
	public static final StructureFeature<NoneFeatureConfiguration> NETHER_FOSSIL = register(
		"Nether_Fossil", new NetherFossilFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION
	);
	public static final StructureFeature<JigsawConfiguration> BASTION_REMNANT = register(
		"Bastion_Remnant", new BastionFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES
	);
	public static final List<StructureFeature<?>> NOISE_AFFECTING_FEATURES = ImmutableList.of(PILLAGER_OUTPOST, VILLAGE, NETHER_FOSSIL, STRONGHOLD);
	private static final ResourceLocation JIGSAW_RENAME = new ResourceLocation("jigsaw");
	private static final Map<ResourceLocation, ResourceLocation> RENAMES = ImmutableMap.<ResourceLocation, ResourceLocation>builder()
		.put(new ResourceLocation("nvi"), JIGSAW_RENAME)
		.put(new ResourceLocation("pcp"), JIGSAW_RENAME)
		.put(new ResourceLocation("bastionremnant"), JIGSAW_RENAME)
		.put(new ResourceLocation("runtime"), JIGSAW_RENAME)
		.build();
	private final Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec;

	private static <F extends StructureFeature<?>> F register(String string, F structureFeature, GenerationStep.Decoration decoration) {
		STRUCTURES_REGISTRY.put(string.toLowerCase(Locale.ROOT), structureFeature);
		STEP.put(structureFeature, decoration);
		return Registry.register(Registry.STRUCTURE_FEATURE, string.toLowerCase(Locale.ROOT), structureFeature);
	}

	public StructureFeature(Codec<C> codec) {
		this.configuredStructureCodec = codec.fieldOf("config")
			.<ConfiguredStructureFeature<C, StructureFeature<C>>>xmap(
				featureConfiguration -> new ConfiguredStructureFeature<>(this, (C)featureConfiguration), configuredStructureFeature -> configuredStructureFeature.config
			)
			.codec();
	}

	public GenerationStep.Decoration step() {
		return (GenerationStep.Decoration)STEP.get(this);
	}

	public static void bootstrap() {
	}

	@Nullable
	public static StructureStart<?> loadStaticStart(StructureManager structureManager, CompoundTag compoundTag, long l) {
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
				BoundingBox boundingBox;
				if (compoundTag.contains("BB")) {
					boundingBox = (BoundingBox)BoundingBox.CODEC
						.parse(NbtOps.INSTANCE, compoundTag.get("BB"))
						.resultOrPartial(LOGGER::error)
						.orElse(new BoundingBox(BlockPos.ZERO));
				} else {
					boundingBox = BoundingBox.getUnknownBox();
				}

				ListTag listTag = compoundTag.getList("Children", 10);

				try {
					StructureStart<?> structureStart = structureFeature.createStart(chunkPos, boundingBox, i, l);

					for (int j = 0; j < listTag.size(); j++) {
						CompoundTag compoundTag2 = listTag.getCompound(j);
						String string2 = compoundTag2.getString("id").toLowerCase(Locale.ROOT);
						ResourceLocation resourceLocation = new ResourceLocation(string2);
						ResourceLocation resourceLocation2 = (ResourceLocation)RENAMES.getOrDefault(resourceLocation, resourceLocation);
						StructurePieceType structurePieceType = Registry.STRUCTURE_PIECE.get(resourceLocation2);
						if (structurePieceType == null) {
							LOGGER.error("Unknown structure piece id: {}", resourceLocation2);
						} else {
							try {
								StructurePiece structurePiece = structurePieceType.load(structureManager, compoundTag2);
								structureStart.getPieces().add(structurePiece);
							} catch (Exception var18) {
								LOGGER.error("Exception loading structure piece with id {}", resourceLocation2, var18);
							}
						}
					}

					return structureStart;
				} catch (Exception var19) {
					LOGGER.error("Failed Start with id {}", string, var19);
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
						boolean bl4 = levelReader.getBiomeManager().getPrimaryBiomeAtChunk(chunkPos).getGenerationSettings().isValidStart(this);
						if (bl4) {
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
		Biome biome,
		ChunkPos chunkPos2,
		C featureConfiguration,
		LevelHeightAccessor levelHeightAccessor
	) {
		return true;
	}

	private StructureStart<C> createStart(ChunkPos chunkPos, BoundingBox boundingBox, int i, long l) {
		return this.getStartFactory().create(this, chunkPos, boundingBox, i, l);
	}

	public StructureStart<?> generate(
		RegistryAccess registryAccess,
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		StructureManager structureManager,
		long l,
		ChunkPos chunkPos,
		Biome biome,
		int i,
		WorldgenRandom worldgenRandom,
		StructureFeatureConfiguration structureFeatureConfiguration,
		C featureConfiguration,
		LevelHeightAccessor levelHeightAccessor
	) {
		ChunkPos chunkPos2 = this.getPotentialFeatureChunk(structureFeatureConfiguration, l, worldgenRandom, chunkPos.x, chunkPos.z);
		if (chunkPos.x == chunkPos2.x
			&& chunkPos.z == chunkPos2.z
			&& this.isFeatureChunk(chunkGenerator, biomeSource, l, worldgenRandom, chunkPos, biome, chunkPos2, featureConfiguration, levelHeightAccessor)) {
			StructureStart<C> structureStart = this.createStart(chunkPos, BoundingBox.getUnknownBox(), i, l);
			structureStart.generatePieces(registryAccess, chunkGenerator, structureManager, chunkPos, biome, featureConfiguration, levelHeightAccessor);
			if (structureStart.isValid()) {
				return structureStart;
			}
		}

		return StructureStart.INVALID_START;
	}

	public abstract StructureFeature.StructureStartFactory<C> getStartFactory();

	public String getFeatureName() {
		return (String)STRUCTURES_REGISTRY.inverse().get(this);
	}

	public List<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
		return ImmutableList.of();
	}

	public List<MobSpawnSettings.SpawnerData> getSpecialAnimals() {
		return ImmutableList.of();
	}

	public interface StructureStartFactory<C extends FeatureConfiguration> {
		StructureStart<C> create(StructureFeature<C> structureFeature, ChunkPos chunkPos, BoundingBox boundingBox, int i, long l);
	}
}
