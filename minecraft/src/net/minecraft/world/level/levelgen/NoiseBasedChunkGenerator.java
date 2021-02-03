package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.levelgen.synth.SurfaceNoise;

public final class NoiseBasedChunkGenerator extends ChunkGenerator {
	public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BiomeSource.CODEC.fieldOf("biome_source").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.biomeSource),
					Codec.LONG.fieldOf("seed").stable().forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.seed),
					NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.settings)
				)
				.apply(instance, instance.stable(NoiseBasedChunkGenerator::new))
	);
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState[] EMPTY_COLUMN = new BlockState[0];
	private final int cellHeight;
	private final int cellWidth;
	private final int cellCountX;
	private final int cellCountY;
	private final int cellCountZ;
	protected final RandomSource random;
	private final SurfaceNoise surfaceNoise;
	protected final BlockState defaultBlock;
	protected final BlockState defaultFluid;
	private final long seed;
	protected final Supplier<NoiseGeneratorSettings> settings;
	private final int height;
	private final NoiseSampler sampler;

	public NoiseBasedChunkGenerator(BiomeSource biomeSource, long l, Supplier<NoiseGeneratorSettings> supplier) {
		this(biomeSource, biomeSource, l, supplier);
	}

	private NoiseBasedChunkGenerator(BiomeSource biomeSource, BiomeSource biomeSource2, long l, Supplier<NoiseGeneratorSettings> supplier) {
		super(biomeSource, biomeSource2, ((NoiseGeneratorSettings)supplier.get()).structureSettings(), l);
		this.seed = l;
		NoiseGeneratorSettings noiseGeneratorSettings = (NoiseGeneratorSettings)supplier.get();
		this.settings = supplier;
		NoiseSettings noiseSettings = noiseGeneratorSettings.noiseSettings();
		this.height = noiseSettings.height();
		this.cellHeight = QuartPos.toBlock(noiseSettings.noiseSizeVertical());
		this.cellWidth = QuartPos.toBlock(noiseSettings.noiseSizeHorizontal());
		this.defaultBlock = noiseGeneratorSettings.getDefaultBlock();
		this.defaultFluid = noiseGeneratorSettings.getDefaultFluid();
		this.cellCountX = 16 / this.cellWidth;
		this.cellCountY = noiseSettings.height() / this.cellHeight;
		this.cellCountZ = 16 / this.cellWidth;
		this.random = new WorldgenRandom(l);
		BlendedNoise blendedNoise = new BlendedNoise(this.random);
		this.surfaceNoise = (SurfaceNoise)(noiseSettings.useSimplexSurfaceNoise()
			? new PerlinSimplexNoise(this.random, IntStream.rangeClosed(-3, 0))
			: new PerlinNoise(this.random, IntStream.rangeClosed(-3, 0)));
		this.random.consumeCount(2620);
		PerlinNoise perlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
		SimplexNoise simplexNoise;
		if (noiseSettings.islandNoiseOverride()) {
			WorldgenRandom worldgenRandom = new WorldgenRandom(l);
			worldgenRandom.consumeCount(17292);
			simplexNoise = new SimplexNoise(worldgenRandom);
		} else {
			simplexNoise = null;
		}

		this.sampler = new NoiseSampler(biomeSource, this.cellWidth, this.cellHeight, this.cellCountY, noiseSettings, blendedNoise, simplexNoise, perlinNoise);
	}

	@Override
	protected Codec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ChunkGenerator withSeed(long l) {
		return new NoiseBasedChunkGenerator(this.biomeSource.withSeed(l), l, this.settings);
	}

	public boolean stable(long l, ResourceKey<NoiseGeneratorSettings> resourceKey) {
		return this.seed == l && ((NoiseGeneratorSettings)this.settings.get()).stable(resourceKey);
	}

	private double[] makeAndFillNoiseColumn(int i, int j, int k, int l) {
		double[] ds = new double[l + 1];
		this.sampler.fillNoiseColumn(ds, i, j, ((NoiseGeneratorSettings)this.settings.get()).noiseSettings(), this.getSeaLevel(), k, l);
		return ds;
	}

	@Override
	public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
		int k = Math.max(((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY(), levelHeightAccessor.getMinBuildHeight());
		int l = Math.min(
			((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY() + ((NoiseGeneratorSettings)this.settings.get()).noiseSettings().height(),
			levelHeightAccessor.getMaxBuildHeight()
		);
		int m = Mth.intFloorDiv(k, this.cellHeight);
		int n = Mth.intFloorDiv(l - k, this.cellHeight);
		return n <= 0
			? levelHeightAccessor.getMinBuildHeight()
			: this.iterateNoiseColumn(i, j, null, types.isOpaque(), m, n).orElse(levelHeightAccessor.getMinBuildHeight());
	}

	@Override
	public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor) {
		int k = Math.max(((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY(), levelHeightAccessor.getMinBuildHeight());
		int l = Math.min(
			((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY() + ((NoiseGeneratorSettings)this.settings.get()).noiseSettings().height(),
			levelHeightAccessor.getMaxBuildHeight()
		);
		int m = Mth.intFloorDiv(k, this.cellHeight);
		int n = Mth.intFloorDiv(l - k, this.cellHeight);
		if (n <= 0) {
			return new NoiseColumn(k, EMPTY_COLUMN);
		} else {
			BlockState[] blockStates = new BlockState[n * this.cellHeight];
			this.iterateNoiseColumn(i, j, blockStates, null, m, n);
			return new NoiseColumn(k, blockStates);
		}
	}

	private OptionalInt iterateNoiseColumn(int i, int j, @Nullable BlockState[] blockStates, @Nullable Predicate<BlockState> predicate, int k, int l) {
		int m = Math.floorDiv(i, this.cellWidth);
		int n = Math.floorDiv(j, this.cellWidth);
		int o = Math.floorMod(i, this.cellWidth);
		int p = Math.floorMod(j, this.cellWidth);
		double d = (double)o / (double)this.cellWidth;
		double e = (double)p / (double)this.cellWidth;
		double[][] ds = new double[][]{
			this.makeAndFillNoiseColumn(m, n, k, l),
			this.makeAndFillNoiseColumn(m, n + 1, k, l),
			this.makeAndFillNoiseColumn(m + 1, n, k, l),
			this.makeAndFillNoiseColumn(m + 1, n + 1, k, l)
		};

		for (int q = l - 1; q >= 0; q--) {
			double f = ds[0][q];
			double g = ds[1][q];
			double h = ds[2][q];
			double r = ds[3][q];
			double s = ds[0][q + 1];
			double t = ds[1][q + 1];
			double u = ds[2][q + 1];
			double v = ds[3][q + 1];

			for (int w = this.cellHeight - 1; w >= 0; w--) {
				double x = (double)w / (double)this.cellHeight;
				double y = Mth.lerp3(x, d, e, f, s, h, u, g, t, r, v);
				int z = q * this.cellHeight + w;
				int aa = z + k * this.cellHeight;
				BlockState blockState = this.updateNoiseAndGenerateBaseState(Beardifier.NO_BEARDS, i, aa, j, y);
				if (blockStates != null) {
					blockStates[z] = blockState;
				}

				if (predicate != null && predicate.test(blockState)) {
					return OptionalInt.of(aa + 1);
				}
			}
		}

		return OptionalInt.empty();
	}

	protected BlockState updateNoiseAndGenerateBaseState(Beardifier beardifier, int i, int j, int k, double d) {
		double e = Mth.clamp(d / 200.0, -1.0, 1.0);
		e = e / 2.0 - e * e * e / 24.0;
		e += beardifier.beardify(i, j, k);
		BlockState blockState;
		if (e > 0.0) {
			blockState = this.defaultBlock;
		} else if (j < this.getSeaLevel()) {
			blockState = this.defaultFluid;
		} else {
			blockState = AIR;
		}

		return blockState;
	}

	@Override
	public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
		ChunkPos chunkPos = chunkAccess.getPos();
		int i = chunkPos.x;
		int j = chunkPos.z;
		WorldgenRandom worldgenRandom = new WorldgenRandom();
		worldgenRandom.setBaseChunkSeed(i, j);
		ChunkPos chunkPos2 = chunkAccess.getPos();
		int k = chunkPos2.getMinBlockX();
		int l = chunkPos2.getMinBlockZ();
		double d = 0.0625;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int m = 0; m < 16; m++) {
			for (int n = 0; n < 16; n++) {
				int o = k + m;
				int p = l + n;
				int q = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, m, n) + 1;
				double e = this.surfaceNoise.getSurfaceNoiseValue((double)o * 0.0625, (double)p * 0.0625, 0.0625, (double)m * 0.0625) * 15.0;
				worldGenRegion.getBiome(mutableBlockPos.set(k + m, q, l + n))
					.buildSurfaceAt(worldgenRandom, chunkAccess, o, p, q, e, this.defaultBlock, this.defaultFluid, this.getSeaLevel(), worldGenRegion.getSeed());
			}
		}

		this.setBedrock(chunkAccess, worldgenRandom);
	}

	private void setBedrock(ChunkAccess chunkAccess, Random random) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int i = chunkAccess.getPos().getMinBlockX();
		int j = chunkAccess.getPos().getMinBlockZ();
		NoiseGeneratorSettings noiseGeneratorSettings = (NoiseGeneratorSettings)this.settings.get();
		int k = noiseGeneratorSettings.getBedrockFloorPosition();
		int l = this.height - 1 - noiseGeneratorSettings.getBedrockRoofPosition();
		int m = 5;
		boolean bl = l + 5 - 1 >= chunkAccess.getMinBuildHeight() && l < chunkAccess.getMaxBuildHeight();
		boolean bl2 = k + 5 - 1 >= chunkAccess.getMinBuildHeight() && k < chunkAccess.getMaxBuildHeight();
		if (bl || bl2) {
			for (BlockPos blockPos : BlockPos.betweenClosed(i, 0, j, i + 15, 0, j + 15)) {
				if (bl) {
					for (int n = 0; n < 5; n++) {
						if (n <= random.nextInt(5)) {
							chunkAccess.setBlockState(mutableBlockPos.set(blockPos.getX(), l - n, blockPos.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
						}
					}
				}

				if (bl2) {
					for (int nx = 4; nx >= 0; nx--) {
						if (nx <= random.nextInt(5)) {
							chunkAccess.setBlockState(mutableBlockPos.set(blockPos.getX(), k + nx, blockPos.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
						}
					}
				}
			}
		}
	}

	@Override
	public void fillFromNoise(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		ChunkPos chunkPos = chunkAccess.getPos();
		ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
		Heightmap heightmap = protoChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap heightmap2 = protoChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		int i = Math.max(((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY(), chunkAccess.getMinBuildHeight());
		int j = Math.min(
			((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY() + ((NoiseGeneratorSettings)this.settings.get()).noiseSettings().height(),
			chunkAccess.getMaxBuildHeight()
		);
		int k = Mth.intFloorDiv(i, this.cellHeight);
		int l = Mth.intFloorDiv(j - i, this.cellHeight);
		if (l > 0) {
			int m = chunkPos.x;
			int n = chunkPos.z;
			int o = chunkPos.getMinBlockX();
			int p = chunkPos.getMinBlockZ();
			Beardifier beardifier = new Beardifier(structureFeatureManager, chunkAccess);
			double[][][] ds = new double[2][this.cellCountZ + 1][l + 1];
			NoiseSettings noiseSettings = ((NoiseGeneratorSettings)this.settings.get()).noiseSettings();

			for (int q = 0; q < this.cellCountZ + 1; q++) {
				ds[0][q] = new double[l + 1];
				double[] es = ds[0][q];
				int r = m * this.cellCountX;
				int s = n * this.cellCountZ + q;
				this.sampler.fillNoiseColumn(es, r, s, noiseSettings, this.getSeaLevel(), k, l);
				ds[1][q] = new double[l + 1];
			}

			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int t = 0; t < this.cellCountX; t++) {
				int r = m * this.cellCountX + t + 1;

				for (int s = 0; s < this.cellCountZ + 1; s++) {
					double[] fs = ds[1][s];
					int u = n * this.cellCountZ + s;
					this.sampler.fillNoiseColumn(fs, r, u, noiseSettings, this.getSeaLevel(), k, l);
				}

				for (int s = 0; s < this.cellCountZ; s++) {
					LevelChunkSection levelChunkSection = protoChunk.getOrCreateSection(protoChunk.getSectionsCount() - 1);
					levelChunkSection.acquire();

					for (int u = l - 1; u >= 0; u--) {
						double d = ds[0][s][u];
						double e = ds[0][s + 1][u];
						double f = ds[1][s][u];
						double g = ds[1][s + 1][u];
						double h = ds[0][s][u + 1];
						double v = ds[0][s + 1][u + 1];
						double w = ds[1][s][u + 1];
						double x = ds[1][s + 1][u + 1];

						for (int y = this.cellHeight - 1; y >= 0; y--) {
							int z = u * this.cellHeight + y + ((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY();
							int aa = z & 15;
							int ab = protoChunk.getSectionIndex(z);
							if (protoChunk.getSectionIndex(levelChunkSection.bottomBlockY()) != ab) {
								levelChunkSection.release();
								levelChunkSection = protoChunk.getOrCreateSection(ab);
								levelChunkSection.acquire();
							}

							double ac = (double)y / (double)this.cellHeight;
							double ad = Mth.lerp(ac, d, h);
							double ae = Mth.lerp(ac, f, w);
							double af = Mth.lerp(ac, e, v);
							double ag = Mth.lerp(ac, g, x);

							for (int ah = 0; ah < this.cellWidth; ah++) {
								int ai = o + t * this.cellWidth + ah;
								int aj = ai & 15;
								double ak = (double)ah / (double)this.cellWidth;
								double al = Mth.lerp(ak, ad, ae);
								double am = Mth.lerp(ak, af, ag);

								for (int an = 0; an < this.cellWidth; an++) {
									int ao = p + s * this.cellWidth + an;
									int ap = ao & 15;
									double aq = (double)an / (double)this.cellWidth;
									double ar = Mth.lerp(aq, al, am);
									BlockState blockState = this.updateNoiseAndGenerateBaseState(beardifier, ai, z, ao, ar);
									if (blockState != AIR) {
										if (blockState.getLightEmission() != 0) {
											mutableBlockPos.set(ai, z, ao);
											protoChunk.addLight(mutableBlockPos);
										}

										levelChunkSection.setBlockState(aj, aa, ap, blockState, false);
										heightmap.update(aj, z, ap, blockState);
										heightmap2.update(aj, z, ap, blockState);
									}
								}
							}
						}
					}

					levelChunkSection.release();
				}

				this.swapFirstTwoElements(ds);
			}
		}
	}

	public <T> void swapFirstTwoElements(T[] objects) {
		T object = objects[0];
		objects[0] = objects[1];
		objects[1] = object;
	}

	@Override
	public int getGenDepth() {
		return this.height;
	}

	@Override
	public int getSeaLevel() {
		return ((NoiseGeneratorSettings)this.settings.get()).seaLevel();
	}

	@Override
	public List<MobSpawnSettings.SpawnerData> getMobsAt(Biome biome, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos) {
		if (structureFeatureManager.getStructureAt(blockPos, true, StructureFeature.SWAMP_HUT).isValid()) {
			if (mobCategory == MobCategory.MONSTER) {
				return StructureFeature.SWAMP_HUT.getSpecialEnemies();
			}

			if (mobCategory == MobCategory.CREATURE) {
				return StructureFeature.SWAMP_HUT.getSpecialAnimals();
			}
		}

		if (mobCategory == MobCategory.MONSTER) {
			if (structureFeatureManager.getStructureAt(blockPos, false, StructureFeature.PILLAGER_OUTPOST).isValid()) {
				return StructureFeature.PILLAGER_OUTPOST.getSpecialEnemies();
			}

			if (structureFeatureManager.getStructureAt(blockPos, false, StructureFeature.OCEAN_MONUMENT).isValid()) {
				return StructureFeature.OCEAN_MONUMENT.getSpecialEnemies();
			}

			if (structureFeatureManager.getStructureAt(blockPos, true, StructureFeature.NETHER_BRIDGE).isValid()) {
				return StructureFeature.NETHER_BRIDGE.getSpecialEnemies();
			}
		}

		return super.getMobsAt(biome, structureFeatureManager, mobCategory, blockPos);
	}

	@Override
	public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
		if (!((NoiseGeneratorSettings)this.settings.get()).disableMobGeneration()) {
			ChunkPos chunkPos = worldGenRegion.getCenter();
			Biome biome = worldGenRegion.getBiome(chunkPos.getWorldPosition());
			WorldgenRandom worldgenRandom = new WorldgenRandom();
			worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
			NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, biome, chunkPos, worldgenRandom);
		}
	}
}
