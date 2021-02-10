package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public abstract class ChunkGenerator {
	public static final Codec<ChunkGenerator> CODEC = Registry.CHUNK_GENERATOR.dispatchStable(ChunkGenerator::codec, Function.identity());
	protected final BiomeSource biomeSource;
	protected final BiomeSource runtimeBiomeSource;
	private final StructureSettings settings;
	private final long strongholdSeed;
	private final List<ChunkPos> strongholdPositions = Lists.<ChunkPos>newArrayList();

	public ChunkGenerator(BiomeSource biomeSource, StructureSettings structureSettings) {
		this(biomeSource, biomeSource, structureSettings, 0L);
	}

	public ChunkGenerator(BiomeSource biomeSource, BiomeSource biomeSource2, StructureSettings structureSettings, long l) {
		this.biomeSource = biomeSource;
		this.runtimeBiomeSource = biomeSource2;
		this.settings = structureSettings;
		this.strongholdSeed = l;
	}

	private void generateStrongholds() {
		if (this.strongholdPositions.isEmpty()) {
			StrongholdConfiguration strongholdConfiguration = this.settings.stronghold();
			if (strongholdConfiguration != null && strongholdConfiguration.count() != 0) {
				List<Biome> list = Lists.<Biome>newArrayList();

				for (Biome biome : this.biomeSource.possibleBiomes()) {
					if (biome.getGenerationSettings().isValidStart(StructureFeature.STRONGHOLD)) {
						list.add(biome);
					}
				}

				int i = strongholdConfiguration.distance();
				int j = strongholdConfiguration.count();
				int k = strongholdConfiguration.spread();
				Random random = new Random();
				random.setSeed(this.strongholdSeed);
				double d = random.nextDouble() * Math.PI * 2.0;
				int l = 0;
				int m = 0;

				for (int n = 0; n < j; n++) {
					double e = (double)(4 * i + i * m * 6) + (random.nextDouble() - 0.5) * (double)i * 2.5;
					int o = (int)Math.round(Math.cos(d) * e);
					int p = (int)Math.round(Math.sin(d) * e);
					BlockPos blockPos = this.biomeSource
						.findBiomeHorizontal(SectionPos.sectionToBlockCoord(o, 8), 0, SectionPos.sectionToBlockCoord(p, 8), 112, list::contains, random);
					if (blockPos != null) {
						o = SectionPos.blockToSectionCoord(blockPos.getX());
						p = SectionPos.blockToSectionCoord(blockPos.getZ());
					}

					this.strongholdPositions.add(new ChunkPos(o, p));
					d += (Math.PI * 2) / (double)k;
					if (++l == k) {
						m++;
						l = 0;
						k += 2 * k / (m + 1);
						k = Math.min(k, j - n);
						d += random.nextDouble() * Math.PI * 2.0;
					}
				}
			}
		}
	}

	protected abstract Codec<? extends ChunkGenerator> codec();

	@Environment(EnvType.CLIENT)
	public abstract ChunkGenerator withSeed(long l);

	public void createBiomes(Registry<Biome> registry, ChunkAccess chunkAccess) {
		ChunkPos chunkPos = chunkAccess.getPos();
		((ProtoChunk)chunkAccess).setBiomes(new ChunkBiomeContainer(registry, chunkAccess, chunkPos, this.runtimeBiomeSource));
	}

	public void applyCarvers(long l, BiomeManager biomeManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
		BiomeManager biomeManager2 = biomeManager.withDifferentSource(this.biomeSource);
		WorldgenRandom worldgenRandom = new WorldgenRandom();
		int i = 8;
		ChunkPos chunkPos = chunkAccess.getPos();
		int j = chunkPos.x;
		int k = chunkPos.z;
		BiomeGenerationSettings biomeGenerationSettings = this.biomeSource
			.getNoiseBiome(QuartPos.fromBlock(chunkPos.getMinBlockX()), 0, QuartPos.fromBlock(chunkPos.getMinBlockZ()))
			.getGenerationSettings();
		BitSet bitSet = ((ProtoChunk)chunkAccess).getOrCreateCarvingMask(carving);

		for (int m = j - 8; m <= j + 8; m++) {
			for (int n = k - 8; n <= k + 8; n++) {
				List<Supplier<ConfiguredWorldCarver<?>>> list = biomeGenerationSettings.getCarvers(carving);
				ListIterator<Supplier<ConfiguredWorldCarver<?>>> listIterator = list.listIterator();

				while (listIterator.hasNext()) {
					int o = listIterator.nextIndex();
					ConfiguredWorldCarver<?> configuredWorldCarver = (ConfiguredWorldCarver<?>)((Supplier)listIterator.next()).get();
					worldgenRandom.setLargeFeatureSeed(l + (long)o, m, n);
					if (configuredWorldCarver.isStartChunk(worldgenRandom, m, n)) {
						configuredWorldCarver.carve(chunkAccess, biomeManager2::getBiome, worldgenRandom, this.getSeaLevel(), m, n, j, k, bitSet);
					}
				}
			}
		}
	}

	@Nullable
	public BlockPos findNearestMapFeature(ServerLevel serverLevel, StructureFeature<?> structureFeature, BlockPos blockPos, int i, boolean bl) {
		if (!this.biomeSource.canGenerateStructure(structureFeature)) {
			return null;
		} else if (structureFeature == StructureFeature.STRONGHOLD) {
			this.generateStrongholds();
			BlockPos blockPos2 = null;
			double d = Double.MAX_VALUE;
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (ChunkPos chunkPos : this.strongholdPositions) {
				mutableBlockPos.set(SectionPos.sectionToBlockCoord(chunkPos.x, 8), 32, SectionPos.sectionToBlockCoord(chunkPos.z, 8));
				double e = mutableBlockPos.distSqr(blockPos);
				if (blockPos2 == null) {
					blockPos2 = new BlockPos(mutableBlockPos);
					d = e;
				} else if (e < d) {
					blockPos2 = new BlockPos(mutableBlockPos);
					d = e;
				}
			}

			return blockPos2;
		} else {
			StructureFeatureConfiguration structureFeatureConfiguration = this.settings.getConfig(structureFeature);
			return structureFeatureConfiguration == null
				? null
				: structureFeature.getNearestGeneratedFeature(
					serverLevel, serverLevel.structureFeatureManager(), blockPos, i, bl, serverLevel.getSeed(), structureFeatureConfiguration
				);
		}
	}

	public void applyBiomeDecoration(WorldGenRegion worldGenRegion, StructureFeatureManager structureFeatureManager) {
		ChunkPos chunkPos = worldGenRegion.getCenter();
		int i = chunkPos.getMinBlockX();
		int j = chunkPos.getMinBlockZ();
		BlockPos blockPos = new BlockPos(i, worldGenRegion.getMinBuildHeight(), j);
		Biome biome = this.biomeSource.getPrimaryBiome(chunkPos);
		WorldgenRandom worldgenRandom = new WorldgenRandom();
		long l = worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), i, j);

		try {
			biome.generate(structureFeatureManager, this, worldGenRegion, l, worldgenRandom, blockPos);
		} catch (Exception var13) {
			CrashReport crashReport = CrashReport.forThrowable(var13, "Biome decoration");
			crashReport.addCategory("Generation").setDetail("CenterX", chunkPos.x).setDetail("CenterZ", chunkPos.z).setDetail("Seed", l).setDetail("Biome", biome);
			throw new ReportedException(crashReport);
		}
	}

	public abstract void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess);

	public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
	}

	public StructureSettings getSettings() {
		return this.settings;
	}

	public int getSpawnHeight() {
		return 64;
	}

	public BiomeSource getBiomeSource() {
		return this.runtimeBiomeSource;
	}

	public int getGenDepth() {
		return 384;
	}

	public List<MobSpawnSettings.SpawnerData> getMobsAt(Biome biome, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos) {
		return biome.getMobSettings().getMobs(mobCategory);
	}

	public void createStructures(
		RegistryAccess registryAccess, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, StructureManager structureManager, long l
	) {
		Biome biome = this.biomeSource.getPrimaryBiome(chunkAccess.getPos());
		this.createStructure(StructureFeatures.STRONGHOLD, registryAccess, structureFeatureManager, chunkAccess, structureManager, l, biome);

		for (Supplier<ConfiguredStructureFeature<?, ?>> supplier : biome.getGenerationSettings().structures()) {
			this.createStructure((ConfiguredStructureFeature<?, ?>)supplier.get(), registryAccess, structureFeatureManager, chunkAccess, structureManager, l, biome);
		}
	}

	private void createStructure(
		ConfiguredStructureFeature<?, ?> configuredStructureFeature,
		RegistryAccess registryAccess,
		StructureFeatureManager structureFeatureManager,
		ChunkAccess chunkAccess,
		StructureManager structureManager,
		long l,
		Biome biome
	) {
		ChunkPos chunkPos = chunkAccess.getPos();
		SectionPos sectionPos = SectionPos.bottomOf(chunkAccess);
		StructureStart<?> structureStart = structureFeatureManager.getStartForFeature(sectionPos, configuredStructureFeature.feature, chunkAccess);
		int i = structureStart != null ? structureStart.getReferences() : 0;
		StructureFeatureConfiguration structureFeatureConfiguration = this.settings.getConfig(configuredStructureFeature.feature);
		if (structureFeatureConfiguration != null) {
			StructureStart<?> structureStart2 = configuredStructureFeature.generate(
				registryAccess, this, this.biomeSource, structureManager, l, chunkPos, biome, i, structureFeatureConfiguration, chunkAccess
			);
			structureFeatureManager.setStartForFeature(sectionPos, configuredStructureFeature.feature, structureStart2, chunkAccess);
		}
	}

	public void createReferences(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		int i = 8;
		ChunkPos chunkPos = chunkAccess.getPos();
		int j = chunkPos.x;
		int k = chunkPos.z;
		int l = chunkPos.getMinBlockX();
		int m = chunkPos.getMinBlockZ();
		SectionPos sectionPos = SectionPos.bottomOf(chunkAccess);

		for (int n = j - 8; n <= j + 8; n++) {
			for (int o = k - 8; o <= k + 8; o++) {
				long p = ChunkPos.asLong(n, o);

				for (StructureStart<?> structureStart : worldGenLevel.getChunk(n, o).getAllStarts().values()) {
					try {
						if (structureStart != StructureStart.INVALID_START && structureStart.getBoundingBox().intersects(l, m, l + 15, m + 15)) {
							structureFeatureManager.addReferenceForFeature(sectionPos, structureStart.getFeature(), p, chunkAccess);
							DebugPackets.sendStructurePacket(worldGenLevel, structureStart);
						}
					} catch (Exception var20) {
						CrashReport crashReport = CrashReport.forThrowable(var20, "Generating structure reference");
						CrashReportCategory crashReportCategory = crashReport.addCategory("Structure");
						crashReportCategory.setDetail("Id", (CrashReportDetail<String>)(() -> Registry.STRUCTURE_FEATURE.getKey(structureStart.getFeature()).toString()));
						crashReportCategory.setDetail("Name", (CrashReportDetail<String>)(() -> structureStart.getFeature().getFeatureName()));
						crashReportCategory.setDetail("Class", (CrashReportDetail<String>)(() -> structureStart.getFeature().getClass().getCanonicalName()));
						throw new ReportedException(crashReport);
					}
				}
			}
		}
	}

	public abstract CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess);

	public int getSeaLevel() {
		return 63;
	}

	public int getMinY() {
		return 0;
	}

	public abstract int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor);

	public abstract NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor);

	public int getFirstFreeHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
		return this.getBaseHeight(i, j, types, levelHeightAccessor);
	}

	public int getFirstOccupiedHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
		return this.getBaseHeight(i, j, types, levelHeightAccessor) - 1;
	}

	public boolean hasStronghold(ChunkPos chunkPos) {
		this.generateStrongholds();
		return this.strongholdPositions.contains(chunkPos);
	}

	static {
		Registry.register(Registry.CHUNK_GENERATOR, "noise", NoiseBasedChunkGenerator.CODEC);
		Registry.register(Registry.CHUNK_GENERATOR, "flat", FlatLevelSource.CODEC);
		Registry.register(Registry.CHUNK_GENERATOR, "debug", DebugLevelSource.CODEC);
	}
}
