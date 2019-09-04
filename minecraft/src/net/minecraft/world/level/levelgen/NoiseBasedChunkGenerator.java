package net.minecraft.world.level.levelgen;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
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
		this.minLimitPerlinNoise = new PerlinNoise(this.random, 15, 0);
		this.maxLimitPerlinNoise = new PerlinNoise(this.random, 15, 0);
		this.mainPerlinNoise = new PerlinNoise(this.random, 7, 0);
		this.surfaceNoise = (SurfaceNoise)(bl ? new PerlinSimplexNoise(this.random, 3, 0) : new PerlinNoise(this.random, 3, 0));
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
		int k = Math.floorDiv(i, this.chunkWidth);
		int l = Math.floorDiv(j, this.chunkWidth);
		int m = Math.floorMod(i, this.chunkWidth);
		int n = Math.floorMod(j, this.chunkWidth);
		double d = (double)m / (double)this.chunkWidth;
		double e = (double)n / (double)this.chunkWidth;
		double[][] ds = new double[][]{
			this.makeAndFillNoiseColumn(k, l), this.makeAndFillNoiseColumn(k, l + 1), this.makeAndFillNoiseColumn(k + 1, l), this.makeAndFillNoiseColumn(k + 1, l + 1)
		};
		int o = this.getSeaLevel();

		for (int p = this.chunkCountY - 1; p >= 0; p--) {
			double f = ds[0][p];
			double g = ds[1][p];
			double h = ds[2][p];
			double q = ds[3][p];
			double r = ds[0][p + 1];
			double s = ds[1][p + 1];
			double t = ds[2][p + 1];
			double u = ds[3][p + 1];

			for (int v = this.chunkHeight - 1; v >= 0; v--) {
				double w = (double)v / (double)this.chunkHeight;
				double x = Mth.lerp3(w, d, e, f, r, h, t, g, s, q, u);
				int y = p * this.chunkHeight + v;
				if (x > 0.0 || y < o) {
					BlockState blockState;
					if (x > 0.0) {
						blockState = this.defaultBlock;
					} else {
						blockState = this.defaultFluid;
					}

					if (types.isOpaque().test(blockState)) {
						return y + 1;
					}
				}
			}
		}

		return 0;
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
		int i = this.getSeaLevel();
		ObjectList<PoolElementStructurePiece> objectList = new ObjectArrayList<>(10);
		ObjectList<JigsawJunction> objectList2 = new ObjectArrayList<>(32);
		ChunkPos chunkPos = chunkAccess.getPos();
		int j = chunkPos.x;
		int k = chunkPos.z;
		int l = j << 4;
		int m = k << 4;

		for (StructureFeature<?> structureFeature : Feature.NOISE_AFFECTING_FEATURES) {
			String string = structureFeature.getFeatureName();
			LongIterator longIterator = chunkAccess.getReferencesForFeature(string).iterator();

			while (longIterator.hasNext()) {
				long n = longIterator.nextLong();
				ChunkPos chunkPos2 = new ChunkPos(n);
				ChunkAccess chunkAccess2 = levelAccessor.getChunk(chunkPos2.x, chunkPos2.z);
				StructureStart structureStart = chunkAccess2.getStartForFeature(string);
				if (structureStart != null && structureStart.isValid()) {
					for (StructurePiece structurePiece : structureStart.getPieces()) {
						if (structurePiece.isCloseToChunk(chunkPos, 12) && structurePiece instanceof PoolElementStructurePiece) {
							PoolElementStructurePiece poolElementStructurePiece = (PoolElementStructurePiece)structurePiece;
							StructureTemplatePool.Projection projection = poolElementStructurePiece.getElement().getProjection();
							if (projection == StructureTemplatePool.Projection.RIGID) {
								objectList.add(poolElementStructurePiece);
							}

							for (JigsawJunction jigsawJunction : poolElementStructurePiece.getJunctions()) {
								int o = jigsawJunction.getSourceX();
								int p = jigsawJunction.getSourceZ();
								if (o > l - 12 && p > m - 12 && o < l + 15 + 12 && p < m + 15 + 12) {
									objectList2.add(jigsawJunction);
								}
							}
						}
					}
				}
			}
		}

		double[][][] ds = new double[2][this.chunkCountZ + 1][this.chunkCountY + 1];

		for (int q = 0; q < this.chunkCountZ + 1; q++) {
			ds[0][q] = new double[this.chunkCountY + 1];
			this.fillNoiseColumn(ds[0][q], j * this.chunkCountX, k * this.chunkCountZ + q);
			ds[1][q] = new double[this.chunkCountY + 1];
		}

		ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
		Heightmap heightmap = protoChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap heightmap2 = protoChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		ObjectListIterator<PoolElementStructurePiece> objectListIterator = objectList.iterator();
		ObjectListIterator<JigsawJunction> objectListIterator2 = objectList2.iterator();

		for (int r = 0; r < this.chunkCountX; r++) {
			for (int s = 0; s < this.chunkCountZ + 1; s++) {
				this.fillNoiseColumn(ds[1][s], j * this.chunkCountX + r + 1, k * this.chunkCountZ + s);
			}

			for (int s = 0; s < this.chunkCountZ; s++) {
				LevelChunkSection levelChunkSection = protoChunk.getOrCreateSection(15);
				levelChunkSection.acquire();

				for (int t = this.chunkCountY - 1; t >= 0; t--) {
					double d = ds[0][s][t];
					double e = ds[0][s + 1][t];
					double f = ds[1][s][t];
					double g = ds[1][s + 1][t];
					double h = ds[0][s][t + 1];
					double u = ds[0][s + 1][t + 1];
					double v = ds[1][s][t + 1];
					double w = ds[1][s + 1][t + 1];

					for (int x = this.chunkHeight - 1; x >= 0; x--) {
						int y = t * this.chunkHeight + x;
						int z = y & 15;
						int aa = y >> 4;
						if (levelChunkSection.bottomBlockY() >> 4 != aa) {
							levelChunkSection.release();
							levelChunkSection = protoChunk.getOrCreateSection(aa);
							levelChunkSection.acquire();
						}

						double ab = (double)x / (double)this.chunkHeight;
						double ac = Mth.lerp(ab, d, h);
						double ad = Mth.lerp(ab, f, v);
						double ae = Mth.lerp(ab, e, u);
						double af = Mth.lerp(ab, g, w);

						for (int ag = 0; ag < this.chunkWidth; ag++) {
							int ah = l + r * this.chunkWidth + ag;
							int ai = ah & 15;
							double aj = (double)ag / (double)this.chunkWidth;
							double ak = Mth.lerp(aj, ac, ad);
							double al = Mth.lerp(aj, ae, af);

							for (int am = 0; am < this.chunkWidth; am++) {
								int an = m + s * this.chunkWidth + am;
								int ao = an & 15;
								double ap = (double)am / (double)this.chunkWidth;
								double aq = Mth.lerp(ap, ak, al);
								double ar = Mth.clamp(aq / 200.0, -1.0, 1.0);
								ar = ar / 2.0 - ar * ar * ar / 24.0;

								while (objectListIterator.hasNext()) {
									PoolElementStructurePiece poolElementStructurePiece2 = (PoolElementStructurePiece)objectListIterator.next();
									BoundingBox boundingBox = poolElementStructurePiece2.getBoundingBox();
									int as = Math.max(0, Math.max(boundingBox.x0 - ah, ah - boundingBox.x1));
									int at = y - (boundingBox.y0 + poolElementStructurePiece2.getGroundLevelDelta());
									int au = Math.max(0, Math.max(boundingBox.z0 - an, an - boundingBox.z1));
									ar += getContribution(as, at, au) * 0.8;
								}

								objectListIterator.back(objectList.size());

								while (objectListIterator2.hasNext()) {
									JigsawJunction jigsawJunction2 = (JigsawJunction)objectListIterator2.next();
									int av = ah - jigsawJunction2.getSourceX();
									int as = y - jigsawJunction2.getSourceGroundY();
									int at = an - jigsawJunction2.getSourceZ();
									ar += getContribution(av, as, at) * 0.4;
								}

								objectListIterator2.back(objectList2.size());
								BlockState blockState;
								if (ar > 0.0) {
									blockState = this.defaultBlock;
								} else if (y < i) {
									blockState = this.defaultFluid;
								} else {
									blockState = AIR;
								}

								if (blockState != AIR) {
									if (blockState.getLightEmission() != 0) {
										mutableBlockPos.set(ah, y, an);
										protoChunk.addLight(mutableBlockPos);
									}

									levelChunkSection.setBlockState(ai, z, ao, blockState, false);
									heightmap.update(ai, y, ao, blockState);
									heightmap2.update(ai, y, ao, blockState);
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
