package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
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
					if (biome.isValidStart(StructureFeature.STRONGHOLD)) {
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
					BlockPos blockPos = this.biomeSource.findBiomeHorizontal((o << 4) + 8, 0, (p << 4) + 8, 112, list, random);
					if (blockPos != null) {
						o = blockPos.getX() >> 4;
						p = blockPos.getZ() >> 4;
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

	public void createBiomes(ChunkAccess chunkAccess) {
		ChunkPos chunkPos = chunkAccess.getPos();
		((ProtoChunk)chunkAccess).setBiomes(new ChunkBiomeContainer(chunkPos, this.runtimeBiomeSource));
	}

	public void applyCarvers(long l, BiomeManager biomeManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
		BiomeManager biomeManager2 = biomeManager.withDifferentSource(this.biomeSource);
		WorldgenRandom worldgenRandom = new WorldgenRandom();
		int i = 8;
		ChunkPos chunkPos = chunkAccess.getPos();
		int j = chunkPos.x;
		int k = chunkPos.z;
		Biome biome = this.biomeSource.getNoiseBiome(chunkPos.x << 2, 0, chunkPos.z << 2);
		BitSet bitSet = ((ProtoChunk)chunkAccess).getOrCreateCarvingMask(carving);

		for (int m = j - 8; m <= j + 8; m++) {
			for (int n = k - 8; n <= k + 8; n++) {
				List<ConfiguredWorldCarver<?>> list = biome.getCarvers(carving);
				ListIterator<ConfiguredWorldCarver<?>> listIterator = list.listIterator();

				while (listIterator.hasNext()) {
					int o = listIterator.nextIndex();
					ConfiguredWorldCarver<?> configuredWorldCarver = (ConfiguredWorldCarver<?>)listIterator.next();
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
				mutableBlockPos.set((chunkPos.x << 4) + 8, 32, (chunkPos.z << 4) + 8);
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
			return structureFeature.getNearestGeneratedFeature(
				serverLevel, serverLevel.structureFeatureManager(), blockPos, i, bl, serverLevel.getSeed(), this.settings.getConfig(structureFeature)
			);
		}
	}

	public void applyBiomeDecoration(WorldGenRegion worldGenRegion, StructureFeatureManager structureFeatureManager) {
		int i = worldGenRegion.getCenterX();
		int j = worldGenRegion.getCenterZ();
		int k = i * 16;
		int l = j * 16;
		BlockPos blockPos = new BlockPos(k, 0, l);
		Biome biome = this.biomeSource.getNoiseBiome((i << 2) + 2, 2, (j << 2) + 2);
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
		return 256;
	}

	public List<Biome.SpawnerData> getMobsAt(Biome biome, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos) {
		return biome.getMobs(mobCategory);
	}

	public void createStructures(StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, StructureManager structureManager, long l) {
		ChunkPos chunkPos = chunkAccess.getPos();
		Biome biome = this.biomeSource.getNoiseBiome((chunkPos.x << 2) + 2, 0, (chunkPos.z << 2) + 2);
		this.createStructure(BiomeDefaultFeatures.STRONGHOLD, structureFeatureManager, chunkAccess, structureManager, l, chunkPos, biome);

		for (ConfiguredStructureFeature<?, ?> configuredStructureFeature : biome.structures()) {
			this.createStructure(configuredStructureFeature, structureFeatureManager, chunkAccess, structureManager, l, chunkPos, biome);
		}
	}

	private void createStructure(
		ConfiguredStructureFeature<?, ?> configuredStructureFeature,
		StructureFeatureManager structureFeatureManager,
		ChunkAccess chunkAccess,
		StructureManager structureManager,
		long l,
		ChunkPos chunkPos,
		Biome biome
	) {
		StructureStart<?> structureStart = structureFeatureManager.getStartForFeature(
			SectionPos.of(chunkAccess.getPos(), 0), configuredStructureFeature.feature, chunkAccess
		);
		int i = structureStart != null ? structureStart.getReferences() : 0;
		StructureStart<?> structureStart2 = configuredStructureFeature.generate(
			this, this.biomeSource, structureManager, l, chunkPos, biome, i, this.settings.getConfig(configuredStructureFeature.feature)
		);
		structureFeatureManager.setStartForFeature(SectionPos.of(chunkAccess.getPos(), 0), configuredStructureFeature.feature, structureStart2, chunkAccess);
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

				for (Entry<String, StructureStart<?>> entry : levelAccessor.getChunk(n, o).getAllStarts().entrySet()) {
					StructureStart<?> structureStart = (StructureStart<?>)entry.getValue();
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
