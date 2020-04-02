package net.minecraft.world.level.levelgen;

import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class OverworldLevelSource extends NoiseBasedChunkGenerator<OverworldGeneratorSettings> {
	private static final float[] BIOME_WEIGHTS = Util.make(new float[25], fs -> {
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				float f = 10.0F / Mth.sqrt((float)(i * i + j * j) + 0.2F);
				fs[i + 2 + (j + 2) * 5] = f;
			}
		}
	});
	private final PerlinNoise depthNoise;
	private final boolean isAmplified;
	private final PhantomSpawner phantomSpawner = new PhantomSpawner();
	private final PatrolSpawner patrolSpawner = new PatrolSpawner();
	private final CatSpawner catSpawner = new CatSpawner();
	private final VillageSiege villageSiege = new VillageSiege();

	public OverworldLevelSource(LevelAccessor levelAccessor, BiomeSource biomeSource, OverworldGeneratorSettings overworldGeneratorSettings) {
		super(levelAccessor, biomeSource, 4, 8, 256, overworldGeneratorSettings, true);
		this.random.consumeCount(2620);
		this.depthNoise = new PerlinNoise(this.random, IntStream.rangeClosed(-15, 0));
		this.isAmplified = levelAccessor.getLevelData().getGeneratorType() == LevelType.AMPLIFIED;
	}

	@Override
	public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
		int i = worldGenRegion.getCenterX();
		int j = worldGenRegion.getCenterZ();
		Biome biome = worldGenRegion.getBiome(new ChunkPos(i, j).getWorldPosition());
		WorldgenRandom worldgenRandom = new WorldgenRandom();
		worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), i << 4, j << 4);
		NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, biome, i, j, worldgenRandom);
	}

	@Override
	protected void fillNoiseColumn(double[] ds, int i, int j) {
		double d = 684.412F;
		double e = 684.412F;
		double f = 8.555149841308594;
		double g = 4.277574920654297;
		int k = -10;
		int l = 3;
		this.fillNoiseColumn(ds, i, j, 684.412F, 684.412F, 8.555149841308594, 4.277574920654297, 3, -10);
	}

	@Override
	protected double getYOffset(double d, double e, int i) {
		double f = 8.5;
		double g = ((double)i - (8.5 + d * 8.5 / 8.0 * 4.0)) * 12.0 * 128.0 / 256.0 / e;
		if (g < 0.0) {
			g *= 4.0;
		}

		return g;
	}

	@Override
	protected double[] getDepthAndScale(int i, int j) {
		double[] ds = new double[2];
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
				if (this.isAmplified && p > 0.0F) {
					p = 1.0F + p * 2.0F;
					q = 1.0F + q * 4.0F;
				}

				float r = BIOME_WEIGHTS[n + 2 + (o + 2) * 5] / (p + 2.0F);
				if (biome.getDepth() > m) {
					r /= 2.0F;
				}

				f += q * r;
				g += p * r;
				h += r;
			}
		}

		f /= h;
		g /= h;
		f = f * 0.9F + 0.1F;
		g = (g * 4.0F - 1.0F) / 8.0F;
		ds[0] = (double)g + this.getRdepth(i, j);
		ds[1] = (double)f;
		return ds;
	}

	private double getRdepth(int i, int j) {
		double d = this.depthNoise.getValue((double)(i * 200), 10.0, (double)(j * 200), 1.0, 0.0, true) * 65535.0 / 8000.0;
		if (d < 0.0) {
			d = -d * 0.3;
		}

		d = d * 3.0 - 2.0;
		if (d < 0.0) {
			d /= 28.0;
		} else {
			if (d > 1.0) {
				d = 1.0;
			}

			d /= 40.0;
		}

		return d;
	}

	@Override
	public List<Biome.SpawnerData> getMobsAt(StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos) {
		if (Feature.SWAMP_HUT.isSwamphut(this.level, structureFeatureManager, blockPos)) {
			if (mobCategory == MobCategory.MONSTER) {
				return Feature.SWAMP_HUT.getSpecialEnemies();
			}

			if (mobCategory == MobCategory.CREATURE) {
				return Feature.SWAMP_HUT.getSpecialAnimals();
			}
		} else if (mobCategory == MobCategory.MONSTER) {
			if (Feature.PILLAGER_OUTPOST.isInsideBoundingFeature(this.level, structureFeatureManager, blockPos)) {
				return Feature.PILLAGER_OUTPOST.getSpecialEnemies();
			}

			if (Feature.OCEAN_MONUMENT.isInsideBoundingFeature(this.level, structureFeatureManager, blockPos)) {
				return Feature.OCEAN_MONUMENT.getSpecialEnemies();
			}
		}

		return super.getMobsAt(structureFeatureManager, mobCategory, blockPos);
	}

	@Override
	public void tickCustomSpawners(ServerLevel serverLevel, boolean bl, boolean bl2) {
		this.phantomSpawner.tick(serverLevel, bl, bl2);
		this.patrolSpawner.tick(serverLevel, bl, bl2);
		this.catSpawner.tick(serverLevel, bl, bl2);
		this.villageSiege.tick(serverLevel, bl, bl2);
	}

	@Override
	public int getSpawnHeight() {
		return this.level.getSeaLevel() + 1;
	}

	@Override
	public int getSeaLevel() {
		return 63;
	}
}
