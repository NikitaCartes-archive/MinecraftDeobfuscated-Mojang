package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
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
	private static final float[] BEARD_KERNEL = Util.make(new float[13824], fs -> {
		for (int i = 0; i < 24; i++) {
			for (int j = 0; j < 24; j++) {
				for (int k = 0; k < 24; k++) {
					fs[i * 24 * 24 + j * 24 + k] = (float)computeContribution(j - 12, k - 12, i - 12);
				}
			}
		}
	});
	private static final float[] BIOME_WEIGHTS = Util.make(new float[25], fs -> {
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				float f = 10.0F / Mth.sqrt((float)(i * i + j * j) + 0.2F);
				fs[i + 2 + (j + 2) * 5] = f;
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
	private final PerlinNoise depthNoise;
	@Nullable
	private final SimplexNoise islandNoise;
	protected final BlockState defaultBlock;
	protected final BlockState defaultFluid;
	private final long seed;
	protected final Supplier<NoiseGeneratorSettings> settings;
	private final int height;

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
		this.chunkHeight = noiseSettings.noiseSizeVertical() * 4;
		this.chunkWidth = noiseSettings.noiseSizeHorizontal() * 4;
		this.defaultBlock = noiseGeneratorSettings.getDefaultBlock();
		this.defaultFluid = noiseGeneratorSettings.getDefaultFluid();
		this.chunkCountX = 16 / this.chunkWidth;
		this.chunkCountY = noiseSettings.height() / this.chunkHeight;
		this.chunkCountZ = 16 / this.chunkWidth;
		this.random = new WorldgenRandom(l);
		this.minLimitPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
		this.maxLimitPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
		this.mainPerlinNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-7, 0));
		this.surfaceNoise = (SurfaceNoise)(noiseSettings.useSimplexSurfaceNoise()
			? new PerlinSimplexNoise(this.random, IntStream.rangeClosed(-3, 0))
			: new PerlinNoise(this.random, IntStream.rangeClosed(-3, 0)));
		this.random.consumeCount(2620);
		this.depthNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
		if (noiseSettings.islandNoiseOverride()) {
			WorldgenRandom worldgenRandom = new WorldgenRandom(l);
			worldgenRandom.consumeCount(17292);
			this.islandNoise = new SimplexNoise(worldgenRandom);
		} else {
			this.islandNoise = null;
		}
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

	public boolean stable(long l, NoiseGeneratorSettings noiseGeneratorSettings) {
		return this.seed == l && ((NoiseGeneratorSettings)this.settings.get()).stable(noiseGeneratorSettings);
	}

	private double sampleAndClampNoise(int i, int j, int k, double d, double e, double f, double g) {
		double h = 0.0;
		double l = 0.0;
		double m = 0.0;
		boolean bl = true;
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

	private double[] makeAndFillNoiseColumn(int i, int j) {
		double[] ds = new double[this.chunkCountY + 1];
		this.fillNoiseColumn(ds, i, j);
		return ds;
	}

	private void fillNoiseColumn(double[] ds, int i, int j) {
		NoiseSettings noiseSettings = ((NoiseGeneratorSettings)this.settings.get()).noiseSettings();
		double d;
		double e;
		if (this.islandNoise != null) {
			d = (double)(TheEndBiomeSource.getHeightValue(this.islandNoise, i, j) - 8.0F);
			if (d > 0.0) {
				e = 0.25;
			} else {
				e = 1.0;
			}
		} else {
			float f = 0.0F;
			float g = 0.0F;
			float h = 0.0F;
			int k = 2;
			int l = this.getSeaLevel();
			float m = this.biomeSource.getNoiseBiome(i, l, j).getDepth();

			for (int n = -2; n <= 2; n++) {
				for (int o = -2; o <= 2; o++) {
					Biome biome = this.biomeSource.getNoiseBiome(i + n, l, j + o);
					float p = biome.getDepth();
					float q = biome.getScale();
					float r;
					float s;
					if (noiseSettings.isAmplified() && p > 0.0F) {
						r = 1.0F + p * 2.0F;
						s = 1.0F + q * 4.0F;
					} else {
						r = p;
						s = q;
					}

					float t = p > m ? 0.5F : 1.0F;
					float u = t * BIOME_WEIGHTS[n + 2 + (o + 2) * 5] / (r + 2.0F);
					f += s * u;
					g += r * u;
					h += u;
				}
			}

			float v = g / h;
			float w = f / h;
			double x = (double)(v * 0.5F - 0.125F);
			double y = (double)(w * 0.9F + 0.1F);
			d = x * 0.265625;
			e = 96.0 / y;
		}

		double z = 684.412 * noiseSettings.noiseSamplingSettings().xzScale();
		double aa = 684.412 * noiseSettings.noiseSamplingSettings().yScale();
		double ab = z / noiseSettings.noiseSamplingSettings().xzFactor();
		double ac = aa / noiseSettings.noiseSamplingSettings().yFactor();
		double x = (double)noiseSettings.topSlideSettings().target();
		double y = (double)noiseSettings.topSlideSettings().size();
		double ad = (double)noiseSettings.topSlideSettings().offset();
		double ae = (double)noiseSettings.bottomSlideSettings().target();
		double af = (double)noiseSettings.bottomSlideSettings().size();
		double ag = (double)noiseSettings.bottomSlideSettings().offset();
		double ah = noiseSettings.randomDensityOffset() ? this.getRandomDensity(i, j) : 0.0;
		double ai = noiseSettings.densityFactor();
		double aj = noiseSettings.densityOffset();

		for (int ak = 0; ak <= this.chunkCountY; ak++) {
			double al = this.sampleAndClampNoise(i, ak, j, z, aa, ab, ac);
			double am = 1.0 - (double)ak * 2.0 / (double)this.chunkCountY + ah;
			double an = am * ai + aj;
			double ao = (an + d) * e;
			if (ao > 0.0) {
				al += ao * 4.0;
			} else {
				al += ao;
			}

			if (y > 0.0) {
				double ap = ((double)(this.chunkCountY - ak) - ad) / y;
				al = Mth.clampedLerp(x, al, ap);
			}

			if (af > 0.0) {
				double ap = ((double)ak - ag) / af;
				al = Mth.clampedLerp(ae, al, ap);
			}

			ds[ak] = al;
		}
	}

	private double getRandomDensity(int i, int j) {
		double d = this.depthNoise.getValue((double)(i * 200), 10.0, (double)(j * 200), 1.0, 0.0, true);
		double e;
		if (d < 0.0) {
			e = -d * 0.3;
		} else {
			e = d;
		}

		double f = e * 24.575625 - 2.0;
		return f < 0.0 ? f * 0.009486607142857142 : Math.min(f, 1.0) * 0.006640625;
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
		boolean bl = l + 4 >= 0 && l < this.height;
		boolean bl2 = k + 4 >= 0 && k < this.height;
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
		ObjectList<StructurePiece> objectList = new ObjectArrayList<>(10);
		ObjectList<JigsawJunction> objectList2 = new ObjectArrayList<>(32);
		ChunkPos chunkPos = chunkAccess.getPos();
		int i = chunkPos.x;
		int j = chunkPos.z;
		int k = i << 4;
		int l = j << 4;

		for (StructureFeature<?> structureFeature : StructureFeature.NOISE_AFFECTING_FEATURES) {
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

	@Override
	public int getGenDepth() {
		return this.height;
	}

	@Override
	public int getSeaLevel() {
		return ((NoiseGeneratorSettings)this.settings.get()).seaLevel();
	}

	@Override
	public List<Biome.SpawnerData> getMobsAt(Biome biome, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos) {
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
			int i = worldGenRegion.getCenterX();
			int j = worldGenRegion.getCenterZ();
			Biome biome = worldGenRegion.getBiome(new ChunkPos(i, j).getWorldPosition());
			WorldgenRandom worldgenRandom = new WorldgenRandom();
			worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), i << 4, j << 4);
			NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, biome, i, j, worldgenRandom);
		}
	}
}
