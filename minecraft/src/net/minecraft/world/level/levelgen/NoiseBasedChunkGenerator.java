package net.minecraft.world.level.levelgen;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
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
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.SurfaceNoise;

public abstract class NoiseBasedChunkGenerator<T extends NoiseGeneratorSettings> extends ChunkGenerator {
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
	private final int bedrockFloorPosition;
	private final int bedrockRoofPosition;

	public NoiseBasedChunkGenerator(BiomeSource biomeSource, long l, T noiseGeneratorSettings, int i, int j, int k, boolean bl) {
		super(biomeSource, noiseGeneratorSettings.structureSettings());
		this.chunkHeight = j;
		this.chunkWidth = i;
		this.defaultBlock = noiseGeneratorSettings.getDefaultBlock();
		this.defaultFluid = noiseGeneratorSettings.getDefaultFluid();
		this.chunkCountX = 16 / this.chunkWidth;
		this.chunkCountY = k / this.chunkHeight;
		this.chunkCountZ = 16 / this.chunkWidth;
		this.random = new WorldgenRandom(l);
		this.minLimitPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
		this.maxLimitPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
		this.mainPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-7, 0));
		this.surfaceNoise = (SurfaceNoise)(bl
			? new PerlinSimplexNoise(this.random, IntStream.rangeClosed(-3, 0))
			: new PerlinNoise(this.random, IntStream.rangeClosed(-3, 0)));
		this.bedrockFloorPosition = noiseGeneratorSettings.getBedrockFloorPosition();
		this.bedrockRoofPosition = noiseGeneratorSettings.getBedrockRoofPosition();
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
					.buildSurfaceAt(worldgenRandom, chunkAccess, o, p, q, e, this.defaultBlock, this.defaultFluid, this.getSeaLevel(), worldGenRegion.getSeed());
			}
		}

		this.setBedrock(chunkAccess, worldgenRandom);
	}

	protected void setBedrock(ChunkAccess chunkAccess, Random random) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int i = chunkAccess.getPos().getMinBlockX();
		int j = chunkAccess.getPos().getMinBlockZ();
		int k = this.bedrockFloorPosition;
		int l = this.bedrockRoofPosition;

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
	public void fillFromNoise(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		ObjectList<StructurePiece> objectList = new ObjectArrayList<>(10);
		ObjectList<JigsawJunction> objectList2 = new ObjectArrayList<>(32);
		ChunkPos chunkPos = chunkAccess.getPos();
		int i = chunkPos.x;
		int j = chunkPos.z;
		int k = i << 4;
		int l = j << 4;

		for (StructureFeature<?> structureFeature : Feature.NOISE_AFFECTING_FEATURES) {
			structureFeatureManager.startsForFeature(SectionPos.of(chunkPos, 0), structureFeature).forEach(structureStart -> {
				for (StructurePiece structurePiece : structureStart.getPieces()) {
					if (structurePiece.isCloseToChunk(chunkPos, 12)) {
						if (structurePiece instanceof PoolElementStructurePiece) {
							PoolElementStructurePiece poolElementStructurePiece = (PoolElementStructurePiece)structurePiece;
							StructureTemplatePool.Projection projection = poolElementStructurePiece.getElement().getProjection();
							if (projection == StructureTemplatePool.Projection.RIGID) {
								objectList.add(poolElementStructurePiece);
							}

							for (JigsawJunction jigsawJunction : poolElementStructurePiece.getJunctions()) {
								int kx = jigsawJunction.getSourceX();
								int lx = jigsawJunction.getSourceZ();
								if (kx > k - 12 && lx > l - 12 && kx < k + 15 + 12 && lx < l + 15 + 12) {
									objectList2.add(jigsawJunction);
								}
							}
						} else {
							objectList.add(structurePiece);
						}
					}
				}
			});
		}

		double[][][] ds = new double[2][this.chunkCountZ + 1][this.chunkCountY + 1];

		for (int m = 0; m < this.chunkCountZ + 1; m++) {
			ds[0][m] = new double[this.chunkCountY + 1];
			this.fillNoiseColumn(ds[0][m], i * this.chunkCountX, j * this.chunkCountZ + m);
			ds[1][m] = new double[this.chunkCountY + 1];
		}

		ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
		Heightmap heightmap = protoChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap heightmap2 = protoChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		ObjectListIterator<StructurePiece> objectListIterator = objectList.iterator();
		ObjectListIterator<JigsawJunction> objectListIterator2 = objectList2.iterator();

		for (int n = 0; n < this.chunkCountX; n++) {
			for (int o = 0; o < this.chunkCountZ + 1; o++) {
				this.fillNoiseColumn(ds[1][o], i * this.chunkCountX + n + 1, j * this.chunkCountZ + o);
			}

			for (int o = 0; o < this.chunkCountZ; o++) {
				LevelChunkSection levelChunkSection = protoChunk.getOrCreateSection(15);
				levelChunkSection.acquire();

				for (int p = this.chunkCountY - 1; p >= 0; p--) {
					double d = ds[0][o][p];
					double e = ds[0][o + 1][p];
					double f = ds[1][o][p];
					double g = ds[1][o + 1][p];
					double h = ds[0][o][p + 1];
					double q = ds[0][o + 1][p + 1];
					double r = ds[1][o][p + 1];
					double s = ds[1][o + 1][p + 1];

					for (int t = this.chunkHeight - 1; t >= 0; t--) {
						int u = p * this.chunkHeight + t;
						int v = u & 15;
						int w = u >> 4;
						if (levelChunkSection.bottomBlockY() >> 4 != w) {
							levelChunkSection.release();
							levelChunkSection = protoChunk.getOrCreateSection(w);
							levelChunkSection.acquire();
						}

						double x = (double)t / (double)this.chunkHeight;
						double y = Mth.lerp(x, d, h);
						double z = Mth.lerp(x, f, r);
						double aa = Mth.lerp(x, e, q);
						double ab = Mth.lerp(x, g, s);

						for (int ac = 0; ac < this.chunkWidth; ac++) {
							int ad = k + n * this.chunkWidth + ac;
							int ae = ad & 15;
							double af = (double)ac / (double)this.chunkWidth;
							double ag = Mth.lerp(af, y, z);
							double ah = Mth.lerp(af, aa, ab);

							for (int ai = 0; ai < this.chunkWidth; ai++) {
								int aj = l + o * this.chunkWidth + ai;
								int ak = aj & 15;
								double al = (double)ai / (double)this.chunkWidth;
								double am = Mth.lerp(al, ag, ah);
								double an = Mth.clamp(am / 200.0, -1.0, 1.0);
								an = an / 2.0 - an * an * an / 24.0;

								while (objectListIterator.hasNext()) {
									StructurePiece structurePiece = (StructurePiece)objectListIterator.next();
									BoundingBox boundingBox = structurePiece.getBoundingBox();
									int ao = Math.max(0, Math.max(boundingBox.x0 - ad, ad - boundingBox.x1));
									int ap = u
										- (boundingBox.y0 + (structurePiece instanceof PoolElementStructurePiece ? ((PoolElementStructurePiece)structurePiece).getGroundLevelDelta() : 0));
									int aq = Math.max(0, Math.max(boundingBox.z0 - aj, aj - boundingBox.z1));
									an += getContribution(ao, ap, aq) * 0.8;
								}

								objectListIterator.back(objectList.size());

								while (objectListIterator2.hasNext()) {
									JigsawJunction jigsawJunction = (JigsawJunction)objectListIterator2.next();
									int ar = ad - jigsawJunction.getSourceX();
									int ao = u - jigsawJunction.getSourceGroundY();
									int ap = aj - jigsawJunction.getSourceZ();
									an += getContribution(ar, ao, ap) * 0.4;
								}

								objectListIterator2.back(objectList2.size());
								BlockState blockState = this.generateBaseState(an, u);
								if (blockState != AIR) {
									if (blockState.getLightEmission() != 0) {
										mutableBlockPos.set(ad, u, aj);
										protoChunk.addLight(mutableBlockPos);
									}

									levelChunkSection.setBlockState(ae, v, ak, blockState, false);
									heightmap.update(ae, u, ak, blockState);
									heightmap2.update(ae, u, ak, blockState);
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
