package net.minecraft.world.level.chunk;

import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public abstract class ChunkGenerator<C extends ChunkGeneratorSettings> {
	protected final LevelAccessor level;
	protected final long seed;
	protected final BiomeSource biomeSource;
	protected final C settings;

	public ChunkGenerator(LevelAccessor levelAccessor, BiomeSource biomeSource, C chunkGeneratorSettings) {
		this.level = levelAccessor;
		this.seed = levelAccessor.getSeed();
		this.biomeSource = biomeSource;
		this.settings = chunkGeneratorSettings;
	}

	public void createBiomes(ChunkAccess chunkAccess) {
		ChunkPos chunkPos = chunkAccess.getPos();
		((ProtoChunk)chunkAccess).setBiomes(new ChunkBiomeContainer(chunkPos, this.biomeSource));
	}

	protected Biome getCarvingOrDecorationBiome(BiomeManager biomeManager, BlockPos blockPos) {
		return biomeManager.getBiome(blockPos);
	}

	public void applyCarvers(BiomeManager biomeManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
		WorldgenRandom worldgenRandom = new WorldgenRandom();
		int i = 8;
		ChunkPos chunkPos = chunkAccess.getPos();
		int j = chunkPos.x;
		int k = chunkPos.z;
		Biome biome = this.getCarvingOrDecorationBiome(biomeManager, chunkPos.getWorldPosition());
		BitSet bitSet = chunkAccess.getCarvingMask(carving);

		for (int l = j - 8; l <= j + 8; l++) {
			for (int m = k - 8; m <= k + 8; m++) {
				List<ConfiguredWorldCarver<?>> list = biome.getCarvers(carving);
				ListIterator<ConfiguredWorldCarver<?>> listIterator = list.listIterator();

				while (listIterator.hasNext()) {
					int n = listIterator.nextIndex();
					ConfiguredWorldCarver<?> configuredWorldCarver = (ConfiguredWorldCarver<?>)listIterator.next();
					worldgenRandom.setLargeFeatureSeed(this.seed + (long)n, l, m);
					if (configuredWorldCarver.isStartChunk(worldgenRandom, l, m)) {
						configuredWorldCarver.carve(
							chunkAccess, blockPos -> this.getCarvingOrDecorationBiome(biomeManager, blockPos), worldgenRandom, this.getSeaLevel(), l, m, j, k, bitSet
						);
					}
				}
			}
		}
	}

	@Nullable
	public BlockPos findNearestMapFeature(ServerLevel serverLevel, String string, BlockPos blockPos, int i, boolean bl) {
		StructureFeature<?> structureFeature = (StructureFeature<?>)Feature.STRUCTURES_REGISTRY.get(string.toLowerCase(Locale.ROOT));
		return structureFeature != null ? structureFeature.getNearestGeneratedFeature(serverLevel, this, blockPos, i, bl) : null;
	}

	public void applyBiomeDecoration(WorldGenRegion worldGenRegion, StructureFeatureManager structureFeatureManager) {
		int i = worldGenRegion.getCenterX();
		int j = worldGenRegion.getCenterZ();
		int k = i * 16;
		int l = j * 16;
		BlockPos blockPos = new BlockPos(k, 0, l);
		Biome biome = this.getCarvingOrDecorationBiome(worldGenRegion.getBiomeManager(), blockPos.offset(8, 8, 8));
		WorldgenRandom worldgenRandom = new WorldgenRandom();
		long m = worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), k, l);

		for (GenerationStep.Decoration decoration : GenerationStep.Decoration.values()) {
			try {
				biome.generate(decoration, structureFeatureManager, this, worldGenRegion, m, worldgenRandom, blockPos);
			} catch (Exception var18) {
				CrashReport crashReport = CrashReport.forThrowable(var18, "Biome decoration");
				crashReport.addCategory("Generation")
					.setDetail("CenterX", i)
					.setDetail("CenterZ", j)
					.setDetail("Step", decoration)
					.setDetail("Seed", m)
					.setDetail("Biome", Registry.BIOME.getKey(biome));
				throw new ReportedException(crashReport);
			}
		}
	}

	public abstract void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess);

	public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
	}

	public C getSettings() {
		return this.settings;
	}

	public abstract int getSpawnHeight();

	public void tickCustomSpawners(ServerLevel serverLevel, boolean bl, boolean bl2) {
	}

	public boolean isBiomeValidStartForStructure(Biome biome, StructureFeature<? extends FeatureConfiguration> structureFeature) {
		return biome.isValidStart(structureFeature);
	}

	@Nullable
	public <C extends FeatureConfiguration> C getStructureConfiguration(Biome biome, StructureFeature<C> structureFeature) {
		return biome.getStructureConfiguration(structureFeature);
	}

	public BiomeSource getBiomeSource() {
		return this.biomeSource;
	}

	public long getSeed() {
		return this.seed;
	}

	public int getGenDepth() {
		return 256;
	}

	public List<Biome.SpawnerData> getMobsAt(StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos) {
		return this.level.getBiome(blockPos).getMobs(mobCategory);
	}

	public void createStructures(
		StructureFeatureManager structureFeatureManager,
		BiomeManager biomeManager,
		ChunkAccess chunkAccess,
		ChunkGenerator<?> chunkGenerator,
		StructureManager structureManager
	) {
		for (StructureFeature<?> structureFeature : Feature.STRUCTURES_REGISTRY.values()) {
			if (chunkGenerator.getBiomeSource().canGenerateStructure(structureFeature)) {
				StructureStart structureStart = structureFeatureManager.getStartForFeature(SectionPos.of(chunkAccess.getPos(), 0), structureFeature, chunkAccess);
				int i = structureStart != null ? structureStart.getReferences() : 0;
				WorldgenRandom worldgenRandom = new WorldgenRandom();
				ChunkPos chunkPos = chunkAccess.getPos();
				StructureStart structureStart2 = StructureStart.INVALID_START;
				Biome biome = biomeManager.getBiome(new BlockPos(chunkPos.getMinBlockX() + 9, 0, chunkPos.getMinBlockZ() + 9));
				if (structureFeature.isFeatureChunk(biomeManager, chunkGenerator, worldgenRandom, chunkPos.x, chunkPos.z, biome)) {
					StructureStart structureStart3 = structureFeature.getStartFactory()
						.create(structureFeature, chunkPos.x, chunkPos.z, BoundingBox.getUnknownBox(), i, chunkGenerator.getSeed());
					structureStart3.generatePieces(this, structureManager, chunkPos.x, chunkPos.z, biome);
					structureStart2 = structureStart3.isValid() ? structureStart3 : StructureStart.INVALID_START;
				}

				structureFeatureManager.setStartForFeature(SectionPos.of(chunkAccess.getPos(), 0), structureFeature, structureStart2, chunkAccess);
			}
		}
	}

	public void createReferences(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		int i = 8;
		int j = chunkAccess.getPos().x;
		int k = chunkAccess.getPos().z;
		int l = j << 4;
		int m = k << 4;
		SectionPos sectionPos = SectionPos.of(chunkAccess.getPos(), 0);

		for (int n = j - 8; n <= j + 8; n++) {
			for (int o = k - 8; o <= k + 8; o++) {
				long p = ChunkPos.asLong(n, o);

				for (Entry<String, StructureStart> entry : levelAccessor.getChunk(n, o).getAllStarts().entrySet()) {
					StructureStart structureStart = (StructureStart)entry.getValue();
					if (structureStart != StructureStart.INVALID_START && structureStart.getBoundingBox().intersects(l, m, l + 15, m + 15)) {
						structureFeatureManager.addReferenceForFeature(sectionPos, structureStart.getFeature(), p, chunkAccess);
						DebugPackets.sendStructurePacket(levelAccessor, structureStart);
					}
				}
			}
		}
	}

	public abstract void fillFromNoise(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess);

	public int getSeaLevel() {
		return 63;
	}

	public abstract int getBaseHeight(int i, int j, Heightmap.Types types);

	public abstract BlockGetter getBaseColumn(int i, int j);

	public int getFirstFreeHeight(int i, int j, Heightmap.Types types) {
		return this.getBaseHeight(i, j, types);
	}

	public int getFirstOccupiedHeight(int i, int j, Heightmap.Types types) {
		return this.getBaseHeight(i, j, types) - 1;
	}
}
