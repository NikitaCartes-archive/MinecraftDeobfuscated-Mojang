package net.minecraft.world.level.levelgen;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.SurfaceNoise;

public abstract class NoiseBasedChunkGenerator<T extends ChunkGeneratorSettings> extends ChunkGenerator<T> {
	private static final float[] BEARD_KERNEL = Util.make(new float[13824], fs -> {
		for (int i = 0; i < 24; i++) {
			for (int j = 0; j < 24; j++) {
				for (int k = 0; k < 24; k++) {
					fs[i * 24 * 24 + j * 24 + k] = (float)computeContribution(j - 12, k - 12, i - 12);
				}
			}
		}
	});
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private final int chunkHeight;
	private final int chunkWidth;
	private final int chunkCountX;
	private final int chunkCountY;
	private final int chunkCountZ;
	protected final WorldgenRandom random;
	private final PerlinNoise minLimitPerlinNoise;
	private final PerlinNoise maxLimitPerlinNoise;
	private final PerlinNoise mainPerlinNoise;
	private final SurfaceNoise surfaceNoise;
	protected final BlockState defaultBlock;
	protected final BlockState defaultFluid;

	public NoiseBasedChunkGenerator(LevelAccessor levelAccessor, BiomeSource biomeSource, int i, int j, int k, T chunkGeneratorSettings, boolean bl) {
		super(levelAccessor, biomeSource, chunkGeneratorSettings);
		this.chunkHeight = j;
		this.chunkWidth = i;
		this.defaultBlock = chunkGeneratorSettings.getDefaultBlock();
		this.defaultFluid = chunkGeneratorSettings.getDefaultFluid();
		this.chunkCountX = 16 / this.chunkWidth;
		this.chunkCountY = k / this.chunkHeight;
		this.chunkCountZ = 16 / this.chunkWidth;
		this.random = new WorldgenRandom(this.seed);
		this.minLimitPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
		this.maxLimitPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
		this.mainPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-7, 0));
		this.surfaceNoise = (SurfaceNoise)(bl
			? new PerlinSimplexNoise(this.random, IntStream.rangeClosed(-3, 0))
			: new PerlinNoise(this.random, IntStream.rangeClosed(-3, 0)));
	}

	private double sampleAndClampNoise(int i, int j, int k, double d, double e, double f, double g) {
		double h = 0.0;
		double l = 0.0;
		double m = 0.0;
		double n = 1.0;

		for (int o = 0; o < 16; o++) {
			double p = PerlinNoise.wrap((double)i * d * n);
			double q = PerlinNoise.wrap((double)j * e * n);
			double r = PerlinNoise.wrap((double)k * d * n);
			double s = e * n;
			ImprovedNoise improvedNoise = this.minLimitPerlinNoise.getOctaveNoise(o);
			if (improvedNoise != null) {
				h += improvedNoise.noise(p, q, r, s, (double)j * s) / n;
			}

			ImprovedNoise improvedNoise2 = this.maxLimitPerlinNoise.getOctaveNoise(o);
			if (improvedNoise2 != null) {
				l += improvedNoise2.noise(p, q, r, s, (double)j * s) / n;
			}

			if (o < 8) {
				ImprovedNoise improvedNoise3 = this.mainPerlinNoise.getOctaveNoise(o);
				if (improvedNoise3 != null) {
					m += improvedNoise3.noise(
							PerlinNoise.wrap((double)i * f * n), PerlinNoise.wrap((double)j * g * n), PerlinNoise.wrap((double)k * f * n), g * n, (double)j * g * n
						)
						/ n;
				}
			}

			n /= 2.0;
		}

		return Mth.clampedLerp(h / 512.0, l / 512.0, (m / 10.0 + 1.0) / 2.0);
	}

	protected double[] makeAndFillNoiseColumn(int i, int j) {
		double[] ds = new double[this.chunkCountY + 1];
		this.fillNoiseColumn(ds, i, j);
		return ds;
	}

	protected void fillNoiseColumn(double[] ds, int i, int j, double d, double e, double f, double g, int k, int l) {
		double[] es = this.getDepthAndScale(i, j);
		double h = es[0];
		double m = es[1];
		double n = this.getTopSlideStart();
		double o = this.getBottomSlideStart();

		for (int p = 0; p < this.getNoiseSizeY(); p++) {
			double q = this.sampleAndClampNoise(i, p, j, d, e, f, g);
			q -= this.getYOffset(h, m, p);
			if ((double)p > n) {
				q = Mth.clampedLerp(q, (double)l, ((double)p - n) / (double)k);
			} else if ((double)p < o) {
				q = Mth.clampedLerp(q, -30.0, (o - (double)p) / (o - 1.0));
			}

			ds[p] = q;
		}
	}

	protected abstract double[] getDepthAndScale(int i, int j);

	protected abstract double getYOffset(double d, double e, int i);

	protected double getTopSlideStart() {
		return (double)(this.getNoiseSizeY() - 4);
	}

	protected double getBottomSlideStart() {
		return 0.0;
	}

	@Override
	public int getBaseHeight(int i, int j, Heightmap.Types types) {
		return this.iterateNoiseColumn(i, j, null, types.isOpaque());
	}

	@Override
	public BlockGetter getBaseColumn(int i, int j) {
		BlockState[] blockStates = new BlockState[this.chunkCountY * this.chunkHeight];
		this.iterateNoiseColumn(i, j, blockStates, null);
		return new NoiseColumn(blockStates);
	}

	private int iterateNoiseColumn(int i, int j, @Nullable BlockState[] blockStates, @Nullable Predicate<BlockState> predicate) {
		int k = Math.floorDiv(i, this.chunkWidth);
		int l = Math.floorDiv(j, this.chunkWidth);
		int m = Math.floorMod(i, this.chunkWidth);
		int n = Math.floorMod(j, this.chunkWidth);
		double d = (double)m / (double)this.chunkWidth;
		double e = (double)n / (double)this.chunkWidth;
		double[][] ds = new double[][]{
			this.makeAndFillNoiseColumn(k, l), this.makeAndFillNoiseColumn(k, l + 1), this.makeAndFillNoiseColumn(k + 1, l), this.makeAndFillNoiseColumn(k + 1, l + 1)
		};

		for (int o = this.chunkCountY - 1; o >= 0; o--) {
			double f = ds[0][o];
			double g = ds[1][o];
			double h = ds[2][o];
			double p = ds[3][o];
			double q = ds[0][o + 1];
			double r = ds[1][o + 1];
			double s = ds[2][o + 1];
			double t = ds[3][o + 1];

			for (int u = this.chunkHeight - 1; u >= 0; u--) {
				double v = (double)u / (double)this.chunkHeight;
				double w = Mth.lerp3(v, d, e, f, q, h, s, g, r, p, t);
				int x = o * this.chunkHeight + u;
				BlockState blockState = this.generateBaseState(w, x);
				if (blockStates != null) {
					blockStates[x] = blockState;
				}

				if (predicate != null && predicate.test(blockState)) {
					return x + 1;
				}
			}
		}

		return 0;
	}

	protected BlockState generateBaseState(double d, int i) {
		BlockState blockState;
		if (d > 0.0) {
			blockState = this.defaultBlock;
		} else if (i < this.getSeaLevel()) {
			blockState = this.defaultFluid;
		} else {
			blockState = AIR;
		}

		return blockState;
	}

	protected abstract void fillNoiseColumn(double[] ds, int i, int j);

	public int getNoiseSizeY() {
		return this.chunkCountY + 1;
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
					.buildSurfaceAt(
						worldgenRandom,
						chunkAccess,
						o,
						p,
						q,
						e,
						this.getSettings().getDefaultBlock(),
						this.getSettings().getDefaultFluid(),
						this.getSeaLevel(),
						this.level.getSeed()
					);
			}
		}

		this.setBedrock(chunkAccess, worldgenRandom);
	}

	protected void setBedrock(ChunkAccess chunkAccess, Random random) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int i = chunkAccess.getPos().getMinBlockX();
		int j = chunkAccess.getPos().getMinBlockZ();
		T chunkGeneratorSettings = this.getSettings();
		int k = chunkGeneratorSettings.getBedrockFloorPosition();
		int l = chunkGeneratorSettings.getBedrockRoofPosition();

		for (BlockPos blockPos : BlockPos.betweenClosed(i, 0, j, i + 15, 0, j + 15)) {
			if (l > 0) {
				for (int m = l; m >= l - 4; m--) {
					if (m >= l - random.nextInt(5)) {
						chunkAccess.setBlockState(mutableBlockPos.set(blockPos.getX(), m, blockPos.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
					}
				}
			}

			if (k < 256) {
				for (int mx = k + 4; mx >= k; mx--) {
					if (mx <= k + random.nextInt(5)) {
						chunkAccess.setBlockState(mutableBlockPos.set(blockPos.getX(), mx, blockPos.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
					}
				}
			}
		}
	}

	@Override
	public void fillFromNoise(LevelAccessor levelAccessor, ChunkAccess chunkAccess) {
		ObjectList<StructurePiece> objectList = new ObjectArrayList<>(10);
		ObjectList<JigsawJunction> objectList2 = new ObjectArrayList<>(32);
		ChunkPos chunkPos = chunkAccess.getPos();
		int i = chunkPos.x;
		int j = chunkPos.z;
		int k = i << 4;
		int l = j << 4;

		for (StructureFeature<?> structureFeature : Feature.NOISE_AFFECTING_FEATURES) {
			String string = structureFeature.getFeatureName();
			LongIterator longIterator = chunkAccess.getReferencesForFeature(string).iterator();

			while (longIterator.hasNext()) {
				long m = longIterator.nextLong();
				ChunkPos chunkPos2 = new ChunkPos(m);
				ChunkAccess chunkAccess2 = levelAccessor.getChunk(chunkPos2.x, chunkPos2.z);
				StructureStart structureStart = chunkAccess2.getStartForFeature(string);
				if (structureStart != null && structureStart.isValid()) {
					for (StructurePiece structurePiece : structureStart.getPieces()) {
						if (structurePiece.isCloseToChunk(chunkPos, 12)) {
							if (structurePiece instanceof PoolElementStructurePiece) {
								PoolElementStructurePiece poolElementStructurePiece = (PoolElementStructurePiece)structurePiece;
								StructureTemplatePool.Projection projection = poolElementStructurePiece.getElement().getProjection();
								if (projection == StructureTemplatePool.Projection.RIGID) {
									objectList.add(poolElementStructurePiece);
								}

								for (JigsawJunction jigsawJunction : poolElementStructurePiece.getJunctions()) {
									int n = jigsawJunction.getSourceX();
									int o = jigsawJunction.getSourceZ();
									if (n > k - 12 && o > l - 12 && n < k + 15 + 12 && o < l + 15 + 12) {
										objectList2.add(jigsawJunction);
									}
								}
							} else {
								objectList.add(structurePiece);
							}
						}
					}
				}
			}
		}

		double[][][] ds = new double[2][this.chunkCountZ + 1][this.chunkCountY + 1];

		for (int p = 0; p < this.chunkCountZ + 1; p++) {
			ds[0][p] = new double[this.chunkCountY + 1];
			this.fillNoiseColumn(ds[0][p], i * this.chunkCountX, j * this.chunkCountZ + p);
			ds[1][p] = new double[this.chunkCountY + 1];
		}

		ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
		Heightmap heightmap = protoChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap heightmap2 = protoChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		ObjectListIterator<StructurePiece> objectListIterator = objectList.iterator();
		ObjectListIterator<JigsawJunction> objectListIterator2 = objectList2.iterator();

		for (int q = 0; q < this.chunkCountX; q++) {
			for (int r = 0; r < this.chunkCountZ + 1; r++) {
				this.fillNoiseColumn(ds[1][r], i * this.chunkCountX + q + 1, j * this.chunkCountZ + r);
			}

			for (int r = 0; r < this.chunkCountZ; r++) {
				LevelChunkSection levelChunkSection = protoChunk.getOrCreateSection(15);
				levelChunkSection.acquire();

				for (int s = this.chunkCountY - 1; s >= 0; s--) {
					double d = ds[0][r][s];
					double e = ds[0][r + 1][s];
					double f = ds[1][r][s];
					double g = ds[1][r + 1][s];
					double h = ds[0][r][s + 1];
					double t = ds[0][r + 1][s + 1];
					double u = ds[1][r][s + 1];
					double v = ds[1][r + 1][s + 1];

					for (int w = this.chunkHeight - 1; w >= 0; w--) {
						int x = s * this.chunkHeight + w;
						int y = x & 15;
						int z = x >> 4;
						if (levelChunkSection.bottomBlockY() >> 4 != z) {
							levelChunkSection.release();
							levelChunkSection = protoChunk.getOrCreateSection(z);
							levelChunkSection.acquire();
						}

						double aa = (double)w / (double)this.chunkHeight;
						double ab = Mth.lerp(aa, d, h);
						double ac = Mth.lerp(aa, f, u);
						double ad = Mth.lerp(aa, e, t);
						double ae = Mth.lerp(aa, g, v);

						for (int af = 0; af < this.chunkWidth; af++) {
							int ag = k + q * this.chunkWidth + af;
							int ah = ag & 15;
							double ai = (double)af / (double)this.chunkWidth;
							double aj = Mth.lerp(ai, ab, ac);
							double ak = Mth.lerp(ai, ad, ae);

							for (int al = 0; al < this.chunkWidth; al++) {
								int am = l + r * this.chunkWidth + al;
								int an = am & 15;
								double ao = (double)al / (double)this.chunkWidth;
								double ap = Mth.lerp(ao, aj, ak);
								double aq = Mth.clamp(ap / 200.0, -1.0, 1.0);
								aq = aq / 2.0 - aq * aq * aq / 24.0;

								while (objectListIterator.hasNext()) {
									StructurePiece structurePiece2 = (StructurePiece)objectListIterator.next();
									BoundingBox boundingBox = structurePiece2.getBoundingBox();
									int ar = Math.max(0, Math.max(boundingBox.x0 - ag, ag - boundingBox.x1));
									int as = x
										- (boundingBox.y0 + (structurePiece2 instanceof PoolElementStructurePiece ? ((PoolElementStructurePiece)structurePiece2).getGroundLevelDelta() : 0));
									int at = Math.max(0, Math.max(boundingBox.z0 - am, am - boundingBox.z1));
									aq += getContribution(ar, as, at) * 0.8;
								}

								objectListIterator.back(objectList.size());

								while (objectListIterator2.hasNext()) {
									JigsawJunction jigsawJunction2 = (JigsawJunction)objectListIterator2.next();
									int au = ag - jigsawJunction2.getSourceX();
									int ar = x - jigsawJunction2.getSourceGroundY();
									int as = am - jigsawJunction2.getSourceZ();
									aq += getContribution(au, ar, as) * 0.4;
								}

								objectListIterator2.back(objectList2.size());
								BlockState blockState = this.generateBaseState(aq, x);
								if (blockState != AIR) {
									if (blockState.getLightEmission() != 0) {
										mutableBlockPos.set(ag, x, am);
										protoChunk.addLight(mutableBlockPos);
									}

									levelChunkSection.setBlockState(ah, y, an, blockState, false);
									heightmap.update(ah, x, an, blockState);
									heightmap2.update(ah, x, an, blockState);
								}
							}
						}
					}
				}

				levelChunkSection.release();
			}

			double[][] es = ds[0];
			ds[0] = ds[1];
			ds[1] = es;
		}
	}

	private static double getContribution(int i, int j, int k) {
		int l = i + 12;
		int m = j + 12;
		int n = k + 12;
		if (l < 0 || l >= 24) {
			return 0.0;
		} else if (m < 0 || m >= 24) {
			return 0.0;
		} else {
			return n >= 0 && n < 24 ? (double)BEARD_KERNEL[n * 24 * 24 + l * 24 + m] : 0.0;
		}
	}

	private static double computeContribution(int i, int j, int k) {
		double d = (double)(i * i + k * k);
		double e = (double)j + 0.5;
		double f = e * e;
		double g = Math.pow(Math.E, -(f / 16.0 + d / 16.0));
		double h = -e * Mth.fastInvSqrt(f / 2.0 + d / 2.0) / 2.0;
		return h * g;
	}
}
